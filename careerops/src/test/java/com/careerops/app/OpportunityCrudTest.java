package com.careerops.app;

import com.careerops.app.dto.opportunity.OpportunityRequest;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.OpportunityType;
import com.careerops.app.repository.CandidateRepository;
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
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OpportunityCrudTest {

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
        String email = "opp.tester@example.com";
        if (!candidateRepository.existsByEmailIgnoreCase(email)) {
            Candidate candidate = new Candidate("Opp Tester", email, passwordEncoder.encode("Secret123!"));
            candidateRepository.save(candidate);
        }
        authToken = "Bearer " + jwtService.generate(email);
    }

    @Test
    @DisplayName("Opportunity CRUD lifecycle test")
    void testOpportunityCrudLifecycle() throws Exception {
        // 1. CREATE
        OpportunityRequest createReq = new OpportunityRequest(
                "Fullstack Developer",
                "TechCorp",
                OpportunityType.JOB,
                "Software Engineering",
                "Building enterprise cloud applications with Java and React.",
                "Bengaluru",
                "HYBRID",
                "https://example.com/jobs/1",
                LocalDate.now().plusDays(30),
                LocalDate.now(),
                true,
                Set.of()
        );

        MvcResult createResult = mockMvc.perform(post("/api/opportunities")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Fullstack Developer"))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long oppId = createdJson.get("id").asLong();

        // 2. READ by ID
        mockMvc.perform(get("/api/opportunities/" + oppId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(oppId))
                .andExpect(jsonPath("$.title").value("Fullstack Developer"));

        // 3. UPDATE
        OpportunityRequest updateReq = new OpportunityRequest(
                "Senior Fullstack Developer",
                "TechCorp Inc",
                OpportunityType.JOB,
                "Software Engineering",
                "Updated description with lead responsibilities.",
                "Remote",
                "REMOTE",
                "https://example.com/jobs/1-updated",
                LocalDate.now().plusDays(45),
                LocalDate.now(),
                true,
                Set.of()
        );

        mockMvc.perform(put("/api/opportunities/" + oppId)
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Senior Fullstack Developer"))
                .andExpect(jsonPath("$.organization").value("TechCorp Inc"));

        // 4. PAGINATION & SORTING
        mockMvc.perform(get("/api/opportunities")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5));

        // 5. SEARCH FILTER
        mockMvc.perform(get("/api/opportunities")
                        .param("search", "Senior")
                        .param("type", "JOB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // 6. DELETE
        mockMvc.perform(delete("/api/opportunities/" + oppId)
                        .header("Authorization", authToken))
                .andExpect(status().isNoContent());

        // 7. READ after delete should return 404
        mockMvc.perform(get("/api/opportunities/" + oppId))
                .andExpect(status().isNotFound());
    }
}
