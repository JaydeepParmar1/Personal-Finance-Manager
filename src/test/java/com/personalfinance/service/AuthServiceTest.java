package com.personalfinance.service;

import com.personalfinance.dto.LoginRequest;
import com.personalfinance.dto.MessageResponse;
import com.personalfinance.dto.RegisterRequest;
import com.personalfinance.dto.RegisterResponse;
import com.personalfinance.entity.User;
import com.personalfinance.exception.ConflictException;
import com.personalfinance.exception.ResourceNotFoundException;
import com.personalfinance.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("test@example.com")
                .password("password123")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .username("test@example.com")
                .password("password123")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("test@example.com")
                .password("encoded_password")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .build();

        SecurityContextHolder.clearContext();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        RegisterResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("User registered successfully", response.getMessage());
        assertEquals(1L, response.getUserId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_UserAlreadyExists_ThrowsConflictException() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        MessageResponse response = authService.login(loginRequest, httpRequest, httpResponse);

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_Success() {
        when(httpRequest.getSession(false)).thenReturn(session);

        MessageResponse response = authService.logout(httpRequest, httpResponse);

        assertNotNull(response);
        assertEquals("Logout successful", response.getMessage());
        verify(session, times(1)).invalidate();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_NoSession_Success() {
        when(httpRequest.getSession(false)).thenReturn(null);

        MessageResponse response = authService.logout(httpRequest, httpResponse);

        assertNotNull(response);
        assertEquals("Logout successful", response.getMessage());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void getCurrentUser_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("test@example.com");
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(testUser));

        User currentUser = authService.getCurrentUser();

        assertNotNull(currentUser);
        assertEquals("test@example.com", currentUser.getUsername());
    }

    @Test
    void getCurrentUser_Unauthenticated_ThrowsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> authService.getCurrentUser());
    }
}
