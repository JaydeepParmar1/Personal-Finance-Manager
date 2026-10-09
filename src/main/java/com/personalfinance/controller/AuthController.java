package com.personalfinance.controller;

import com.personalfinance.dto.LoginRequest;
import com.personalfinance.dto.MessageResponse;
import com.personalfinance.dto.RegisterRequest;
import com.personalfinance.dto.RegisterResponse;
import com.personalfinance.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user registration, login and logout.
 *
 * <p>Registration and login are publicly accessible; all other API endpoints
 * require an authenticated session. Login and logout manage the HTTP session
 * (JSESSIONID cookie) used for session-based authentication.</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account.
     *
     * @param request the registration details (email, password, full name, phone number)
     * @return a confirmation message and the new user id with HTTP 201
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and establishes an HTTP session.
     *
     * @param request      the login credentials (username/email and password)
     * @param httpRequest  the servlet request used to store the session cookie
     * @param httpResponse the servlet response used to store the session cookie
     * @return a confirmation message with HTTP 200
     */
    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {
        MessageResponse response = authService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Invalidates the current HTTP session and logs the user out.
     *
     * @param httpRequest  the servlet request whose session is invalidated
     * @param httpResponse the servlet response
     * @return a confirmation message with HTTP 200
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {
        MessageResponse response = authService.logout(httpRequest, httpResponse);
        return ResponseEntity.ok(response);
    }
}
