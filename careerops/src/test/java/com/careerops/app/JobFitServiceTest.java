package com.careerops.app;

import com.careerops.app.dto.fit.FitResponse;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;
import com.careerops.app.entity.Skill;
import com.careerops.app.service.JobFitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class JobFitServiceTest {

    @Autowired
    private JobFitService jobFitService;

    @Test
    @DisplayName("Calculates explainable job-fit score and identifies matched vs missing skills")
    void testJobFitCalculation() {
        Skill javaSkill = new Skill("Java", "Backend");
        Skill springSkill = new Skill("Spring Boot", "Backend");
        Skill dockerSkill = new Skill("Docker", "DevOps");

        Candidate candidate = new Candidate("Alice Dev", "alice@example.com", "hash");
        candidate.setSkills(Set.of(javaSkill, springSkill));
        candidate.setExperienceYears(3);
        candidate.setEducation("Bachelor of Technology in Computer Science");
        candidate.setResumeText("Experienced Java developer building microservices with Spring Boot and MySQL.");

        Opportunity opportunity = new Opportunity(
                "Java Software Engineer",
                "Apex Cloud",
                OpportunityType.FULL_TIME,
                "Software Engineering",
                "Looking for a software engineer with 2+ years of experience in Java, Spring Boot, and Docker."
        );
        opportunity.setRequiredSkills(Set.of(javaSkill, springSkill, dockerSkill));

        FitResponse fit = jobFitService.analyze(candidate, opportunity);

        assertNotNull(fit);
        assertTrue(fit.matchScore() >= 0 && fit.matchScore() <= 100, "Score should be between 0 and 100");
        assertNotNull(fit.confidence());
        assertNotNull(fit.disclaimer());
        assertFalse(fit.disclaimer().isEmpty());

        // Check matched skills
        assertTrue(fit.matchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("Java")));
        assertTrue(fit.matchedSkills().stream().anyMatch(s -> s.equalsIgnoreCase("Spring Boot")));

        // Check missing skills
        assertTrue(fit.missingSkills().stream().anyMatch(s -> s.equalsIgnoreCase("Docker")));

        // Check eligibility checks exist
        assertFalse(fit.eligibility().isEmpty());
    }
}
