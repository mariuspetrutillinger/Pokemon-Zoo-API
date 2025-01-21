package com.pokemonzoo.api;

import com.pokemonzoo.api.controller.AuthController;
import com.pokemonzoo.api.dto.AuthResponse;
import com.pokemonzoo.api.dto.LoginRequest;
import com.pokemonzoo.api.dto.RegisterRequest;
import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.service.AppClientService;
import com.pokemonzoo.api.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AppClientService appClientService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(appClientService, passwordEncoder, jwtUtil);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testUser", "password123");
        AppClient authenticatedClient = new AppClient();
        authenticatedClient.setUsername("testUser");

        when(appClientService.getUser(loginRequest)).thenReturn(authenticatedClient);
        when(jwtUtil.generateToken("testUser")).thenReturn("test-token");

        // Act
        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bearer test-token", response.getBody().getResponse());
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnBadRequest() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("wrongUser", "wrongPass");
        String errorMessage = "Invalid credentials";

        when(appClientService.getUser(loginRequest))
                .thenThrow(new EntityNotFoundException(errorMessage));

        // Act
        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getResponse());
    }

    @Test
    void login_WithServerError_ShouldReturnInternalServerError() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("testUser", "password123");

        when(appClientService.getUser(loginRequest))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An error occurred", response.getBody().getResponse());
    }

    @Test
    void register_WithValidRequest_ShouldRegisterSuccessfully() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newUser");
        registerRequest.setPassword("password123");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        doNothing().when(appClientService).createUser(any(AppClient.class));

        // Act
        ResponseEntity<String> response = authController.register(registerRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());

        verify(passwordEncoder).encode("password123");
        verify(appClientService).createUser(any(AppClient.class));
    }

    @Test
    void register_WithExistingUsername_ShouldReturnBadRequest() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("existingUser");
        registerRequest.setPassword("password123");
        String errorMessage = "Username already exists";

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        doThrow(new IllegalArgumentException(errorMessage))
                .when(appClientService).createUser(any(AppClient.class));

        // Act
        ResponseEntity<String> response = authController.register(registerRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void register_WithServerError_ShouldReturnInternalServerError() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newUser");
        registerRequest.setPassword("password123");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        doThrow(new RuntimeException("Unexpected error"))
                .when(appClientService).createUser(any(AppClient.class));

        // Act
        ResponseEntity<String> response = authController.register(registerRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred", response.getBody());
    }

    @Test
    void register_WithNullUsername_ShouldReturnBadRequest() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setPassword("password123");

        // Act
        ResponseEntity<String> response = authController.register(registerRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username and password are required", response.getBody());
    }

    @Test
    void login_WithNullRequest_ShouldReturnInternalServerError() {
        // Act
        ResponseEntity<AuthResponse> response = authController.login(null);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An error occurred", response.getBody().getResponse());
    }

    @Test
    void register_ShouldEncodePassword() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newUser");
        registerRequest.setPassword("password123");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        doNothing().when(appClientService).createUser(any(AppClient.class));

        // Act
        authController.register(registerRequest);

        // Assert
        verify(passwordEncoder).encode("password123");
    }
}