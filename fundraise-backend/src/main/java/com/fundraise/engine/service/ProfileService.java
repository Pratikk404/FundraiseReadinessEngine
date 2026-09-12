package com.fundraise.engine.service;

import com.fundraise.engine.dto.ChangePasswordRequest;
import com.fundraise.engine.dto.UpdateProfileRequest;
import com.fundraise.engine.dto.UserProfileResponse;
import com.fundraise.engine.entity.User;
import com.fundraise.engine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .plan(user.getPlan().name())
                .verified(user.isVerified())
                .build();
    }

    public UserProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new IllegalArgumentException("Email already in use");
                }
                user.setEmail(request.getEmail());
            }
        }

        userRepository.save(user);

        return getProfile(user);
    }

    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
