package com.ecomm.ecommauth.controller;

import com.ecomm.ecommauth.dto.AuthResponse;
import com.ecomm.ecommauth.dto.LoginRequest;
import com.ecomm.ecommauth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.ecomm.ecommauth.service.UserService userService;

    @MockBean
    private com.ecomm.ecommauth.security.JwtUtil jwtUtil;

    @MockBean
    private com.ecomm.ecommauth.repository.UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "password", "BUYER");
        AuthResponse response = new AuthResponse(1L, "testuser", "BUYER", null, "User registered successfully");

        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("BUYER"))
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void testRegister_BadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "password", "BUYER");

        when(userService.register(any(RegisterRequest.class))).thenThrow(new RuntimeException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password");
        AuthResponse response = new AuthResponse(1L, "testuser", "BUYER", "token", "Login successful");

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("BUYER"))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void testLogin_Unauthorized() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password");

        when(userService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void testValidateUser_Success() throws Exception {
        AuthResponse response = new AuthResponse(1L, "testuser", "BUYER", null, "User validated");

        when(userService.validateUser("testuser")).thenReturn(response);

        mockMvc.perform(get("/api/auth/validate/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("BUYER"))
                .andExpect(jsonPath("$.message").value("User validated"));
    }

    @Test
    void testValidateUser_NotFound() throws Exception {
        when(userService.validateUser("testuser")).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/auth/validate/testuser"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}