package com.careerops.app.controller;

import com.careerops.app.dto.match.JobImportResponse;
import com.careerops.app.dto.match.JobSearchCriteria;
import com.careerops.app.entity.Candidate;
import com.careerops.app.service.CandidateService;
import com.careerops.app.service.OpportunityDiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Job discovery and refresh endpoints")
@SecurityRequirement(name = "bearerAuth")
public class JobRefreshController {

    private final OpportunityDiscoveryService discoveryService;
    private final CandidateService candidateService;

    public JobRefreshController(OpportunityDiscoveryService discoveryService,
                                CandidateService candidateService) {
        this.discoveryService = discoveryService;
        this.candidateService = candidateService;
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh jobs and opportunities from all enabled providers")
    public ResponseEntity<JobImportResponse> refreshJobs(
            @RequestBody(required = false) JobSearchCriteria criteria,
            Authentication authentication) {
        Candidate candidate = null;
        if (authentication != null && authentication.getName() != null) {
            try {
                candidate = candidateService.getByEmail(authentication.getName());
            } catch (Exception ignored) {}
        }
        JobImportResponse response = discoveryService.runDiscoveryPipeline(candidate, criteria);
        return ResponseEntity.ok(response);
    }
}
