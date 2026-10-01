package com.eventmanager.auth.dto;

import jakarta.validation.constraints.*;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @Pattern(regexp = "^\\+?[0-9 ]{6,20}$", message = "Numéro de téléphone invalide") String phone) {}
