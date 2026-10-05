package com.careerops.app.controller;

import com.careerops.app.dto.PageResponse;
import com.careerops.app.dto.application.ApplicationRequest;
import com.careerops.app.dto.application.ApplicationResponse;
import com.careerops.app.entity.ApplicationStatus;
import com.careerops.app.entity.OpportunityType;
import com.careerops.app.exception.BadRequestException;
import com.careerops.app.service.ApplicationService;
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

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Applications", description = "Application tracking workflow (TO_APPLY, APPLIED, ASSESSMENT, INTERVIEW, etc.) with pagination, sorting, and stats")
@SecurityRequirement(name = "bearerAuth")
public class ApplicationController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "createdAt", "updatedAt", "appliedAt", "nextActionDate", "status"
    );

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    @Operation(summary = "List candidate applications", description = "Paged & sorted list of applications for the authenticated candidate with status and opportunity type filtering")
    public ResponseEntity<PageResponse<ApplicationResponse>> listApplications(
            Authentication authentication,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) OpportunityType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Pageable pageable = createPageable(page, size, sort);
        Page<ApplicationResponse> results = applicationService.findAll(authentication.getName(), status, type, pageable);
        return ResponseEntity.ok(PageResponse.from(results));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get application dashboard statistics", description = "Returns aggregated counts across all application statuses for candidate dashboard")
    public ResponseEntity<Map<String, Object>> getStats(Authentication authentication) {
        return ResponseEntity.ok(applicationService.getDashboardStats(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application by ID", description = "Retrieves an application details and embedded opportunity for the authenticated candidate")
    public ResponseEntity<ApplicationResponse> getById(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.findById(authentication.getName(), id));
    }

    @PostMapping
    @Operation(summary = "Create/track application", description = "Adds an opportunity to the candidate's tracker with status (TO_APPLY, APPLIED, etc.) preventing duplicates")
    public ResponseEntity<ApplicationResponse> create(Authentication authentication,
                                                      @Valid @RequestBody ApplicationRequest req) {
        ApplicationResponse created = applicationService.create(authentication.getName(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update application status & notes", description = "Updates the stage, next action date, or notes of an existing application")
    public ResponseEntity<ApplicationResponse> update(Authentication authentication,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody ApplicationRequest req) {
        return ResponseEntity.ok(applicationService.update(authentication.getName(), id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tracked application", description = "Removes an application from the tracker")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        applicationService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
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
