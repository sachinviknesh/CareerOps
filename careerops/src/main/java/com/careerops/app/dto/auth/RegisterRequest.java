package com.careerops.app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank @Email(message = "Valid email required") String email,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password
) {}
