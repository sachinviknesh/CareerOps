package com.careerops.app.dto.match;

import com.careerops.app.entity.OpportunityType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record JobMatchResponse(
        Long matchId,
        Long opportunityId,
        String title,
        String organization,
        OpportunityType type,
        String roleFamily,
        String description,
        String location,
        String workMode,
        String url,
        LocalDate deadline,
        int fitScore,
        String confidence,
        List<String> matchedSkills,
        List<String> missingSkills,
        String experienceMatch,
        double resumeQualityScore,
        List<String> recommendations,
        LocalDateTime calculatedAt,
        Integer locationFit,
        String eligibilityStatus,
        Boolean travelRequired,
        Double distanceKm,
        String distanceLabel,
        String travelBadge,
        String estimatedTravelTime,
        String transportOptions,
        String approximateCost,
        LocalDate eventStartDate,
        LocalDate eventEndDate,
        String actionLabel
) {
    // Backward-compatible constructor
    public JobMatchResponse(
            Long matchId,
            Long opportunityId,
            String title,
            String organization,
            OpportunityType type,
            String roleFamily,
            String description,
            String location,
            String workMode,
            String url,
            LocalDate deadline,
            int fitScore,
            String confidence,
            List<String> matchedSkills,
            List<String> missingSkills,
            String experienceMatch,
            double resumeQualityScore,
            List<String> recommendations,
            LocalDateTime calculatedAt
    ) {
        this(matchId, opportunityId, title, organization, type, roleFamily, description, location, workMode, url, deadline,
                fitScore, confidence, matchedSkills, missingSkills, experienceMatch, resumeQualityScore, recommendations,
                calculatedAt, null, "ELIGIBLE", false, null, null, null, null, null, null, null, null,
                type != null ? type.getActionLabel() : "Apply Now");
    }
}
