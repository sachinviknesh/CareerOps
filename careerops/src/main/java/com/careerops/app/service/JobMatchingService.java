package com.careerops.app.service;

import com.careerops.app.dto.fit.FitResponse;
import com.careerops.app.dto.match.JobMatchResponse;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.JobMatch;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;
import com.careerops.app.exception.ResourceNotFoundException;
import com.careerops.app.repository.JobMatchRepository;
import com.careerops.app.repository.OpportunityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobMatchingService {

    private static final Logger log = LoggerFactory.getLogger(JobMatchingService.class);

    private final JobMatchRepository matchRepo;
    private final JobFitService jobFitService;
    private final OpportunityRepository opportunityRepo;
    private final LocationTravelService locationTravelService;
    private final EligibilityService eligibilityService;

    public JobMatchingService(JobMatchRepository matchRepo,
                              JobFitService jobFitService,
                              OpportunityRepository opportunityRepo,
                              LocationTravelService locationTravelService,
                              EligibilityService eligibilityService) {
        this.matchRepo = matchRepo;
        this.jobFitService = jobFitService;
        this.opportunityRepo = opportunityRepo;
        this.locationTravelService = locationTravelService;
        this.eligibilityService = eligibilityService;
    }

    public JobMatch computeAndSaveMatch(Candidate candidate, Opportunity opportunity) {
        FitResponse fit = jobFitService.analyze(candidate, opportunity);
        LocationTravelService.TravelIntel travel = locationTravelService.analyzeTravel(opportunity, candidate);
        EligibilityService.EligibilityResult elig = eligibilityService.evaluate(opportunity, candidate);

        String matchedStr = String.join(", ", fit.matchedSkills());
        String missingStr = String.join(", ", fit.missingSkills());
        String recsStr = String.join(" | ", fit.nextActions());

        String expMatch = "UNKNOWN";
        if (fit.eligibilityChecks() != null) {
            for (var chk : fit.eligibilityChecks()) {
                if ("Experience".equalsIgnoreCase(chk.criterion())) {
                    expMatch = chk.result();
                    break;
                }
            }
        }

        double resumeQualityScore = 0.5;
        if (candidate.getResumeText() != null && !candidate.getResumeText().isBlank()) {
            resumeQualityScore = Math.min(candidate.getResumeText().length() / 2000.0, 1.0);
        }

        JobMatch match = matchRepo.findByCandidateIdAndOpportunityId(candidate.getId(), opportunity.getId())
                .orElseGet(() -> {
                    JobMatch m = new JobMatch();
                    m.setCandidate(candidate);
                    m.setOpportunity(opportunity);
                    return m;
                });

        match.setFitScore(fit.score());
        match.setLocationFit(travel.getLocationFitScore());
        match.setEligibilityStatus(elig.getStatus());
        match.setTravelRequired(travel.isTravelRequired() ? (travel.getMode().equalsIgnoreCase("hybrid") ? "OPTIONAL" : "REQUIRED") : "NONE");
        match.setEstimatedDistanceKm(travel.getDistanceKm());
        match.setConfidence(fit.confidence());
        match.setMatchedSkills(matchedStr);
        match.setMissingSkills(missingStr);
        match.setExperienceMatch(expMatch);
        match.setResumeQualityScore(resumeQualityScore);
        match.setRecommendations(recsStr);
        match.setCalculatedAt(LocalDateTime.now());

        JobMatch saved = matchRepo.save(match);
        log.debug("Computed match for candidate {} and opp {}: CareerFit {}%, LocFit {}% ({})",
                candidate.getId(), opportunity.getId(), fit.score(), travel.getLocationFitScore(), fit.confidence());
        return saved;
    }

    public List<JobMatchResponse> recalculateForCandidate(Candidate candidate) {
        List<Opportunity> activeOpps = opportunityRepo.findAllActive();
        log.info("Recalculating job matches for candidate '{}' across {} active opportunities",
                candidate.getEmail(), activeOpps.size());

        List<JobMatchResponse> results = new ArrayList<>();
        for (Opportunity opp : activeOpps) {
            JobMatch m = computeAndSaveMatch(candidate, opp);
            results.add(toResponse(m, candidate));
        }

        results.sort(Comparator.comparingInt(JobMatchResponse::fitScore).reversed());
        return results;
    }

    @Transactional(readOnly = true)
    public List<JobMatchResponse> getTopMatches(Long candidateId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        List<JobMatch> matches = matchRepo.findTop10ByCandidateIdOrderByFitScoreDesc(candidateId);
        return matches.stream()
                .limit(safeLimit)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<JobMatchResponse> getFilteredMatches(Long candidateId,
                                                     Integer minScore,
                                                     OpportunityType type,
                                                     String workMode,
                                                     String location,
                                                     LocalDate deadline,
                                                     String skill,
                                                     Pageable pageable) {
        Page<JobMatch> page = matchRepo.findMatchesWithFilters(
                candidateId, minScore, type, workMode, location, deadline, skill, pageable
        );
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public JobMatchResponse getMatchForCandidateAndOpportunity(Long candidateId, Long opportunityId) {
        JobMatch match = matchRepo.findByCandidateIdAndOpportunityId(candidateId, opportunityId)
                .orElseThrow(() -> new ResourceNotFoundException("No match record found for candidate " + candidateId + " and opportunity " + opportunityId));
        return toResponse(match);
    }

    public JobMatchResponse toResponse(JobMatch m) {
        return toResponse(m, m.getCandidate());
    }

    public JobMatchResponse toResponse(JobMatch m, Candidate candidate) {
        Opportunity o = m.getOpportunity();

        List<String> matched = parseList(m.getMatchedSkills(), ", ");
        List<String> missing = parseList(m.getMissingSkills(), ", ");
        List<String> recs = parseList(m.getRecommendations(), " | ");

        LocationTravelService.TravelIntel travel = locationTravelService.analyzeTravel(o, candidate);
        String travelBadge = travel.getTravelBadge();
        String actionLabel = o.getType() != null ? o.getType().getActionLabel() : "Apply Now";
        boolean travelReq = travel.isTravelRequired();

        return new JobMatchResponse(
                m.getId(),
                o.getId(),
                o.getTitle(),
                o.getOrganization(),
                o.getType(),
                o.getRoleFamily(),
                o.getDescription(),
                o.getLocation(),
                o.getWorkMode(),
                o.getUrl(),
                o.getDeadline(),
                m.getFitScore(),
                m.getConfidence(),
                matched,
                missing,
                m.getExperienceMatch(),
                m.getResumeQualityScore(),
                recs,
                m.getCalculatedAt(),
                m.getLocationFit() > 0 ? m.getLocationFit() : travel.getLocationFitScore(),
                m.getEligibilityStatus() != null ? m.getEligibilityStatus() : "ELIGIBLE",
                travelReq,
                travel.getDistanceKm(),
                travel.getDistanceLabel(),
                travelBadge,
                travel.getEstimatedTravelTime(),
                travel.getTransportOptions(),
                travel.getApproximateCost(),
                o.getEventStartDate(),
                o.getEventEndDate(),
                actionLabel
        );
    }

    private List<String> parseList(String raw, String delimiter) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(java.util.regex.Pattern.quote(delimiter)))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
