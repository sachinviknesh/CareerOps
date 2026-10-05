package com.careerops.app.dto.fit;

import java.util.List;

public record FitResponse(
        int score,
        String confidence,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> evidenceFound,
        List<EligibilityCheck> eligibilityChecks,
        List<String> resumeSignals,
        List<String> nextActions,
        String disclaimer
) {
    public record EligibilityCheck(String criterion, String result, String detail) {}
}
