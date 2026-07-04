package com.iris.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iris.backend.dto.user.UpdateUserRequestDTO;
import com.iris.backend.dto.user.UserResponseDTO;
import com.iris.backend.service.user.contract.UserService;
import com.iris.backend.util.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/users — return all users ordered by email. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAll() {
        return ApiResponse.success(userService.getAll());
    }

    /** PUT /api/users/me — update the currently authenticated user's profile. */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateMe(@Valid @RequestBody UpdateUserRequestDTO request) {
        return ApiResponse.success("Updated successfully", userService.update(request));
    }
}
