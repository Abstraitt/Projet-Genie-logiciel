package com.eventmanager.auth.dto;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {
    public static AuthResponse bearer(String access, String refresh, long expiresIn) {
        return new AuthResponse(access, refresh, "Bearer", expiresIn);
    }
}
