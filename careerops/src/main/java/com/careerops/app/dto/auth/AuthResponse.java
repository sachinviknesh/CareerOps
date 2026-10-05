package com.careerops.app.dto.auth;

public record AuthResponse(String token, long expiresIn) {}
