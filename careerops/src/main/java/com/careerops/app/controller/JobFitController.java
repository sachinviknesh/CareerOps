package com.careerops.app.controller;

import com.careerops.app.dto.fit.FitResponse;
import com.careerops.app.dto.fit.ResumeAnalysisResponse;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.service.CandidateService;
import com.careerops.app.service.JobFitService;
import com.careerops.app.service.OpportunityService;
import com.careerops.app.service.ResumeQualityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Job Fit & Resume Analysis", description = "Offline-first job-fit heuristic scoring, skill gap detection, and resume structure evaluation")
@SecurityRequirement(name = "bearerAuth")
public class JobFitController {

    private final CandidateService candidateService;
    private final OpportunityService opportunityService;
    private final JobFitService jobFitService;
    private final ResumeQualityService resumeQualityService;

    public JobFitController(CandidateService candidateService,
                             OpportunityService opportunityService,
                             JobFitService jobFitService,
                             ResumeQualityService resumeQualityService) {
        this.candidateService = candidateService;
        this.opportunityService = opportunityService;
        this.jobFitService = jobFitService;
        this.resumeQualityService = resumeQualityService;
    }

    @GetMapping("/api/opportunities/{id}/fit")
    @Operation(summary = "Analyze offline job-fit", description = "Calculates multi-factor job-fit score comparing authenticated candidate skills, experience, and resume against the opportunity")
    public ResponseEntity<FitResponse> analyzeFit(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new com.careerops.app.exception.UnauthorizedException("Authentication required to analyze candidate fit");
        }
        Candidate candidate = candidateService.getByEmail(authentication.getName());
        Opportunity opportunity = opportunityService.getEntityById(id);
        FitResponse fit = jobFitService.analyze(candidate, opportunity);
        return ResponseEntity.ok(fit);
    }

    @GetMapping("/api/candidates/me/resume-analysis")
    @Operation(summary = "Analyze candidate resume quality", description = "Performs offline heuristics on uploaded resume text: section detection, quantifiable metric count, technical keyword frequency")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(Authentication authentication) {
        Candidate candidate = candidateService.getByEmail(authentication.getName());
        String text = candidate.getResumeText() != null ? candidate.getResumeText() : "";
        ResumeAnalysisResponse quality = resumeQualityService.analyze(text);
        return ResponseEntity.ok(quality);
    }
}
