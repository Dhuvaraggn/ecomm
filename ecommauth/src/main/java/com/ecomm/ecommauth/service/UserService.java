package com.ecomm.ecommauth.service;

import com.ecomm.ecommauth.dto.AuthResponse;
import com.ecomm.ecommauth.dto.LoginRequest;
import com.ecomm.ecommauth.dto.RegisterRequest;
import com.ecomm.ecommauth.entity.User;
import com.ecomm.ecommauth.repository.UserRepository;
import com.ecomm.ecommauth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Create new user with encoded password
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.UserRole.valueOf(request.getRole().toUpperCase()));

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole().name());

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRole().name(),
                token,
                "User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // Verify password using password encoder
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                token,
                "Login successful");
    }

    public AuthResponse validateUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                null,
                "User validated");
    }
}
