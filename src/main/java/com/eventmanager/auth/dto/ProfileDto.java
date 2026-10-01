package com.eventmanager.auth.dto;

import com.eventmanager.auth.model.Role;
import com.eventmanager.auth.model.User;

public record ProfileDto(Long id, String email, String firstName, String lastName, String phone, Role role, String provider) {
    public static ProfileDto from(User u) {
        return new ProfileDto(u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(), u.getPhone(), u.getRole(), u.getProvider());
    }
}
