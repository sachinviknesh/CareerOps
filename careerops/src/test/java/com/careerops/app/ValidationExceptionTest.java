package com.careerops.app;

import com.careerops.app.dto.auth.RegisterRequest;
import com.careerops.app.dto.opportunity.OpportunityRequest;
import com.careerops.app.entity.Candidate;
import com.careerops.app.repository.CandidateRepository;
import com.careerops.app.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ValidationExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String authToken;

    @BeforeEach
    void setup() {
        String email = "val.tester@example.com";
        if (!candidateRepository.existsByEmailIgnoreCase(email)) {
            Candidate candidate = new Candidate("Val Tester", email, passwordEncoder.encode("Password123!"));
            candidateRepository.save(candidate);
        }
        authToken = "Bearer " + jwtService.generate(email);
    }

    @Test
    @DisplayName("Validation fails when required fields are missing on registration")
    void testRegistrationValidationFailure() throws Exception {
        // Invalid email and empty name
        RegisterRequest invalidReq = new RegisterRequest("", "not-an-email", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    @DisplayName("Validation fails when opportunity title or type is null")
    void testOpportunityValidationFailure() throws Exception {
        OpportunityRequest invalidOpp = new OpportunityRequest(
                "",
                "Company",
                null,
                "Family",
                "Desc",
                "Location",
                "REMOTE",
                "https://example.com",
                null,
                null,
                true,
                null
        );

        mockMvc.perform(post("/api/opportunities")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOpp)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.type").exists());
    }
}
