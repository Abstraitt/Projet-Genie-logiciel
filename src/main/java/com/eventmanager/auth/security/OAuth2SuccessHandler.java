package com.eventmanager.auth.security;

import com.eventmanager.auth.dto.AuthResponse;
import com.eventmanager.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final String successRedirectUrl;

    public OAuth2SuccessHandler(@Lazy AuthService authService,
                                @Value("${app.oauth2.success-redirect-url}") String successRedirectUrl) {
        this.authService = authService;
        this.successRedirectUrl = successRedirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // google, github, spotify

        String email = extractEmail(oauthUser, registrationId);
        String firstName = extractFirstName(oauthUser, registrationId);
        String lastName = extractLastName(oauthUser, registrationId);
        String providerId = extractProviderId(oauthUser, registrationId);

        AuthResponse tokens = authService.loginOrRegisterOAuth(
                email, firstName, lastName, registrationId.toUpperCase(), providerId);

        String redirectUrl = UriComponentsBuilder.fromUriString(successRedirectUrl)
                .queryParam("accessToken", tokens.accessToken())
                .queryParam("refreshToken", tokens.refreshToken())
                .queryParam("expiresIn", tokens.expiresInSeconds())
                .queryParam("tokenType", tokens.tokenType())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String extractEmail(OAuth2User user, String provider) {
        Map<String, Object> attrs = user.getAttributes();
        return switch (provider) {
            case "google" -> (String) attrs.get("email");
            case "github" -> {
                String email = (String) attrs.get("email");
                if (email == null || email.isBlank()) {
                    // GitHub may not return email if private
                    yield attrs.get("login") + "@users.noreply.github.com";
                }
                yield email;
            }
            case "spotify" -> (String) attrs.get("email");
            default -> throw new IllegalArgumentException("Provider non supporté: " + provider);
        };
    }

    private String extractFirstName(OAuth2User user, String provider) {
        Map<String, Object> attrs = user.getAttributes();
        return switch (provider) {
            case "google" -> {
                String given = (String) attrs.get("given_name");
                yield given != null ? given : "User";
            }
            case "github" -> {
                String name = (String) attrs.get("name");
                if (name != null && name.contains(" ")) {
                    yield name.split(" ")[0];
                }
                yield name != null ? name : (String) attrs.get("login");
            }
            case "spotify" -> {
                String display = (String) attrs.get("display_name");
                if (display != null && display.contains(" ")) {
                    yield display.split(" ")[0];
                }
                yield display != null ? display : "Spotify";
            }
            default -> "User";
        };
    }

    private String extractLastName(OAuth2User user, String provider) {
        Map<String, Object> attrs = user.getAttributes();
        return switch (provider) {
            case "google" -> {
                String family = (String) attrs.get("family_name");
                yield family != null ? family : "";
            }
            case "github" -> {
                String name = (String) attrs.get("name");
                if (name != null && name.contains(" ")) {
                    String[] parts = name.split(" ", 2);
                    yield parts.length > 1 ? parts[1] : "";
                }
                yield "";
            }
            case "spotify" -> {
                String display = (String) attrs.get("display_name");
                if (display != null && display.contains(" ")) {
                    String[] parts = display.split(" ", 2);
                    yield parts.length > 1 ? parts[1] : "";
                }
                yield "";
            }
            default -> "";
        };
    }

    private String extractProviderId(OAuth2User user, String provider) {
        Map<String, Object> attrs = user.getAttributes();
        return switch (provider) {
            case "google" -> (String) attrs.get("sub");
            case "github" -> String.valueOf(attrs.get("id"));
            case "spotify" -> (String) attrs.get("id");
            default -> null;
        };
    }
}
