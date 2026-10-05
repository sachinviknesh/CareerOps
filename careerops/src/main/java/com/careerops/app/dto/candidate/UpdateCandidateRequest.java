package com.careerops.app.dto.candidate;

import java.util.Set;

public record UpdateCandidateRequest(
        String name,
        String headline,
        String location,
        String education,
        Integer experienceYears,
        String targetRoles,
        Set<Long> skillIds
) {}
