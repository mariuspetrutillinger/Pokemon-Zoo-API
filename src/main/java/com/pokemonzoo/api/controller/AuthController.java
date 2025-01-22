package com.pokemonzoo.api.controller;

import com.pokemonzoo.api.dto.AuthResponse;
import com.pokemonzoo.api.dto.LoginRequest;
import com.pokemonzoo.api.dto.RegisterRequest;
import com.pokemonzoo.api.model.AppClient;
import com.pokemonzoo.api.service.AppClientService;
import com.pokemonzoo.api.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Tag(name = "Auth", description = "Endpoints for user authentication")
public class AuthController {
    private final AppClientService appClientService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "Login to the application",
            description = "Login to the application and recieve the authentication token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Failed to login"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AppClient authenticatedAppClient = appClientService.getUser(request);

        String token = jwtUtil.generateToken(authenticatedAppClient.getUsername());

        return ResponseEntity.ok(new AuthResponse("Bearer " + token));
    }

    @PostMapping("/register")
    @Operation(summary = "Register",
            description = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to register user"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> register(@RequestBody @NotNull RegisterRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }
        AppClient appClient = new AppClient();
        appClient.setUsername(request.getUsername());
        appClient.setPassword(passwordEncoder.encode(request.getPassword()));

        appClientService.createUser(appClient);

        return ResponseEntity.ok("User registered successfully");
    }
}
