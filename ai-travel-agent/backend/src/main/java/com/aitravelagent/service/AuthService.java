package com.aitravelagent.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.aitravelagent.dto.AuthResponse;
import com.aitravelagent.dto.AuthUserResponse;
import com.aitravelagent.dto.LoginRequest;
import com.aitravelagent.dto.RegisterRequest;
import com.aitravelagent.entity.User;
import com.aitravelagent.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        RegisterRequest safeRequest = request == null
                ? new RegisterRequest(null, null, null)
                : request;
        String fullName = defaultString(safeRequest.fullName(), "AI Travel Agent User");
        String email = normalizeEmail(safeRequest.email());
        String password = defaultString(safeRequest.password(), "");

        if (email.isBlank() || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        return toAuthResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest request) {
        LoginRequest safeRequest = request == null
                ? new LoginRequest(null, null)
                : request;
        String email = normalizeEmail(safeRequest.email());
        String password = defaultString(safeRequest.password(), "");

        User user = userRepository.findByEmailIgnoreCase(email)
                .filter(existingUser -> passwordEncoder.matches(password, existingUser.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        return toAuthResponse(user);
    }

    public AuthUserResponse getCurrentUser(String email) {
        String safeEmail = normalizeEmail(email);
        return userRepository.findByEmailIgnoreCase(safeEmail)
                .map(this::toUserResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated"));
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(user.getEmail()),
                toUserResponse(user)
        );
    }

    private AuthUserResponse toUserResponse(User user) {
        return new AuthUserResponse(
                user.getId(),
                defaultString(user.getFullName(), "AI Travel Agent User"),
                user.getEmail()
        );
    }

    private String normalizeEmail(String value) {
        return defaultString(value, "").toLowerCase();
    }

    private String defaultString(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }
}
