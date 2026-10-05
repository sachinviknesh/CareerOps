package com.careerops.app.dto.opportunity;

import com.careerops.app.dto.skill.SkillResponse;
import com.careerops.app.entity.OpportunityType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record OpportunityResponse(
        Long id,
        String title,
        String organization,
        OpportunityType type,
        String roleFamily,
        String description,
        String location,
        String workMode,
        String url,
        LocalDate deadline,
        LocalDate postedAt,
        boolean active,
        Set<SkillResponse> requiredSkills,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
