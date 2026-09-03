package com.fundraise.engine.service;

import com.fundraise.engine.dto.AuthRequest;
import com.fundraise.engine.dto.AuthResponse;
import com.fundraise.engine.dto.LoginRequest;
import com.fundraise.engine.entity.User;
import com.fundraise.engine.repository.UserRepository;
import com.fundraise.engine.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private AuthRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new AuthRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Register creates user and returns JWT token")
    void registerSuccess() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(java.util.UUID.randomUUID());
            return user;
        });
        when(jwtUtil.generateToken("test@example.com", "FOUNDER")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals("FOUNDER", response.getRole());
        assertEquals("mock-jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register fails when email already exists")
    void registerDuplicateEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest));

        assertTrue(ex.getMessage().contains("already registered"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login returns JWT token for valid credentials")
    void loginSuccess() {
        User existingUser = User.builder()
                .id(java.util.UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .password("encoded_password")
                .role(User.Role.FOUNDER)
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com", "FOUNDER")).thenReturn("login-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("login-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    @DisplayName("Login fails with invalid email")
    void loginInvalidEmail() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(loginRequest));

        assertTrue(ex.getMessage().contains("Invalid email or password"));
    }

    @Test
    @DisplayName("Login fails with wrong password")
    void loginWrongPassword() {
        User existingUser = User.builder()
                .id(java.util.UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .password("encoded_password")
                .role(User.Role.FOUNDER)
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(loginRequest));

        assertTrue(ex.getMessage().contains("Invalid email or password"));
    }
}
