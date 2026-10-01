package com.eventmanager.auth.service;

import com.eventmanager.auth.dto.*;
import com.eventmanager.auth.exception.ApiException;
import com.eventmanager.auth.model.User;
import com.eventmanager.auth.repository.RefreshTokenRepository;
import com.eventmanager.auth.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder encoder) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public ProfileDto getProfile(User current) {
        return ProfileDto.from(load(current));
    }

    @Transactional
    public ProfileDto updateProfile(User current, UpdateProfileRequest req) {
        User u = load(current);
        u.setFirstName(req.firstName().trim());
        u.setLastName(req.lastName().trim());
        u.setPhone(req.phone());
        return ProfileDto.from(users.save(u));
    }

    @Transactional
    public void changePassword(User current, ChangePasswordRequest req) {
        User u = load(current);
        if (!encoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mot de passe actuel incorrect");
        }
        u.setPasswordHash(encoder.encode(req.newPassword()));
        users.save(u);
        refreshTokens.revokeAllForUser(u); // déconnecte les autres appareils
    }

    @Transactional
    public void deleteAccount(User current) {
        User u = load(current);
        refreshTokens.revokeAllForUser(u);
        u.setEnabled(false);
        users.save(u);
    }

    @Transactional(readOnly = true)
    public List<ProfileDto> listAll() {
        return users.findAll().stream().map(ProfileDto::from).toList();
    }

    private User load(User current) {
        return users.findById(current.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }
}
