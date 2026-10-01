package com.eventmanager.auth.controller;

import com.eventmanager.auth.dto.*;
import com.eventmanager.auth.model.User;
import com.eventmanager.auth.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService users;

    public UserController(UserService users) { this.users = users; }

    @GetMapping("/api/users/me")
    public ProfileDto me(@AuthenticationPrincipal User current) { return users.getProfile(current); }

    @PutMapping("/api/users/me")
    public ProfileDto update(@AuthenticationPrincipal User current, @Valid @RequestBody UpdateProfileRequest req) {
        return users.updateProfile(current, req);
    }

    @PutMapping("/api/users/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User current,
                                               @Valid @RequestBody ChangePasswordRequest req) {
        users.changePassword(current, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/users/me")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User current) {
        users.deleteAccount(current);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/admin/users")
    public List<ProfileDto> all() { return users.listAll(); }
}
