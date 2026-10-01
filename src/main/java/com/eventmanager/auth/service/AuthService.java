package com.eventmanager.auth.service;

import com.eventmanager.auth.dto.*;
import com.eventmanager.auth.exception.ApiException;
import com.eventmanager.auth.model.RefreshToken;
import com.eventmanager.auth.model.Role;
import com.eventmanager.auth.model.User;
import com.eventmanager.auth.repository.RefreshTokenRepository;
import com.eventmanager.auth.repository.UserRepository;
import com.eventmanager.auth.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String INVALID = "Identifiants invalides";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final int maxFailed;
    private final long lockMinutes;
    private final long refreshDays;
    private final String dummyHash;

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder encoder,
                       JwtService jwt,
                       @Value("${app.security.max-failed-attempts}") int maxFailed,
                       @Value("${app.security.lock-minutes}") long lockMinutes,
                       @Value("${app.jwt.refresh-token-days}") long refreshDays) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.encoder = encoder;
        this.jwt = jwt;
        this.maxFailed = maxFailed;
        this.lockMinutes = lockMinutes;
        this.refreshDays = refreshDays;
        // Hash factice pour uniformiser le temps de réponse quand l'email n'existe pas
        this.dummyHash = encoder.encode("dummy-password-Aa1");
    }

    @Transactional
    public ProfileDto register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        Role role = req.role() == null ? Role.USER : req.role();
        if (role == Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rôle non autorisé à l'inscription");
        }
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Un compte existe déjà avec cet email");
        }
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        u.setFirstName(req.firstName().trim());
        u.setLastName(req.lastName().trim());
        u.setPhone(req.phone());
        u.setRole(role);
        u.setProvider("LOCAL");
        return ProfileDto.from(users.save(u));
    }

    // noRollbackFor : les compteurs d'échecs doivent être conservés même quand on lève l'exception
    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = users.findByEmail(email).orElse(null);

        if (user == null) {
            encoder.matches(req.password(), dummyHash);
            throw new ApiException(HttpStatus.UNAUTHORIZED, INVALID);
        }
        if (user.getPasswordHash() == null) {
            // Compte créé via OAuth uniquement
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "Ce compte utilise la connexion " + user.getProvider() + ". Utilisez le bouton correspondant.");
        }
        if (!user.isEnabled()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, INVALID);
        }
        if (!user.isAccountNonLocked()) {
            throw new ApiException(HttpStatus.LOCKED, "Compte temporairement verrouillé, réessayez plus tard");
        }
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= maxFailed) {
                user.setLockedUntil(Instant.now().plus(lockMinutes, ChronoUnit.MINUTES));
                user.setFailedAttempts(0);
            }
            users.save(user);
            throw new ApiException(HttpStatus.UNAUTHORIZED, INVALID);
        }
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        users.save(user);
        return issueTokens(user);
    }

    /** Rotation : l'ancien refresh token est révoqué, un nouveau couple de tokens est émis. */
    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken stored = refreshTokens.findByTokenHash(hash(req.refreshToken()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token invalide"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            // Réutilisation d'un token révoqué = vol possible : on révoque toute la famille
            if (stored.isRevoked()) refreshTokens.revokeAllForUser(stored.getUser());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token invalide");
        }
        stored.setRevoked(true);
        User user = stored.getUser();
        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token invalide");
        }
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshRequest req) {
        refreshTokens.findByTokenHash(hash(req.refreshToken())).ifPresent(t -> t.setRevoked(true));
    }

    /**
     * Connexion ou inscription via OAuth2 (Google / GitHub / Spotify).
     * Si l'email existe déjà avec le même provider → login.
     * Si l'email existe avec un autre provider ou en LOCAL → on refuse (conflit).
     * Sinon → création du compte.
     */
    @Transactional
    public AuthResponse loginOrRegisterOAuth(String email, String firstName, String lastName,
                                             String provider, String providerId) {
        if (email == null || email.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email non fourni par le provider OAuth");
        }
        email = email.trim().toLowerCase();
        firstName = (firstName == null || firstName.isBlank()) ? "User" : firstName.trim();
        lastName = (lastName == null) ? "" : lastName.trim();

        User existing = users.findByEmail(email).orElse(null);

        if (existing != null) {
            // Compte déjà existant
            if (!provider.equalsIgnoreCase(existing.getProvider())) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Un compte existe déjà avec cet email via " + existing.getProvider()
                                + ". Connectez-vous avec ce provider ou utilisez un autre email.");
            }
            if (!existing.isEnabled()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Compte désactivé");
            }
            if (!existing.isAccountNonLocked()) {
                throw new ApiException(HttpStatus.LOCKED, "Compte temporairement verrouillé");
            }
            // Mise à jour éventuelle du providerId
            if (providerId != null && !providerId.equals(existing.getProviderId())) {
                existing.setProviderId(providerId);
                users.save(existing);
            }
            return issueTokens(existing);
        }

        // Nouveau compte OAuth
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(null); // pas de mot de passe
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setProvider(provider.toUpperCase());
        u.setProviderId(providerId);
        u.setRole(Role.USER);
        u = users.save(u);
        return issueTokens(u);
    }

    private AuthResponse issueTokens(User user) {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String refreshPlain = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hash(refreshPlain));
        rt.setExpiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS));
        refreshTokens.save(rt);

        String access = jwt.generateAccessToken(user.getEmail(), user.getRole().name());
        return AuthResponse.bearer(access, refreshPlain, jwt.accessTtlSeconds());
    }

    static String hash(String value) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
