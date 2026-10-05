package com.careerops.app.dto.fit;

import java.util.List;

public record AiReviewResponse(
        String summary,
        List<String> strengths,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> evidenceGaps,
        List<String> resumeRecommendations,
        List<String> projectRecommendations,
        List<String> interviewFocus,
        List<String> nextActions
) {}
