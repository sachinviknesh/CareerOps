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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CandidateService {

    private static final Logger log = LoggerFactory.getLogger(CandidateService.class);

    private final CandidateRepository candidateRepo;
    private final SkillRepository skillRepo;

    public CandidateService(CandidateRepository candidateRepo, SkillRepository skillRepo) {
        this.candidateRepo = candidateRepo;
        this.skillRepo = skillRepo;
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
                skillResponses, c.getCreatedAt(), c.getUpdatedAt()
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

        if (req.skillIds() != null) {
            Set<Skill> skills = new HashSet<>(skillRepo.findAllById(req.skillIds()));
            candidate.setSkills(skills);
        }

        candidateRepo.save(candidate);
        log.info("Candidate updated: {}", email);
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
        return toResponse(candidate);
    }
}
