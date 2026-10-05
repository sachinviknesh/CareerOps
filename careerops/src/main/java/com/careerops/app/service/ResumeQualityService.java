package com.careerops.app.service;

import com.careerops.app.dto.fit.ResumeAnalysisResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Offline resume quality analysis — no AI required.
 * Inspects extracted resume text for structure, evidence, and content signals.
 */
@Service
public class ResumeQualityService {

    private static final int MIN_MEANINGFUL_WORDS = 100;

    private static final Map<String, List<String>> SECTION_KEYWORDS = Map.of(
            "Summary/Profile",      List.of("summary", "profile", "objective", "about"),
            "Education",            List.of("education", "degree", "university", "college", "b.tech", "b.e", "bachelor", "master", "gpa", "cgpa"),
            "Experience",           List.of("experience", "worked", "employment", "intern", "developer", "engineer"),
            "Projects",             List.of("project", "built", "developed", "implemented", "created"),
            "Skills",               List.of("skills", "technologies", "tools", "proficient", "languages"),
            "Certifications",       List.of("certification", "certified", "certificate", "aws certified", "oracle"),
            "Achievements",         List.of("achievement", "award", "winner", "rank", "honor", "distinction")
    );

    private static final Pattern METRIC_PATTERN = Pattern.compile(
            "\\b\\d+[%+]?\\s*(percent|%|users|requests|ms|seconds|minutes|hours|days|kb|mb|gb|lines|commits)?\\b",
            Pattern.CASE_INSENSITIVE);

    private static final List<String> TECH_TERMS = List.of(
            "java", "python", "javascript", "typescript", "react", "spring", "node", "express",
            "sql", "mysql", "postgresql", "mongodb", "redis", "docker", "kubernetes", "aws",
            "azure", "gcp", "git", "linux", "html", "css", "rest", "api", "microservice",
            "machine learning", "deep learning", "tensorflow", "pytorch", "pandas", "numpy",
            "jenkins", "ci/cd", "maven", "gradle", "hibernate", "jpa", "hibernate"
    );

    public ResumeAnalysisResponse analyze(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            return new ResumeAnalysisResponse(false, 0, Collections.emptyMap(),
                    List.of(), List.of("No resume text found. Please upload your resume."),
                    List.of(), List.of("Upload a text-extractable PDF or DOCX resume."));
        }

        String lowerText = resumeText.toLowerCase();
        String[] words = resumeText.split("\\s+");
        int wordCount = words.length;

        // Section detection
        Map<String, Boolean> sections = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : SECTION_KEYWORDS.entrySet()) {
            boolean found = entry.getValue().stream().anyMatch(lowerText::contains);
            sections.put(entry.getKey(), found);
        }

        List<String> strengths = new ArrayList<>();
        List<String> gaps = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        // Word count check
        if (wordCount < MIN_MEANINGFUL_WORDS) {
            gaps.add("Very little text extracted (" + wordCount + " words). The file may be image-based or poorly formatted.");
            suggestions.add("Use a text-based PDF or DOCX. Avoid scanning or image-based PDFs.");
        } else {
            strengths.add("Resume contains sufficient text content (" + wordCount + " words).");
        }

        // Section-by-section feedback
        for (Map.Entry<String, Boolean> entry : sections.entrySet()) {
            if (entry.getValue()) {
                strengths.add(entry.getKey() + " section detected.");
            } else if (entry.getKey().equals("Education") || entry.getKey().equals("Skills") || entry.getKey().equals("Experience") || entry.getKey().equals("Projects")) {
                gaps.add(entry.getKey() + " section not clearly detected.");
                suggestions.add("Add a clearly labelled '" + entry.getKey() + "' section.");
            }
        }

        // Metrics check
        var metricMatcher = METRIC_PATTERN.matcher(resumeText);
        int metricCount = 0;
        while (metricMatcher.find()) metricCount++;
        if (metricCount > 0) {
            strengths.add("Quantified evidence found (" + metricCount + " metric mentions detected).");
        } else {
            gaps.add("No measurable outcomes detected. Consider adding numbers, percentages, or scale indicators.");
            suggestions.add("Where truthful, describe outcomes with measurable indicators (e.g., 'reduced load time by 30%').");
        }

        // Technical terms found
        List<String> techFound = TECH_TERMS.stream()
                .filter(lowerText::contains)
                .toList();

        if (!techFound.isEmpty()) {
            strengths.add("Technical terminology present: " + String.join(", ", techFound.subList(0, Math.min(techFound.size(), 8))));
        } else {
            gaps.add("No common technical terms detected.");
            suggestions.add("Ensure technical skills and tools are explicitly listed.");
        }

        return new ResumeAnalysisResponse(true, wordCount, sections, strengths, gaps, techFound, suggestions);
    }
}
