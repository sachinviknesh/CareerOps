package com.careerops.app;

import com.careerops.app.dto.application.ApplicationRequest;
import com.careerops.app.entity.ApplicationStatus;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;
import com.careerops.app.repository.CandidateRepository;
import com.careerops.app.repository.OpportunityRepository;
import com.careerops.app.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApplicationCrudTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String authToken;
    private Opportunity savedOpp;

    @BeforeEach
    void setup() {
        String email = "app.tester@example.com";
        if (!candidateRepository.existsByEmailIgnoreCase(email)) {
            Candidate candidate = new Candidate("App Tester", email, passwordEncoder.encode("Password123!"));
            candidateRepository.save(candidate);
        }
        authToken = "Bearer " + jwtService.generate(email);

        savedOpp = new Opportunity(
                "Data Engineer",
                "DataFlow Inc",
                OpportunityType.JOB,
                "Data Engineering",
                "Pipeline architecture and SQL data warehousing."
        );
        savedOpp = opportunityRepository.save(savedOpp);
    }

    @Test
    @DisplayName("Application tracking CRUD and duplicate prevention test")
    void testApplicationCrudAndDuplicateCheck() throws Exception {
        // 1. CREATE APPLICATION
        ApplicationRequest createReq = new ApplicationRequest(
                savedOpp.getId(),
                ApplicationStatus.APPLIED,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                "Applied through referral."
        );

        MvcResult createResult = mockMvc.perform(post("/api/applications")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.notes").value("Applied through referral."))
                .andReturn();

        JsonNode json = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long appId = json.get("id").asLong();

        // 2. DUPLICATE CHECK - Attempting to track the same opportunity again must return 409 CONFLICT
        mockMvc.perform(post("/api/applications")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isConflict());

        // 3. READ APPLICATION BY ID
        mockMvc.perform(get("/api/applications/" + appId)
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appId))
                .andExpect(jsonPath("$.opportunity.id").value(savedOpp.getId()));

        // 4. UPDATE APPLICATION STATUS
        ApplicationRequest updateReq = new ApplicationRequest(
                savedOpp.getId(),
                ApplicationStatus.INTERVIEW,
                LocalDate.now(),
                LocalDate.now().plusDays(3),
                "Passed HR screening, scheduled technical round."
        );

        mockMvc.perform(put("/api/applications/" + appId)
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERVIEW"))
                .andExpect(jsonPath("$.notes").value("Passed HR screening, scheduled technical round."));

        // 5. DELETE APPLICATION
        mockMvc.perform(delete("/api/applications/" + appId)
                        .header("Authorization", authToken))
                .andExpect(status().isNoContent());

        // 6. READ AFTER DELETE returns 404
        mockMvc.perform(get("/api/applications/" + appId)
                        .header("Authorization", authToken))
                .andExpect(status().isNotFound());
    }
}
