package com.iris.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iris.backend.dto.auth.LoginRequestDTO;
import com.iris.backend.dto.auth.LoginResponseDTO;
import com.iris.backend.dto.auth.RegisterRequestDTO;
import com.iris.backend.dto.user.UserResponseDTO;
import com.iris.backend.service.auth.contract.AuthService;
import com.iris.backend.service.user.contract.UserService;
import com.iris.backend.util.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /** POST /api/auth/register — create a new account. */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ApiResponse.success(HttpStatus.CREATED, "Registered successfully", authService.register(request));
    }

    /** POST /api/auth/login — authenticate and receive a JWT token. */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    /** GET /api/auth/me — return the currently authenticated user (requires Bearer token). */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> me() {
        return ApiResponse.success(userService.getCurrentUser());
    }
}
