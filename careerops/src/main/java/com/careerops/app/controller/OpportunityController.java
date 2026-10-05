package com.careerops.app.controller;

import com.careerops.app.dto.PageResponse;
import com.careerops.app.dto.fit.AiReviewResponse;
import com.careerops.app.dto.fit.FitResponse;
import com.careerops.app.dto.fit.ResumeAnalysisResponse;
import com.careerops.app.dto.opportunity.OpportunityRequest;
import com.careerops.app.dto.opportunity.OpportunityResponse;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;
import com.careerops.app.exception.BadRequestException;
import com.careerops.app.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/opportunities")
@Tag(name = "Opportunities", description = "Opportunity management (Jobs, Internships, Hackathons, CTFs) with derived/JPQL search, pagination, and sorting")
public class OpportunityController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "title", "deadline", "postedAt", "createdAt", "organization", "type", "location"
    );

    private final OpportunityService opportunityService;
    private final CandidateService candidateService;
    private final JobFitService jobFitService;
    private final ResumeQualityService resumeQualityService;
    private final AiAnalysisService aiAnalysisService;

    public OpportunityController(OpportunityService opportunityService,
                                 CandidateService candidateService,
                                 JobFitService jobFitService,
                                 ResumeQualityService resumeQualityService,
                                 AiAnalysisService aiAnalysisService) {
        this.opportunityService = opportunityService;
        this.candidateService = candidateService;
        this.jobFitService = jobFitService;
        this.resumeQualityService = resumeQualityService;
        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping
    @Operation(summary = "List opportunities", description = "Paged & sorted list of opportunities with multi-parameter filtering (type, search, roleFamily, skill)")
    public ResponseEntity<PageResponse<OpportunityResponse>> listOpportunities(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OpportunityType type,
            @RequestParam(required = false) String roleFamily,
            @RequestParam(required = false) String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<OpportunityResponse> results = opportunityService.findAll(search, type, roleFamily, skill, pageable);
        return ResponseEntity.ok(PageResponse.from(results));
    }

    @GetMapping("/closing-soon")
    @Operation(summary = "List opportunities closing soon", description = "JPQL query for active opportunities with deadlines in the next N days")
    public ResponseEntity<List<OpportunityResponse>> closingSoon(
            @RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(opportunityService.findClosingSoon(days));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get opportunity by ID", description = "Retrieves full details and required skills for an opportunity")
    public ResponseEntity<OpportunityResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(opportunityService.findById(id));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create opportunity", description = "Creates a new opportunity record with associated required skills")
    public ResponseEntity<OpportunityResponse> create(@Valid @RequestBody OpportunityRequest req) {
        OpportunityResponse created = opportunityService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update opportunity", description = "Updates fields and required skill associations for an opportunity")
    public ResponseEntity<OpportunityResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody OpportunityRequest req) {
        return ResponseEntity.ok(opportunityService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete opportunity", description = "Removes an opportunity and cascades or clears relations")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        opportunityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ai-review")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Generate optional AI review", description = "Invokes OpenAI/Ollama compatible endpoint to produce an evidence-grounded review against this opportunity")
    public ResponseEntity<AiReviewResponse> aiReview(@PathVariable Long id, Authentication authentication) {
        Candidate candidate = candidateService.getByEmail(authentication.getName());
        Opportunity opportunity = opportunityService.getEntityById(id);

        FitResponse fit = jobFitService.analyze(candidate, opportunity);
        ResumeAnalysisResponse resumeQuality = resumeQualityService.analyze(
                candidate.getResumeText() != null ? candidate.getResumeText() : ""
        );

        AiReviewResponse review = aiAnalysisService.reviewOpportunity(candidate, opportunity, fit, resumeQuality);
        return ResponseEntity.ok(review);
    }

    private Pageable createPageable(int page, int size, String sortParam) {
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(property)) {
            throw new BadRequestException("Invalid sort field: '" + property + "'. Allowed: " + ALLOWED_SORT_FIELDS);
        }

        Sort.Direction direction = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(page, Math.min(size, 100), Sort.by(direction, property));
    }
}
