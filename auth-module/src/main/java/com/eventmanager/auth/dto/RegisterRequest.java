package com.eventmanager.auth.dto;

import com.eventmanager.auth.model.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,64}$",
                 message = "Le mot de passe doit contenir 8 à 64 caractères, avec une minuscule, une majuscule et un chiffre")
        String password,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Pattern(regexp = "^\\+?[0-9 ]{6,20}$", message = "Numéro de téléphone invalide") String phone,
        /** USER (défaut) ou ORGANIZER. ADMIN est refusé. */
        Role role) {}
