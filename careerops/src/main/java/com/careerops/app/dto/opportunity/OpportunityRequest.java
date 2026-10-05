package com.careerops.app.dto.opportunity;

import com.careerops.app.entity.OpportunityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record OpportunityRequest(
        @NotBlank(message = "Title is required") String title,
        String organization,
        @NotNull(message = "Type is required") OpportunityType type,
        String roleFamily,
        @Size(max = 10000, message = "Description must be under 10000 characters") String description,
        String location,
        String workMode,
        String url,
        LocalDate deadline,
        LocalDate postedAt,
        Boolean active,
        Set<Long> requiredSkillIds
) {}
