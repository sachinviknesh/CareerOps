package com.careerops.app;

import com.careerops.app.dto.auth.LoginRequest;
import com.careerops.app.dto.auth.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Registration succeeds and returns JWT token")
    void testRegistrationSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("Test Candidate", "candidate.test1@example.com", "Password123!");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    @DisplayName("Registration fails with 409 Conflict when email is already registered")
    void testDuplicateRegistrationReturnsConflict() throws Exception {
        RegisterRequest initial = new RegisterRequest("First User", "duplicate@example.com", "Password123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initial)))
                .andExpect(status().isCreated());

        RegisterRequest duplicate = new RegisterRequest("Second User", "duplicate@example.com", "DifferentPass123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("An account with this email already exists."));
    }

    @Test
    @DisplayName("Login succeeds with valid credentials and returns JWT")
    void testLoginSuccess() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("Login User", "candidate.login@example.com", "Password123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("candidate.login@example.com", "Password123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    @DisplayName("Login fails with invalid password")
    void testLoginFailureInvalidPassword() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("Bad Auth User", "candidate.badpass@example.com", "Password123!");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("candidate.badpass@example.com", "WrongPassword999!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    @DisplayName("Login fails for non-existent candidate")
    void testLoginFailureNonExistent() throws Exception {
        LoginRequest loginReq = new LoginRequest("nonexistent@example.com", "Password123!");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    @Test
    @DisplayName("GET / returns status UP and application CareerOps without authentication")
    void testRootEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("CareerOps"));
    }

    @Test
    @DisplayName("Protected endpoint returns 403 Forbidden when unauthenticated")
    void testProtectedEndpointWithoutAuthReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/candidates/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Protected endpoint succeeds when authenticated with valid JWT")
    void testProtectedEndpointWithValidAuth() throws Exception {
        RegisterRequest registerReq = new RegisterRequest("Auth User", "candidate.protected@example.com", "Password123!");
        String responseJson = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(responseJson).get("token").asText();

        mockMvc.perform(get("/api/candidates/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("candidate.protected@example.com"));
    }
}
