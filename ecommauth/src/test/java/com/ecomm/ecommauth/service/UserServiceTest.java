package com.ecomm.ecommauth.service;

import com.ecomm.ecommauth.dto.AuthResponse;
import com.ecomm.ecommauth.dto.LoginRequest;
import com.ecomm.ecommauth.dto.RegisterRequest;
import com.ecomm.ecommauth.entity.User;
import com.ecomm.ecommauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private com.ecomm.ecommauth.security.JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "password", "BUYER");
        loginRequest = new LoginRequest("testuser", "password");
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(User.UserRole.BUYER);
    }

    @Test
    void testRegister_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(jwtUtil.generateToken("testuser", "BUYER")).thenReturn("token");
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("BUYER", response.getRole());
        assertEquals("token", response.getToken());
        assertEquals("User registered successfully", response.getMessage());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password");
        verify(jwtUtil).generateToken("testuser", "BUYER");
    }

    @Test
    void testRegister_UsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(registerRequest));
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "BUYER")).thenReturn("token");

        AuthResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("BUYER", response.getRole());
        assertEquals("token", response.getToken());
        assertEquals("Login successful", response.getMessage());
        verify(passwordEncoder).matches("password", "encodedPassword");
        verify(jwtUtil).generateToken("testuser", "BUYER");
    }

    @Test
    void testLogin_InvalidUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.login(loginRequest));
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void testLogin_InvalidPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.login(loginRequest));
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void testValidateUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        AuthResponse response = userService.validateUser("testuser");

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("BUYER", response.getRole());
        assertEquals("User validated", response.getMessage());
    }

    @Test
    void testValidateUser_NotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.validateUser("testuser"));
        assertEquals("User not found", exception.getMessage());
    }
}