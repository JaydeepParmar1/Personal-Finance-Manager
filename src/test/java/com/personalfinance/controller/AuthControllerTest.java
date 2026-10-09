package com.personalfinance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalfinance.dto.LoginRequest;
import com.personalfinance.dto.MessageResponse;
import com.personalfinance.dto.RegisterRequest;
import com.personalfinance.dto.RegisterResponse;
import com.personalfinance.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("user@example.com")
                .password("password123")
                .fullName("John Doe")
                .phoneNumber("+1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .username("user@example.com")
                .password("password123")
                .build();
    }

    @Test
    void register_Success() throws Exception {
        RegisterResponse response = RegisterResponse.builder()
                .message("User registered successfully")
                .userId(1L)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void register_WeakPassword_BadRequest() throws Exception {
        registerRequest = RegisterRequest.builder()
                .username("user@example.com")
                .password("short")
                .fullName("John Doe")
                .phoneNumber("+1234567890")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_InvalidPhone_BadRequest() throws Exception {
        registerRequest = RegisterRequest.builder()
                .username("user@example.com")
                .password("password123")
                .fullName("John Doe")
                .phoneNumber("abc")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        MessageResponse response = MessageResponse.builder()
                .message("Login successful")
                .build();

        when(authService.login(any(LoginRequest.class), any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    void logout_Success() throws Exception {
        MessageResponse response = MessageResponse.builder()
                .message("Logout successful")
                .build();

        when(authService.logout(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }
}
