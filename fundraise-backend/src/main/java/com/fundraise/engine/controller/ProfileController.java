package com.fundraise.engine.controller;

import com.fundraise.engine.dto.ChangePasswordRequest;
import com.fundraise.engine.dto.UpdateProfileRequest;
import com.fundraise.engine.dto.UserProfileResponse;
import com.fundraise.engine.entity.User;
import com.fundraise.engine.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Get profile", description = "Get the current user's profile.")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    @PutMapping
    @Operation(summary = "Update profile", description = "Update the current user's name or email.")
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.updateProfile(user, request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change the current user's password.")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        User user = (User) authentication.getPrincipal();
        profileService.changePassword(user, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
