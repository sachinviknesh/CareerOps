package com.careerops.app.controller;

import com.careerops.app.dto.auth.UserResponse;
import com.careerops.app.entity.User;
import com.careerops.app.exception.ResourceNotFoundException;
import com.careerops.app.repository.UserRepository;
import com.careerops.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management and profile queries")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List all users", description = "Retrieves user accounts (passwords excluded)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves user account by primary key ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.findByEmail(authentication.getName()));
    }
}
