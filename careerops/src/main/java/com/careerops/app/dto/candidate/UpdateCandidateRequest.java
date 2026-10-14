package com.careerops.app.dto.candidate;

import java.util.Set;

public record UpdateCandidateRequest(
        String name,
        String headline,
        String location,
        String education,
        Integer experienceYears,
        String targetRoles,
        Set<Long> skillIds,
        String degree,
        String universityOrCollege,
        Integer graduationYear,
        String projects,
        String certifications,
        String preferredOpportunityTypes,
        String preferredStates,
        String preferredCities,
        String preferredWorkMode,
        Boolean willingToTravel,
        Integer maxTravelDistanceKm,
        String careerInterests,
        String currentCity,
        String currentState,
        String currentCountry
) {
    public UpdateCandidateRequest(
            String name,
            String headline,
            String location,
            String education,
            Integer experienceYears,
            String targetRoles,
            Set<Long> skillIds
    ) {
        this(name, headline, location, education, experienceYears, targetRoles, skillIds,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
