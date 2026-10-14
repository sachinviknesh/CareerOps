package com.careerops.app.dto.candidate;

import com.careerops.app.dto.skill.SkillResponse;
import java.time.LocalDateTime;
import java.util.Set;

public record CandidateResponse(
        Long id,
        String name,
        String email,
        String headline,
        String location,
        String education,
        Integer experienceYears,
        String targetRoles,
        String resumeFileName,
        boolean hasResume,
        Set<SkillResponse> skills,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String degree,
        String universityOrCollege,
        Integer graduationYear,
        String projects,
        String certifications,
        String preferredOpportunityTypes,
        String preferredStates,
        String preferredCities,
        String preferredWorkMode,
        boolean willingToTravel,
        Integer maxTravelDistanceKm,
        String careerInterests,
        String currentCity,
        String currentState,
        String currentCountry
) {
    public CandidateResponse(
            Long id,
            String name,
            String email,
            String headline,
            String location,
            String education,
            Integer experienceYears,
            String targetRoles,
            String resumeFileName,
            boolean hasResume,
            Set<SkillResponse> skills,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this(id, name, email, headline, location, education, experienceYears, targetRoles,
                resumeFileName, hasResume, skills, createdAt, updatedAt,
                null, null, null, null, null, null, null, null, "ANY", true, 300, null, null, null, "India");
    }
}
