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
        LocalDateTime updatedAt,
        String city,
        String state,
        String country,
        String venue,
        String eligibility,
        String preferredSkills,
        String experienceRequirement,
        String educationRequirement,
        LocalDate registrationDeadline,
        LocalDate eventStartDate,
        LocalDate eventEndDate,
        String source,
        String externalId,
        String status,
        String actionLabel,
        String travelBadge
) {
    public OpportunityResponse(
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
    ) {
        this(id, title, organization, type, roleFamily, description, location, workMode, url, deadline, postedAt, active,
                requiredSkills, createdAt, updatedAt, null, null, "India", null, null, null, null, null, null, null, null,
                null, null, "OPEN", type != null ? type.getActionLabel() : "Apply Now", null);
    }
}
