package com.careerops.app.service;

import com.careerops.app.dto.application.ApplicationRequest;
import com.careerops.app.dto.application.ApplicationResponse;
import com.careerops.app.entity.Application;
import com.careerops.app.entity.ApplicationStatus;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;
import com.careerops.app.exception.BadRequestException;
import com.careerops.app.exception.ResourceNotFoundException;
import com.careerops.app.repository.ApplicationRepository;
import com.careerops.app.repository.OpportunityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationRepository applicationRepo;
    private final OpportunityRepository opportunityRepo;
    private final CandidateService candidateService;
    private final OpportunityService opportunityService;

    public ApplicationService(ApplicationRepository applicationRepo,
                              OpportunityRepository opportunityRepo,
                              CandidateService candidateService,
                              OpportunityService opportunityService) {
        this.applicationRepo = applicationRepo;
        this.opportunityRepo = opportunityRepo;
        this.candidateService = candidateService;
        this.opportunityService = opportunityService;
    }

    public Page<ApplicationResponse> findAll(String candidateEmail,
                                            ApplicationStatus status,
                                            OpportunityType type,
                                            Pageable pageable) {
        Candidate candidate = candidateService.getByEmail(candidateEmail);
        Page<Application> page;

        if (status != null) {
            page = applicationRepo.findByCandidateIdAndStatus(candidate.getId(), status, pageable);
        } else if (type != null) {
            page = applicationRepo.findApplicationsByType(candidate.getId(), type, pageable);
        } else {
            page = applicationRepo.findByCandidateId(candidate.getId(), pageable);
        }

        return page.map(this::toResponse);
    }

    public ApplicationResponse findById(String candidateEmail, Long id) {
        Candidate candidate = candidateService.getByEmail(candidateEmail);
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        if (!app.getCandidate().getId().equals(candidate.getId())) {
            throw new ResourceNotFoundException("Application not found with id: " + id);
        }

        return toResponse(app);
    }

    @Transactional
    public ApplicationResponse create(String candidateEmail, ApplicationRequest req) {
        if (req.opportunityId() == null) {
            throw new BadRequestException("opportunityId is required to create an application.");
        }

        Candidate candidate = candidateService.getByEmail(candidateEmail);
        Opportunity opportunity = opportunityRepo.findById(req.opportunityId())
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + req.opportunityId()));

        // Section 50: Duplicate application rule
        if (applicationRepo.existsByCandidateIdAndOpportunityId(candidate.getId(), opportunity.getId())) {
            log.warn("Candidate {} already applied/tracked opportunity {}", candidate.getId(), opportunity.getId());
            throw new BadRequestException("You have already added this opportunity to your tracker.");
        }

        Application app = new Application();
        app.setCandidate(candidate);
        app.setOpportunity(opportunity);
        app.setStatus(req.status() != null ? req.status() : ApplicationStatus.TO_APPLY);

        if (app.getStatus() == ApplicationStatus.APPLIED && req.appliedAt() == null) {
            app.setAppliedAt(LocalDate.now());
        } else {
            app.setAppliedAt(req.appliedAt());
        }

        app.setNextActionDate(req.nextActionDate());
        app.setNotes(req.notes());

        Application saved = applicationRepo.save(app);
        log.info("Application created: candidate {} -> opportunity '{}' (status: {})",
                candidate.getId(), opportunity.getTitle(), saved.getStatus());
        return toResponse(saved);
    }

    @Transactional
    public ApplicationResponse update(String candidateEmail, Long id, ApplicationRequest req) {
        Candidate candidate = candidateService.getByEmail(candidateEmail);
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        if (!app.getCandidate().getId().equals(candidate.getId())) {
            throw new ResourceNotFoundException("Application not found with id: " + id);
        }

        ApplicationStatus oldStatus = app.getStatus();
        app.setStatus(req.status());

        if (req.status() == ApplicationStatus.APPLIED && app.getAppliedAt() == null && req.appliedAt() == null) {
            app.setAppliedAt(LocalDate.now());
        } else if (req.appliedAt() != null) {
            app.setAppliedAt(req.appliedAt());
        }

        app.setNextActionDate(req.nextActionDate());
        app.setNotes(req.notes());

        Application updated = applicationRepo.save(app);
        log.info("Application updated: id {} status changed from {} to {}", id, oldStatus, updated.getStatus());
        return toResponse(updated);
    }

    @Transactional
    public void delete(String candidateEmail, Long id) {
        Candidate candidate = candidateService.getByEmail(candidateEmail);
        Application app = applicationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        if (!app.getCandidate().getId().equals(candidate.getId())) {
            throw new ResourceNotFoundException("Application not found with id: " + id);
        }

        applicationRepo.delete(app);
        log.info("Application deleted: id {} for candidate {}", id, candidate.getId());
    }

    public Map<String, Object> getDashboardStats(String candidateEmail) {
        Candidate candidate = candidateService.getByEmail(candidateEmail);
        Long candidateId = candidate.getId();

        Map<String, Object> stats = new HashMap<>();
        long toApply = applicationRepo.countByCandidateIdAndStatus(candidateId, ApplicationStatus.TO_APPLY);
        long applied = applicationRepo.countByCandidateIdAndStatus(candidateId, ApplicationStatus.APPLIED);
        long assessment = applicationRepo.countByCandidateIdAndStatus(candidateId, ApplicationStatus.ASSESSMENT);
        long interview = applicationRepo.countByCandidateIdAndStatus(candidateId, ApplicationStatus.INTERVIEW);
        long selected = applicationRepo.countByCandidateIdAndStatus(candidateId, ApplicationStatus.SELECTED);
        long rejected = applicationRepo.countByCandidateIdAndStatus(candidateId, ApplicationStatus.REJECTED);
        long withdrawn = applicationRepo.countByCandidateIdAndStatus(candidateId, ApplicationStatus.WITHDRAWN);

        long inProgress = assessment + interview;
        long totalTracked = toApply + applied + assessment + interview + selected + rejected + withdrawn;

        stats.put("toApply", toApply);
        stats.put("applied", applied);
        stats.put("assessment", assessment);
        stats.put("interview", interview);
        stats.put("selected", selected);
        stats.put("rejected", rejected);
        stats.put("withdrawn", withdrawn);
        stats.put("inProgress", inProgress);
        stats.put("totalTracked", totalTracked);

        return stats;
    }

    public ApplicationResponse toResponse(Application a) {
        return new ApplicationResponse(
                a.getId(),
                a.getCandidate().getId(),
                opportunityService.toResponse(a.getOpportunity()),
                a.getStatus(),
                a.getAppliedAt(),
                a.getNextActionDate(),
                a.getNotes(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }
}
