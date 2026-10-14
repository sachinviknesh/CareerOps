package com.careerops.app.dto.auth;

public record LoginResponse(
        String token,
        long expiresInSeconds,
        String email,
        String tokenType
) {
    public LoginResponse(String token, long expiresInSeconds, String email) {
        this(token, expiresInSeconds, email, "Bearer");
    }
}
