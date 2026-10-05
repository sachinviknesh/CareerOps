package com.careerops.app.service;

import com.careerops.app.dto.opportunity.OpportunityRequest;
import com.careerops.app.dto.opportunity.OpportunityResponse;
import com.careerops.app.dto.skill.SkillResponse;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;
import com.careerops.app.entity.Skill;
import com.careerops.app.exception.ResourceNotFoundException;
import com.careerops.app.repository.OpportunityRepository;
import com.careerops.app.repository.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OpportunityService {

    private static final Logger log = LoggerFactory.getLogger(OpportunityService.class);

    private final OpportunityRepository opportunityRepo;
    private final SkillRepository skillRepo;

    public OpportunityService(OpportunityRepository opportunityRepo, SkillRepository skillRepo) {
        this.opportunityRepo = opportunityRepo;
        this.skillRepo = skillRepo;
    }

    public Page<OpportunityResponse> findAll(String search,
                                            OpportunityType type,
                                            String roleFamily,
                                            String skill,
                                            Pageable pageable) {
        Page<Opportunity> page;

        if (skill != null && !skill.isBlank()) {
            page = opportunityRepo.findByRequiredSkill(skill.trim(), pageable);
        } else if (type != null && search != null && !search.isBlank()) {
            page = opportunityRepo.findByTypeAndTitleContainingIgnoreCase(type, search.trim(), pageable);
        } else if (type != null) {
            page = opportunityRepo.findByType(type, pageable);
        } else if (roleFamily != null && !roleFamily.isBlank()) {
            page = opportunityRepo.findByRoleFamilyContainingIgnoreCase(roleFamily.trim(), pageable);
        } else if (search != null && !search.isBlank()) {
            page = opportunityRepo.findByTitleContainingIgnoreCase(search.trim(), pageable);
        } else {
            page = opportunityRepo.findAll(pageable);
        }

        return page.map(this::toResponse);
    }

    public Opportunity getEntityById(Long id) {
        return opportunityRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));
    }

    public OpportunityResponse findById(Long id) {
        return toResponse(getEntityById(id));
    }

    public List<OpportunityResponse> findClosingSoon(int days) {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(days > 0 ? days : 14);
        return opportunityRepo.findClosingSoon(today, cutoff).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OpportunityResponse create(OpportunityRequest req) {
        Opportunity opp = new Opportunity();
        opp.setTitle(req.title().trim());
        opp.setOrganization(req.organization() != null ? req.organization().trim() : null);
        opp.setType(req.type());
        opp.setRoleFamily(req.roleFamily() != null ? req.roleFamily().trim() : null);
        opp.setDescription(req.description());
        opp.setLocation(req.location() != null ? req.location().trim() : null);
        opp.setWorkMode(req.workMode() != null ? req.workMode().trim() : null);
        opp.setUrl(req.url() != null ? req.url().trim() : null);
        opp.setDeadline(req.deadline());
        opp.setPostedAt(req.postedAt() != null ? req.postedAt() : LocalDate.now());
        opp.setActive(req.active() != null ? req.active() : true);

        if (req.requiredSkillIds() != null && !req.requiredSkillIds().isEmpty()) {
            Set<Skill> skills = new HashSet<>(skillRepo.findAllById(req.requiredSkillIds()));
            opp.setRequiredSkills(skills);
        }

        Opportunity saved = opportunityRepo.save(opp);
        log.info("Opportunity created: '{}' at '{}' (id: {})", saved.getTitle(), saved.getOrganization(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public OpportunityResponse update(Long id, OpportunityRequest req) {
        Opportunity opp = getEntityById(id);

        opp.setTitle(req.title().trim());
        opp.setOrganization(req.organization() != null ? req.organization().trim() : null);
        opp.setType(req.type());
        opp.setRoleFamily(req.roleFamily() != null ? req.roleFamily().trim() : null);
        opp.setDescription(req.description());
        opp.setLocation(req.location() != null ? req.location().trim() : null);
        opp.setWorkMode(req.workMode() != null ? req.workMode().trim() : null);
        opp.setUrl(req.url() != null ? req.url().trim() : null);
        opp.setDeadline(req.deadline());
        if (req.postedAt() != null) opp.setPostedAt(req.postedAt());
        if (req.active() != null) opp.setActive(req.active());

        if (req.requiredSkillIds() != null) {
            Set<Skill> skills = new HashSet<>(skillRepo.findAllById(req.requiredSkillIds()));
            opp.setRequiredSkills(skills);
        }

        Opportunity updated = opportunityRepo.save(opp);
        log.info("Opportunity updated: '{}' (id: {})", updated.getTitle(), updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Opportunity opp = getEntityById(id);
        opportunityRepo.delete(opp);
        log.info("Opportunity deleted: id {}", id);
    }

    public OpportunityResponse toResponse(Opportunity o) {
        Set<SkillResponse> skillResponses = o.getRequiredSkills().stream()
                .map(s -> new SkillResponse(s.getId(), s.getName(), s.getCategory(), s.getCreatedAt()))
                .collect(Collectors.toSet());

        return new OpportunityResponse(
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
                o.getPostedAt(),
                o.isActive(),
                skillResponses,
                o.getCreatedAt(),
                o.getUpdatedAt()
        );
    }
}
