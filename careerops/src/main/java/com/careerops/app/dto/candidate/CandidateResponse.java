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
        LocalDateTime updatedAt
) {}
