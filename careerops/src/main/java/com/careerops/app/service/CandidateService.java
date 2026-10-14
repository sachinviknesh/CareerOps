package com.careerops.app.service;

import com.careerops.app.dto.candidate.CandidateResponse;
import com.careerops.app.dto.candidate.UpdateCandidateRequest;
import com.careerops.app.dto.skill.SkillResponse;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.Skill;
import com.careerops.app.exception.ResourceNotFoundException;
import com.careerops.app.repository.CandidateRepository;
import com.careerops.app.repository.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CandidateService {

    private static final Logger log = LoggerFactory.getLogger(CandidateService.class);

    private final CandidateRepository candidateRepo;
    private final SkillRepository skillRepo;
    private final JobMatchingService matchingService;

    public CandidateService(CandidateRepository candidateRepo,
                            SkillRepository skillRepo,
                            @Lazy JobMatchingService matchingService) {
        this.candidateRepo = candidateRepo;
        this.skillRepo = skillRepo;
        this.matchingService = matchingService;
    }

    public Candidate getByEmail(String email) {
        return candidateRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + email));
    }

    public CandidateResponse toResponse(Candidate c) {
        Set<SkillResponse> skillResponses = c.getSkills().stream()
                .map(s -> new SkillResponse(s.getId(), s.getName(), s.getCategory(), s.getCreatedAt()))
                .collect(Collectors.toSet());

        return new CandidateResponse(
                c.getId(), c.getName(), c.getEmail(), c.getHeadline(),
                c.getLocation(), c.getEducation(), c.getExperienceYears(),
                c.getTargetRoles(), c.getResumeFileName(),
                c.getResumeText() != null && !c.getResumeText().isBlank(),
                skillResponses, c.getCreatedAt(), c.getUpdatedAt(),
                c.getDegree(), c.getUniversityOrCollege(), c.getGraduationYear(),
                c.getProjects(), c.getCertifications(), c.getPreferredOpportunityTypes(),
                c.getPreferredStates(), c.getPreferredCities(), c.getPreferredWorkMode(),
                c.isWillingToTravel(), c.getMaxTravelDistanceKm(), c.getCareerInterests(),
                c.getCurrentCity(), c.getCurrentState(), c.getCurrentCountry()
        );
    }

    @Transactional
    public CandidateResponse update(String email, UpdateCandidateRequest req) {
        Candidate candidate = getByEmail(email);

        if (req.name() != null && !req.name().isBlank()) candidate.setName(req.name());
        if (req.headline() != null) candidate.setHeadline(req.headline());
        if (req.location() != null) candidate.setLocation(req.location());
        if (req.education() != null) candidate.setEducation(req.education());
        if (req.experienceYears() != null) candidate.setExperienceYears(req.experienceYears());
        if (req.targetRoles() != null) candidate.setTargetRoles(req.targetRoles());

        if (req.degree() != null) candidate.setDegree(req.degree());
        if (req.universityOrCollege() != null) candidate.setUniversityOrCollege(req.universityOrCollege());
        if (req.graduationYear() != null) candidate.setGraduationYear(req.graduationYear());
        if (req.projects() != null) candidate.setProjects(req.projects());
        if (req.certifications() != null) candidate.setCertifications(req.certifications());
        if (req.preferredOpportunityTypes() != null) candidate.setPreferredOpportunityTypes(req.preferredOpportunityTypes());
        if (req.preferredStates() != null) candidate.setPreferredStates(req.preferredStates());
        if (req.preferredCities() != null) candidate.setPreferredCities(req.preferredCities());
        if (req.preferredWorkMode() != null) candidate.setPreferredWorkMode(req.preferredWorkMode());
        if (req.willingToTravel() != null) candidate.setWillingToTravel(req.willingToTravel());
        if (req.maxTravelDistanceKm() != null) candidate.setMaxTravelDistanceKm(req.maxTravelDistanceKm());
        if (req.careerInterests() != null) candidate.setCareerInterests(req.careerInterests());
        if (req.currentCity() != null) candidate.setCurrentCity(req.currentCity());
        if (req.currentState() != null) candidate.setCurrentState(req.currentState());
        if (req.currentCountry() != null) candidate.setCurrentCountry(req.currentCountry());

        if (req.skillIds() != null) {
            Set<Skill> skills = new HashSet<>(skillRepo.findAllById(req.skillIds()));
            candidate.setSkills(skills);
        }

        candidateRepo.save(candidate);
        log.info("Candidate profile updated: {}", email);

        // Recalculate match history when candidate updates profile (Requirement 25)
        try {
            matchingService.recalculateForCandidate(candidate);
        } catch (Exception e) {
            log.warn("Auto-recalculation after profile update had non-fatal warning: {}", e.getMessage());
        }

        return toResponse(candidate);
    }

    @Transactional
    public void delete(String email) {
        Candidate candidate = getByEmail(email);
        candidateRepo.delete(candidate);
        log.info("Candidate deleted: {}", email);
    }

    @Transactional
    public CandidateResponse updateResume(String email, String fileName, String resumeText) {
        Candidate candidate = getByEmail(email);
        candidate.setResumeFileName(fileName);
        candidate.setResumeText(resumeText);
        candidateRepo.save(candidate);
        log.info("Resume updated for: {}", email);

        // Recalculate matches when resume changes (Requirement 25)
        try {
            matchingService.recalculateForCandidate(candidate);
        } catch (Exception e) {
            log.warn("Auto-recalculation after resume update had non-fatal warning: {}", e.getMessage());
        }

        return toResponse(candidate);
    }
}
