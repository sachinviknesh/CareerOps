package com.careerops.app.service;

import com.careerops.app.dto.fit.AiReviewResponse;
import com.careerops.app.dto.fit.FitResponse;
import com.careerops.app.dto.fit.ResumeAnalysisResponse;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.Skill;
import com.careerops.app.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);

    @Value("${ai.enabled:false}")
    private boolean enabled;

    @Value("${ai.base-url:}")
    private String baseUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:}")
    private String model;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AiAnalysisService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public AiReviewResponse reviewOpportunity(Candidate candidate,
                                              Opportunity opportunity,
                                              FitResponse fitResponse,
                                              ResumeAnalysisResponse resumeQuality) {
        if (!enabled) {
            log.warn("AI review requested while AI is disabled");
            throw new BadRequestException("AI feature is optional and currently disabled in configuration (AI_ENABLED=false).");
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BadRequestException("AI base URL is not configured. Please set AI_BASE_URL.");
        }

        String targetModel = (model != null && !model.isBlank()) ? model : "gpt-3.5-turbo";
        String prompt = buildPrompt(candidate, opportunity, fitResponse, resumeQuality);

        log.info("Sending AI review request to {} with model {}", baseUrl, targetModel);

        try {
            Map<String, Object> message = Map.of("role", "user", "content", prompt);
            Map<String, Object> requestBody = Map.of(
                    "model", targetModel,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "You are an objective hiring readiness analyzer. " +
                                    "Use ONLY evidence available in the candidate data. " +
                                    "If information is missing, state clearly that it is missing. " +
                                    "Do NOT fabricate achievements, certifications, or experience. " +
                                    "Always respond with a valid, clean JSON object matching the requested schema without markdown backticks."),
                            message
                    ),
                    "temperature", 0.2
            );

            String jsonPayload = objectMapper.writeValueAsString(requestBody);

            String endpoint = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(45))
                    .header("Content-Type", "application/json");

            if (apiKey != null && !apiKey.isBlank()) {
                reqBuilder.header("Authorization", "Bearer " + apiKey);
            }

            HttpRequest request = reqBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonPayload)).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("AI API returned error status {}: {}", response.statusCode(), response.body());
                throw new BadRequestException("AI provider returned error HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();

            log.info("AI review response received successfully");
            return parseAiResponse(content);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("AI review request was interrupted", e);
            throw new BadRequestException("AI request was interrupted: " + e.getMessage());
        } catch (IOException e) {
            log.error("AI request communication failure", e);
            throw new BadRequestException("Failed to communicate with AI endpoint: " + e.getMessage());
        }
    }

    private String buildPrompt(Candidate candidate,
                               Opportunity opportunity,
                               FitResponse fit,
                               ResumeAnalysisResponse resumeQuality) {
        String candidateSkills = candidate.getSkills().stream().map(Skill::getName).collect(Collectors.joining(", "));
        String requiredSkills = opportunity.getRequiredSkills().stream().map(Skill::getName).collect(Collectors.joining(", "));

        return """
               Perform a factual, evidence-based candidate-to-opportunity fit review.
               
               CANDIDATE DATA:
               - Name: %s
               - Headline: %s
               - Experience: %s years
               - Education: %s
               - Profile Skills: %s
               - Resume Content: %s
               
               OPPORTUNITY DATA:
               - Title: %s
               - Organization: %s
               - Type: %s
               - Role Family: %s
               - Required Skills: %s
               - Description: %s
               
               OFFLINE FIT ANALYSIS:
               - Score: %d%%
               - Confidence: %s
               - Matched Skills: %s
               - Missing Skills: %s
               
               OFFLINE RESUME QUALITY:
               - Strengths: %s
               - Detected Gaps: %s
               
               RULES:
               1. Use ONLY evidence available in candidate data above.
               2. Do NOT fabricate experience, projects, metrics, or certifications.
               3. If something is missing, explicitly describe it as missing.
               4. Return a raw JSON object with the following fields:
               {
                 "summary": "1-2 sentence factual summary of alignment",
                 "strengths": ["list", "of", "demonstrated", "strengths"],
                 "matchedSkills": ["skill1", "skill2"],
                 "missingSkills": ["skill3", "skill4"],
                 "evidenceGaps": ["list", "of", "unsupported", "claims", "or", "missing", "proof"],
                 "resumeRecommendations": ["concrete", "resume", "actionable", "improvements"],
                 "projectRecommendations": ["concrete", "project", "ideas", "to", "bridge", "gaps"],
                 "interviewFocus": ["topics", "likely", "to", "be", "probed"],
                 "nextActions": ["step1", "step2"]
               }
               """.formatted(
                candidate.getName(),
                Optional.ofNullable(candidate.getHeadline()).orElse("Not specified"),
                Optional.ofNullable(candidate.getExperienceYears()).map(String::valueOf).orElse("0"),
                Optional.ofNullable(candidate.getEducation()).orElse("Not specified"),
                candidateSkills.isEmpty() ? "None listed" : candidateSkills,
                Optional.ofNullable(candidate.getResumeText()).map(t -> t.substring(0, Math.min(t.length(), 2500))).orElse("No resume uploaded"),
                opportunity.getTitle(),
                Optional.ofNullable(opportunity.getOrganization()).orElse("Not specified"),
                opportunity.getType(),
                Optional.ofNullable(opportunity.getRoleFamily()).orElse("Not specified"),
                requiredSkills.isEmpty() ? "None listed" : requiredSkills,
                Optional.ofNullable(opportunity.getDescription()).map(d -> d.substring(0, Math.min(d.length(), 2500))).orElse("No description"),
                fit.score(),
                fit.confidence(),
                String.join(", ", fit.matchedSkills()),
                String.join(", ", fit.missingSkills()),
                String.join("; ", resumeQuality.strengths()),
                String.join("; ", resumeQuality.gaps())
        );
    }

    private AiReviewResponse parseAiResponse(String rawContent) {
        String clean = rawContent.trim();
        if (clean.startsWith("```json")) {
            clean = clean.substring(7);
        } else if (clean.startsWith("```")) {
            clean = clean.substring(3);
        }
        if (clean.endsWith("```")) {
            clean = clean.substring(0, clean.length() - 3);
        }
        clean = clean.trim();

        try {
            return objectMapper.readValue(clean, AiReviewResponse.class);
        } catch (Exception e) {
            log.warn("Failed to parse AI JSON directly, attempting fallback extraction: {}", e.getMessage());
            return new AiReviewResponse(
                    clean.length() > 500 ? clean.substring(0, 500) + "..." : clean,
                    List.of(), List.of(), List.of(), List.of(),
                    List.of("Review the detailed AI assessment summary above."),
                    List.of(), List.of(), List.of("Verify role requirements against your resume.")
            );
        }
    }
}
