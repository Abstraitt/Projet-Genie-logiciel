package com.eventmanager.auth.dto;

import jakarta.validation.constraints.*;

public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$",
                 message = "Le mot de passe doit contenir 8 à 64 caractères, avec une minuscule, une majuscule et un chiffre")
        String newPassword) {}
