package com.careerops.app.dto.fit;

import java.util.List;
import java.util.Map;

public record ResumeAnalysisResponse(
        boolean hasText,
        int wordCount,
        Map<String, Boolean> sections,
        List<String> strengths,
        List<String> gaps,
        List<String> technicalTermsFound,
        List<String> suggestions
) {}
