package com.careerops.app.controller;

import com.careerops.app.dto.match.JobImportResponse;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.JobSource;
import com.careerops.app.exception.ResourceNotFoundException;
import com.careerops.app.repository.JobSourceRepository;
import com.careerops.app.service.CandidateService;
import com.careerops.app.service.OpportunityDiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sources")
@Tag(name = "Sources", description = "Source registry and external provider management")
@SecurityRequirement(name = "bearerAuth")
public class SourceController {

    private final JobSourceRepository sourceRepo;
    private final OpportunityDiscoveryService discoveryService;
    private final CandidateService candidateService;

    public SourceController(JobSourceRepository sourceRepo,
                            OpportunityDiscoveryService discoveryService,
                            CandidateService candidateService) {
        this.sourceRepo = sourceRepo;
        this.discoveryService = discoveryService;
        this.candidateService = candidateService;
    }

    @GetMapping
    @Operation(summary = "List all opportunity sources in registry")
    public ResponseEntity<List<JobSource>> listSources() {
        return ResponseEntity.ok(sourceRepo.findAll());
    }

    @PostMapping
    @Operation(summary = "Register a new opportunity source")
    public ResponseEntity<JobSource> addSource(@RequestBody JobSource source) {
        if (source.getCode() == null || source.getCode().isBlank()) {
            source.setCode("SRC_" + System.currentTimeMillis());
        }
        if (source.getStatus() == null) {
            source.setStatus("ACTIVE");
        }
        JobSource saved = sourceRepo.save(source);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an opportunity source")
    public ResponseEntity<JobSource> updateSource(@PathVariable Long id, @RequestBody JobSource req) {
        JobSource existing = sourceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found with id: " + id));

        if (req.getName() != null) existing.setName(req.getName());
        if (req.getBaseUrl() != null) existing.setBaseUrl(req.getBaseUrl());
        if (req.getSourceType() != null) existing.setSourceType(req.getSourceType());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        if (req.getSupportedOpportunityTypes() != null) existing.setSupportedOpportunityTypes(req.getSupportedOpportunityTypes());
        existing.setEnabled(req.isEnabled());

        return ResponseEntity.ok(sourceRepo.save(existing));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an opportunity source")
    public ResponseEntity<Void> deleteSource(@PathVariable Long id) {
        JobSource existing = sourceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found with id: " + id));
        sourceRepo.delete(existing);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/toggle")
    @Operation(summary = "Toggle source enable/disable")
    public ResponseEntity<JobSource> toggleSource(@PathVariable Long id) {
        JobSource existing = sourceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found with id: " + id));
        existing.setEnabled(!existing.isEnabled());
        existing.setStatus(existing.isEnabled() ? "ACTIVE" : "DISABLED");
        return ResponseEntity.ok(sourceRepo.save(existing));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Trigger manual refresh across all enabled sources")
    public ResponseEntity<JobImportResponse> refreshSources(Authentication authentication) {
        Candidate candidate = null;
        if (authentication != null && authentication.getName() != null) {
            try {
                candidate = candidateService.getByEmail(authentication.getName());
            } catch (Exception ignored) {}
        }
        JobImportResponse response = discoveryService.runDiscoveryPipeline(candidate, null);
        return ResponseEntity.ok(response);
    }
}
