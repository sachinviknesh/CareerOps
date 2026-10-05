package com.careerops.app.service;

import com.careerops.app.dto.fit.FitResponse;
import com.careerops.app.dto.fit.FitResponse.EligibilityCheck;
import com.careerops.app.dto.fit.ResumeAnalysisResponse;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.Skill;
import com.careerops.app.util.TextSimilarityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class JobFitService {

    private static final Logger log = LoggerFactory.getLogger(JobFitService.class);

    private static final String DISCLAIMER =
            "Fit is an explanatory heuristic based on the information available in your profile " +
            "and the opportunity description. It is not a hiring decision or guarantee.";

    private static final Pattern EXP_PATTERN = Pattern.compile(
            "(\\d+)\\+?\\s*(?:to\\s*\\d+)?\\s*years?\\s*(?:of)?\\s*experience",
            Pattern.CASE_INSENSITIVE);

    @Value("${app.fit.skill-weight}")
    private int skillWeight;

    @Value("${app.fit.responsibility-weight}")
    private int responsibilityWeight;

    @Value("${app.fit.experience-weight}")
    private int experienceWeight;

    @Value("${app.fit.resume-weight}")
    private int resumeWeight;

    @PostConstruct
    void validateWeights() {
        int total = skillWeight + responsibilityWeight + experienceWeight + resumeWeight;
        if (total != 100) {
            throw new IllegalStateException(
                    "Fit weights must sum to 100. Current sum: " + total);
        }
    }

    private final TextSimilarityUtil similarity;
    private final ResumeQualityService resumeQualityService;

    public JobFitService(TextSimilarityUtil similarity, ResumeQualityService resumeQualityService) {
        this.similarity = similarity;
        this.resumeQualityService = resumeQualityService;
    }

    public FitResponse analyze(Candidate candidate, Opportunity opportunity) {
        log.info("Analyzing fit for candidate {} against opportunity '{}'",
                candidate.getId(), opportunity.getTitle());

        // A. Skill match
        Set<String> candidateSkillNames = candidate.getSkills().stream()
                .map(Skill::getName).map(String::toLowerCase).map(String::trim)
                .collect(Collectors.toSet());

        Set<String> requiredSkillNames = opportunity.getRequiredSkills().stream()
                .map(Skill::getName).map(String::toLowerCase).map(String::trim)
                .collect(Collectors.toSet());

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String req : requiredSkillNames) {
            if (candidateSkillNames.contains(req)) matchedSkills.add(capitalize(req));
            else missingSkills.add(capitalize(req));
        }

        double skillScore = requiredSkillNames.isEmpty() ? 0.5
                : (double) matchedSkills.size() / requiredSkillNames.size();

        // B. Resume evidence for skills
        String resumeText = Optional.ofNullable(candidate.getResumeText()).orElse("");
        String lowerResume = resumeText.toLowerCase();
        List<String> evidenceFound = new ArrayList<>();

        for (String skill : matchedSkills) {
            if (lowerResume.contains(skill.toLowerCase())) {
                evidenceFound.add(skill + " — found in resume");
            }
        }

        // C. Description/resume similarity
        String description = Optional.ofNullable(opportunity.getDescription()).orElse("");
        double textSim = similarity.cosineSimilarity(resumeText, description);
        double responsibilityScore = Math.min(textSim * 2.0, 1.0); // scale: sim >0.5 → full score

        if (textSim > 0.2) {
            evidenceFound.add(String.format("Resume content overlaps with job description (similarity: %.0f%%)", textSim * 100));
        }

        // D. Experience comparison
        EligibilityCheck expCheck = checkExperience(candidate, description);

        // E. Education comparison
        EligibilityCheck eduCheck = checkEducation(candidate, description);

        List<EligibilityCheck> eligibility = List.of(expCheck, eduCheck);

        // Experience score
        double experienceScore = switch (expCheck.result()) {
            case "MATCH" -> 1.0;
            case "PARTIAL" -> 0.6;
            case "OVER" -> 0.9;
            default -> 0.5; // UNKNOWN → neutral
        };

        // F. Resume quality signals
        ResumeAnalysisResponse quality = resumeQualityService.analyze(resumeText);
        List<String> resumeSignals = new ArrayList<>(quality.strengths());
        resumeSignals.addAll(quality.gaps().stream().map(g -> "⚠ " + g).toList());
        double resumeScore = quality.hasText() && quality.wordCount() > 100 ? 0.7 : 0.3;
        if (!quality.strengths().isEmpty()) resumeScore = Math.min(resumeScore + 0.1 * quality.strengths().size() / 5.0, 1.0);

        // Weighted final score
        double raw = (skillScore * skillWeight
                + responsibilityScore * responsibilityWeight
                + experienceScore * experienceWeight
                + resumeScore * resumeWeight) / 100.0;

        int score = (int) Math.round(raw * 100);
        String confidence = score >= 70 ? "HIGH" : score >= 45 ? "MEDIUM" : "LOW";

        // Next actions
        List<String> nextActions = buildNextActions(missingSkills, quality, expCheck, score);

        return new FitResponse(score, confidence, matchedSkills, missingSkills,
                evidenceFound, eligibility, resumeSignals, nextActions, DISCLAIMER);
    }

    private EligibilityCheck checkExperience(Candidate candidate, String description) {
        Matcher m = EXP_PATTERN.matcher(description);
        if (!m.find()) {
            return new EligibilityCheck("Experience", "UNKNOWN",
                    "No specific experience requirement found in the description.");
        }
        int required = Integer.parseInt(m.group(1));
        int actual = Optional.ofNullable(candidate.getExperienceYears()).orElse(0);

        if (actual >= required) {
            return new EligibilityCheck("Experience", "MATCH",
                    "Required: " + required + "+ years. You have: " + actual + " years.");
        } else if (actual >= required - 1) {
            return new EligibilityCheck("Experience", "PARTIAL",
                    "Required: " + required + "+ years. You have: " + actual + " year(s). Close but below stated requirement.");
        } else {
            return new EligibilityCheck("Experience", "BELOW",
                    "Required: " + required + "+ years. You have: " + actual + " year(s). Consider applying for junior/entry-level roles.");
        }
    }

    private EligibilityCheck checkEducation(Candidate candidate, String description) {
        String lowerDesc = description.toLowerCase();
        String lowerEdu = Optional.ofNullable(candidate.getEducation()).orElse("").toLowerCase();

        boolean degreeMentioned = lowerDesc.contains("bachelor") || lowerDesc.contains("b.tech")
                || lowerDesc.contains("b.e") || lowerDesc.contains("degree");

        if (!degreeMentioned) {
            return new EligibilityCheck("Education", "UNKNOWN",
                    "No specific education requirement found in the description.");
        }
        if (lowerEdu.contains("bachelor") || lowerEdu.contains("b.tech") || lowerEdu.contains("b.e")
                || lowerEdu.contains("engineering") || lowerEdu.contains("computer science")) {
            return new EligibilityCheck("Education", "MATCH",
                    "Bachelor's/Engineering degree detected in your profile matching stated requirement.");
        }
        return new EligibilityCheck("Education", "PARTIAL",
                "Education requirement found in description; your education details may not fully match. Please verify.");
    }

    private List<String> buildNextActions(List<String> missingSkills, ResumeAnalysisResponse quality,
                                          EligibilityCheck expCheck, int score) {
        List<String> actions = new ArrayList<>();
        if (!missingSkills.isEmpty()) {
            actions.add("Learn or add evidence for missing skills: " + String.join(", ", missingSkills.subList(0, Math.min(3, missingSkills.size()))));
        }
        if (!quality.hasText() || quality.wordCount() < 100) {
            actions.add("Upload a text-extractable resume (PDF/DOCX without image-only content).");
        }
        if ("BELOW".equals(expCheck.result())) {
            actions.add("Consider filtering for entry-level or internship roles that match your experience level.");
        }
        if (score < 50) {
            actions.add("Significant skill gaps remain. Focus on the top 2–3 missing skills before applying.");
        } else if (score < 70) {
            actions.add("Tailor your resume to highlight relevant projects and technologies listed in the description.");
        } else {
            actions.add("Strong fit. Apply and prepare a cover letter emphasizing matched skills.");
        }
        return actions;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
