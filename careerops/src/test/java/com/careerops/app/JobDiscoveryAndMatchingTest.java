package com.careerops.app;

import com.careerops.app.dto.auth.RegisterRequest;
import com.careerops.app.dto.match.ExternalJobDto;
import com.careerops.app.dto.match.JobImportResponse;
import com.careerops.app.dto.match.JobMatchResponse;
import com.careerops.app.dto.match.JobSearchCriteria;
import com.careerops.app.entity.*;
import com.careerops.app.repository.CandidateRepository;
import com.careerops.app.repository.OpportunityRepository;
import com.careerops.app.repository.SkillRepository;
import com.careerops.app.service.JobImportService;
import com.careerops.app.service.JobMatchingService;
import com.careerops.app.service.jobsource.MockJobSourceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JobDiscoveryAndMatchingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockJobSourceProvider mockJobSourceProvider;

    @Autowired
    private JobImportService jobImportService;

    @Autowired
    private JobMatchingService jobMatchingService;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private SkillRepository skillRepository;

    private Candidate testCandidate;

    @BeforeEach
    void setUp() {
        testCandidate = candidateRepository.findByEmailIgnoreCase("discovery.test@example.com").orElseGet(() -> {
            Candidate c = new Candidate("Discovery Candidate", "discovery.test@example.com", "PasswordHash123!");
            c.setTargetRoles("SOC Analyst");
            c.setExperienceYears(2);
            c.setLocation("San Francisco, CA");
            return candidateRepository.save(c);
        });
    }

    @Test
    @DisplayName("Job Normalization: Correctly normalizes raw external job DTO to standard Opportunity")
    void testJobNormalization() {
        ExternalJobDto rawDto = new ExternalJobDto(
                "MOCK_PROVIDER",
                "EXT-999",
                "  Senior Cloud Security Engineer  ",
                "CyberGuard Inc",
                "San Francisco, CA",
                "Remote",
                "JOB",
                "Lead cloud security architecture across AWS and Kubernetes environments.",
                "https://example.com/jobs/999",
                LocalDate.now(),
                LocalDate.now().plusDays(25),
                List.of("AWS", "Kubernetes", "SIEM")
        );

        Opportunity normalized = mockJobSourceProvider.normalizeJob(rawDto);

        assertNotNull(normalized);
        assertEquals("Senior Cloud Security Engineer", normalized.getTitle());
        assertEquals("CyberGuard Inc", normalized.getOrganization());
        assertEquals(MockJobSourceProvider.SOURCE_CODE, normalized.getSource());
        assertEquals("EXT-999", normalized.getExternalJobId());
        assertEquals("senior cloud security engineer", normalized.getNormalizedTitle());
        assertEquals(OpportunityType.JOB, normalized.getType());
        assertEquals("Remote", normalized.getWorkMode());
        assertEquals("San Francisco, CA", normalized.getLocation());
        assertTrue(normalized.isActive());
        assertEquals(LocalDate.now().plusDays(25), normalized.getDeadline());
    }

    @Test
    @Transactional
    @DisplayName("Duplicate Detection: Prevents inserting opportunities with duplicate source/externalId or normalized title/org")
    void testDuplicateDetection() {
        Opportunity opp = new Opportunity(
                "Penetration Tester",
                "Offensive Labs",
                OpportunityType.JOB,
                "Cybersecurity",
                "Hands on penetration testing and red teaming."
        );
        opp.setSource("EXTERNAL_SOURCE");
        opp.setExternalJobId("LABS-101");
        opp.setNormalizedTitle("penetration tester");
        opportunityRepository.save(opp);

        // Check source + externalId duplicate
        boolean sourceDup = opportunityRepository.existsBySourceAndExternalJobId("EXTERNAL_SOURCE", "LABS-101");
        assertTrue(sourceDup, "Should detect duplicate by source and external ID");

        // Check normalized title + organization duplicate (case-insensitive)
        boolean titleOrgDup = opportunityRepository.existsByNormalizedTitleAndOrganizationIgnoreCase("penetration tester", "offensive labs");
        assertTrue(titleOrgDup, "Should detect duplicate by normalized title and organization");

        // Non-duplicate checks
        assertFalse(opportunityRepository.existsBySourceAndExternalJobId("EXTERNAL_SOURCE", "LABS-999"));
        assertFalse(opportunityRepository.existsByNormalizedTitleAndOrganizationIgnoreCase("software engineer", "offensive labs"));
    }

    @Test
    @Transactional
    @DisplayName("Job Importing: Imports opportunities from provider and prevents duplicate inserts on subsequent runs")
    void testJobImportingAndDeduplication() {
        // Initial import using defaults (retrieves all mock opportunities)
        JobImportResponse firstRun = jobImportService.importJobs(testCandidate, JobSearchCriteria.defaults());
        assertNotNull(firstRun);
        assertTrue(firstRun.totalFetched() > 0, "Should have fetched jobs from mock provider");
        assertTrue(firstRun.importedCount() > 0, "Should have imported new opportunities");
        assertEquals(0, firstRun.duplicateCount(), "First run should have 0 duplicates");
        assertTrue(firstRun.matchedCount() > 0, "Should compute matches for candidate");

        // Subsequent import of the same source
        JobImportResponse secondRun = jobImportService.importJobs(testCandidate, JobSearchCriteria.defaults());
        assertNotNull(secondRun);
        assertEquals(0, secondRun.importedCount(), "Second run should import 0 new opportunities");
        assertTrue(secondRun.duplicateCount() > 0, "Second run should skip all previously imported duplicates");
    }

    @Test
    @Transactional
    @DisplayName("Fit-score calculation and missing skill detection against candidate profile")
    void testFitScoreAndMissingSkillDetection() {
        Skill siemSkill = skillRepository.findByNameIgnoreCase("SIEM").orElseGet(() -> skillRepository.save(new Skill("SIEM", "Security")));
        Skill networkSkill = skillRepository.findByNameIgnoreCase("Network Security").orElseGet(() -> skillRepository.save(new Skill("Network Security", "Security")));
        Skill pythonSkill = skillRepository.findByNameIgnoreCase("Python").orElseGet(() -> skillRepository.save(new Skill("Python", "Programming")));

        testCandidate.setSkills(new java.util.HashSet<>(Set.of(siemSkill, networkSkill)));
        candidateRepository.save(testCandidate);

        Opportunity opp = new Opportunity(
                "SOC Analyst Tier 1",
                "Defense Dynamics",
                OpportunityType.JOB,
                "Cybersecurity",
                "Monitor and investigate SIEM security alerts and network traffic."
        );
        opp.setRequiredSkills(new java.util.HashSet<>(Set.of(siemSkill, networkSkill, pythonSkill)));
        opp = opportunityRepository.save(opp);

        JobMatch savedMatch = jobMatchingService.computeAndSaveMatch(testCandidate, opp);
        JobMatchResponse match = jobMatchingService.toResponse(savedMatch);

        assertNotNull(match);
        assertTrue(match.fitScore() > 0 && match.fitScore() <= 100, "Fit score must be 0-100");
        assertNotNull(match.matchedSkills());
        assertNotNull(match.missingSkills());

        // SIEM and Network Security should be matched
        assertTrue(match.matchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("SIEM")));
        assertTrue(match.matchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("Network Security")));

        // Python was not in candidate's skills, should be flagged in missingSkills
        assertTrue(match.missingSkills().stream().anyMatch(s -> s.equalsIgnoreCase("Python")));

        // Preparation recommendations generated
        assertNotNull(match.recommendations());
        assertFalse(match.recommendations().isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Match Ranking: Returned matches are ranked by descending fit score")
    void testMatchRanking() {
        jobImportService.importJobs(testCandidate, JobSearchCriteria.defaults());

        List<JobMatchResponse> topMatches = jobMatchingService.getTopMatches(testCandidate.getId(), 5);
        assertNotNull(topMatches);
        assertFalse(topMatches.isEmpty());
        assertTrue(topMatches.size() <= 5);

        // Verify descending order
        for (int i = 0; i < topMatches.size() - 1; i++) {
            assertTrue(
                    topMatches.get(i).fitScore() >= topMatches.get(i + 1).fitScore(),
                    "Matches must be ordered by descending fit score"
            );
        }
    }

    @Test
    @DisplayName("API Authorization: Enforces authentication on match and discovery endpoints")
    void testMatchApiAuthorization() throws Exception {
        // Without token -> 403 Forbidden
        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/matches/top"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/opportunities/import"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/matches/recalculate"))
                .andExpect(status().isForbidden());

        // With registered candidate token -> 200 OK
        RegisterRequest registerReq = new RegisterRequest("Match Auth User", "match.auth@example.com", "Password123!");
        String regResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(regResponse).get("token").asText();

        // 1. Import jobs
        mockMvc.perform(post("/api/opportunities/import")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFetched").isNumber())
                .andExpect(jsonPath("$.importedCount").isNumber());

        // 2. Query matches
        mockMvc.perform(get("/api/matches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // 3. Query top matches
        mockMvc.perform(get("/api/matches/top?limit=3")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 4. Recalculate matches
        mockMvc.perform(post("/api/matches/recalculate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
