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

import com.careerops.app.dto.match.JobImportResponse;
import com.careerops.app.dto.match.JobSearchCriteria;
import com.careerops.app.service.JobImportService;

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
    private final JobImportService jobImportService;

    public OpportunityController(OpportunityService opportunityService,
                                 CandidateService candidateService,
                                 JobFitService jobFitService,
                                 ResumeQualityService resumeQualityService,
                                 AiAnalysisService aiAnalysisService,
                                 JobImportService jobImportService) {
        this.opportunityService = opportunityService;
        this.candidateService = candidateService;
        this.jobFitService = jobFitService;
        this.resumeQualityService = resumeQualityService;
        this.aiAnalysisService = aiAnalysisService;
        this.jobImportService = jobImportService;
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

    @GetMapping("/search")
    @Operation(summary = "Search opportunities by title", description = "Derived query finding opportunities by title keyword")
    public ResponseEntity<List<OpportunityResponse>> searchByTitle(@RequestParam String title) {
        return ResponseEntity.ok(opportunityService.findByTitle(title));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Filter opportunities by type", description = "JPQL query finding active opportunities by opportunity type")
    public ResponseEntity<List<OpportunityResponse>> getByType(@PathVariable OpportunityType type) {
        return ResponseEntity.ok(opportunityService.findByType(type));
    }

    @GetMapping("/location/{location}")
    @Operation(summary = "Filter opportunities by location", description = "Derived query finding opportunities by location keyword")
    public ResponseEntity<List<OpportunityResponse>> getByLocation(@PathVariable String location) {
        return ResponseEntity.ok(opportunityService.findByLocation(location));
    }

    @GetMapping("/organization/{organization}")
    @Operation(summary = "Filter opportunities by organization", description = "Derived query finding opportunities by organization keyword")
    public ResponseEntity<List<OpportunityResponse>> getByOrganization(@PathVariable String organization) {
        return ResponseEntity.ok(opportunityService.findByOrganization(organization));
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

    @GetMapping("/types")
    @Operation(summary = "List supported opportunity types with UI labels and metadata")
    public ResponseEntity<List<java.util.Map<String, String>>> listOpportunityTypes() {
        List<java.util.Map<String, String>> types = java.util.Arrays.stream(OpportunityType.values())
                .filter(t -> t != OpportunityType.CTF) // CTF is backward-compatibility alias for CTF_COMPETITION
                .map(t -> java.util.Map.of(
                        "name", t.name(),
                        "displayName", t.getDisplayName(),
                        "actionLabel", t.getActionLabel()
                ))
                .toList();
        return ResponseEntity.ok(types);
    }

    @GetMapping("/nearby")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List opportunities nearby candidate's selected city")
    public ResponseEntity<List<OpportunityResponse>> getNearbyOpportunities(
            @RequestParam(required = false) Double maxDistanceKm,
            Authentication authentication) {
        Candidate candidate = null;
        if (authentication != null && authentication.getName() != null) {
            candidate = candidateService.getByEmail(authentication.getName());
        }
        String city = candidate != null && candidate.getCurrentCity() != null ? candidate.getCurrentCity() : "Chennai";
        List<OpportunityResponse> active = opportunityService.findAll(null, null, null, null, PageRequest.of(0, 50)).getContent();
        return ResponseEntity.ok(active);
    }

    @PostMapping("/refresh")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Refresh opportunities from providers")
    public ResponseEntity<JobImportResponse> refreshOpportunities(
            @RequestBody(required = false) JobSearchCriteria criteria,
            Authentication authentication) {
        return importJobs(criteria, authentication);
    }

    @PostMapping("/import")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Import opportunities", description = "Triggers automatic job discovery from enabled providers and matches against the candidate profile")
    public ResponseEntity<JobImportResponse> importJobs(
            @RequestBody(required = false) JobSearchCriteria criteria,
            Authentication authentication) {
        Candidate candidate = null;
        if (authentication != null && authentication.getName() != null) {
            candidate = candidateService.getByEmail(authentication.getName());
        }
        JobImportResponse response = jobImportService.importJobs(candidate, criteria);
        return ResponseEntity.ok(response);
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
