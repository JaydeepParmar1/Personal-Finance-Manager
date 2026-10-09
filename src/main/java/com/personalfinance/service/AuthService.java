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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for user registration, login, logout and authentication context access.
 *
 * <p>Passwords are stored BCrypt-hashed. Login establishes a session-based
 * authentication via the Spring Security context repository, and logout
 * invalidates the HTTP session.</p>
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    /**
     * Registers a new user with a BCrypt-hashed password.
     *
     * @param request the registration details
     * @return a confirmation message and the new user id
     * @throws ConflictException if a user with the same username (email) already exists
     */
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("User with username " + request.getUsername() + " already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .message("User registered successfully")
                .userId(savedUser.getId())
                .build();
    }

    /**
     * Authenticates a user and stores the security context in the HTTP session.
     *
     * @param request      the login credentials
     * @param httpRequest  the servlet request used to store the session
     * @param httpResponse the servlet response used to store the session cookie
     * @return a confirmation message
     */
    public MessageResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

        return MessageResponse.builder()
                .message("Login successful")
                .build();
    }

    /**
     * Invalidates the current HTTP session and clears the security context.
     *
     * @param httpRequest  the servlet request whose session is invalidated
     * @param httpResponse the servlet response
     * @return a confirmation message
     */
    public MessageResponse logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return MessageResponse.builder()
                .message("Logout successful")
                .build();
    }

    /**
     * Returns the currently authenticated user.
     *
     * @return the authenticated user entity
     * @throws ResourceNotFoundException if no authenticated user is present
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResourceNotFoundException("Authenticated user context not found");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
