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

        String targetModel = resolveModel(model);
        String prompt = buildPrompt(candidate, opportunity, fitResponse, resumeQuality);

        log.info("Sending AI review request to {} with model {}", baseUrl, targetModel);

        try {
            return executeAiRequest(targetModel, prompt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("AI review request was interrupted", e);
            throw new BadRequestException("AI request was interrupted: " + e.getMessage());
        } catch (IOException e) {
            log.error("AI request communication failure", e);
            throw new BadRequestException("Failed to communicate with AI endpoint: " + e.getMessage());
        }
    }

    private String resolveModel(String configuredModel) {
        if (configuredModel == null || configuredModel.isBlank()) {
            return "gemini-3.8-flash";
        }
        String m = configuredModel.trim();
        if ("gemini-1.5-flash".equalsIgnoreCase(m) || "gemini-2.5-flash".equalsIgnoreCase(m)) {
            log.info("Model '{}' is obsolete or retired. Normalizing to supported 'gemini-3.8-flash'", m);
            return "gemini-3.8-flash";
        }
        return m;
    }

    private String getEffectiveApiKey() {
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey.trim();
        }
        String sysAiKey = System.getProperty("AI_API_KEY");
        if (sysAiKey != null && !sysAiKey.isBlank()) {
            return sysAiKey.trim();
        }
        String envAiKey = System.getenv("AI_API_KEY");
        if (envAiKey != null && !envAiKey.isBlank()) {
            return envAiKey.trim();
        }
        String jobKey = System.getProperty("JOB_API_KEY");
        if (jobKey != null && !jobKey.isBlank()) {
            return jobKey.trim();
        }
        return null;
    }

    private AiReviewResponse executeAiRequest(String targetModel, String prompt) throws IOException, InterruptedException {
        boolean isGeminiNative = baseUrl.contains("generativelanguage.googleapis.com") && !baseUrl.contains("/openai");
        String endpoint;
        String jsonPayload;
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json");

        String systemInstructions =
                "You are an objective hiring readiness analyzer. " +
                "Use ONLY evidence available in the candidate data. " +
                "If information is missing, state clearly that it is missing. " +
                "Do NOT fabricate achievements, certifications, or experience. " +
                "Always respond with a valid, clean JSON object matching the requested schema without markdown backticks.";

        String effectiveKey = getEffectiveApiKey();

        if (isGeminiNative) {
            // Google Gemini native generateContent API
            String cleanBase = baseUrl.replaceAll("/+$", "");
            if (cleanBase.contains("/models/")) {
                endpoint = cleanBase.endsWith(":generateContent") ? cleanBase : cleanBase + ":generateContent";
            } else {
                endpoint = cleanBase + "/models/" + targetModel + ":generateContent";
            }

            if (effectiveKey != null && !effectiveKey.isBlank()) {
                reqBuilder.header("x-goog-api-key", effectiveKey);
                if (!endpoint.contains("key=")) {
                    endpoint += (endpoint.contains("?") ? "&" : "?") + "key=" + effectiveKey;
                }
            }

            Map<String, Object> nativePayload = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", systemInstructions + "\n\n" + prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.2
                    )
            );
            jsonPayload = objectMapper.writeValueAsString(nativePayload);
            reqBuilder.uri(URI.create(endpoint));
        } else {
            // OpenAI-compatible /chat/completions API (Ollama, Gemini OpenAI endpoint, etc.)
            endpoint = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
            reqBuilder.uri(URI.create(endpoint));
            if (effectiveKey != null && !effectiveKey.isBlank()) {
                reqBuilder.header("Authorization", "Bearer " + effectiveKey);
            }

            Map<String, Object> requestBody = Map.of(
                    "model", targetModel,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemInstructions),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "temperature", 0.2
            );
            jsonPayload = objectMapper.writeValueAsString(requestBody);
        }

        HttpRequest request = reqBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonPayload)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Automatic retry with gemini-3.8-flash if an obsolete model returned 404
        if (response.statusCode() == 404 && response.body().contains("models/") && !targetModel.equals("gemini-3.8-flash")) {
            log.warn("AI model '{}' returned 404 not found. Retrying with 'gemini-3.8-flash'...", targetModel);
            return executeAiRequest("gemini-3.8-flash", prompt);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.error("AI API returned error status {}: {}", response.statusCode(), response.body());
            throw new BadRequestException("AI provider returned error HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String content = "";
        if (root.has("candidates") && root.path("candidates").isArray() && !root.path("candidates").isEmpty()) {
            content = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        } else if (root.has("choices") && root.path("choices").isArray() && !root.path("choices").isEmpty()) {
            content = root.path("choices").path(0).path("message").path("content").asText();
        } else {
            content = response.body();
        }

        log.info("AI review response received successfully");
        return parseAiResponse(content);
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
