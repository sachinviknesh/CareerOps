package com.careerops.app.controller;

import com.careerops.app.dto.PageResponse;
import com.careerops.app.dto.match.JobImportResponse;
import com.careerops.app.dto.match.JobMatchResponse;
import com.careerops.app.dto.match.JobSearchCriteria;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.OpportunityType;
import com.careerops.app.service.CandidateService;
import com.careerops.app.service.JobImportService;
import com.careerops.app.service.JobMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Job Matches", description = "Automated candidate fit matching, ranking, and job discovery endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MatchController {

    private final JobMatchingService matchingService;
    private final JobImportService importService;
    private final CandidateService candidateService;

    public MatchController(JobMatchingService matchingService,
                           JobImportService importService,
                           CandidateService candidateService) {
        this.matchingService = matchingService;
        this.importService = importService;
        this.candidateService = candidateService;
    }

    @GetMapping
    @Operation(summary = "List candidate job matches", description = "Returns paged match results for the authenticated candidate sorted by fit score with filtering")
    public ResponseEntity<PageResponse<JobMatchResponse>> getMatches(
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) OpportunityType type,
            @RequestParam(required = false) String workMode,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadline,
            @RequestParam(required = false) String skill,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Candidate candidate = candidateService.getByEmail(authentication.getName());

        Page<JobMatchResponse> matchPage = matchingService.getFilteredMatches(
                candidate.getId(), minScore, type, workMode, location, deadline, skill,
                PageRequest.of(page, Math.min(size, 100))
        );

        // If no matches are found yet for this candidate, trigger an initial recalculation
        if (matchPage.isEmpty() && page == 0 && minScore == null && type == null && skill == null) {
            matchingService.recalculateForCandidate(candidate);
            matchPage = matchingService.getFilteredMatches(
                    candidate.getId(), minScore, type, workMode, location, deadline, skill,
                    PageRequest.of(page, Math.min(size, 100))
            );
        }

        return ResponseEntity.ok(PageResponse.from(matchPage));
    }

    @GetMapping("/top")
    @Operation(summary = "Get top job matches", description = "Returns top 5-10 best-fit opportunities for dashboard display")
    public ResponseEntity<List<JobMatchResponse>> getTopMatches(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {

        Candidate candidate = candidateService.getByEmail(authentication.getName());
        List<JobMatchResponse> top = matchingService.getTopMatches(candidate.getId(), limit);

        if (top.isEmpty()) {
            matchingService.recalculateForCandidate(candidate);
            top = matchingService.getTopMatches(candidate.getId(), limit);
        }

        return ResponseEntity.ok(top);
    }

    @GetMapping("/{opportunityId}")
    @Operation(summary = "Get match detail", description = "Returns the pre-calculated match record for an opportunity and the authenticated candidate")
    public ResponseEntity<JobMatchResponse> getMatch(
            @PathVariable Long opportunityId,
            Authentication authentication) {

        Candidate candidate = candidateService.getByEmail(authentication.getName());
        JobMatchResponse response = matchingService.getMatchForCandidateAndOpportunity(candidate.getId(), opportunityId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recalculate")
    @Operation(summary = "Recalculate matches", description = "Recalculates fit scores across all active opportunities for the authenticated candidate")
    public ResponseEntity<List<JobMatchResponse>> recalculateMatches(Authentication authentication) {
        Candidate candidate = candidateService.getByEmail(authentication.getName());
        List<JobMatchResponse> updated = matchingService.recalculateForCandidate(candidate);
        return ResponseEntity.ok(updated);
    }
}
