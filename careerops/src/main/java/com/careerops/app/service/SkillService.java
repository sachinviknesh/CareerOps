package com.careerops.app.service;

import com.careerops.app.dto.skill.SkillRequest;
import com.careerops.app.dto.skill.SkillResponse;
import com.careerops.app.entity.Skill;
import com.careerops.app.exception.BadRequestException;
import com.careerops.app.exception.ResourceNotFoundException;
import com.careerops.app.repository.SkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillService {

    private static final Logger log = LoggerFactory.getLogger(SkillService.class);

    private final SkillRepository skillRepo;

    public SkillService(SkillRepository skillRepo) {
        this.skillRepo = skillRepo;
    }

    public List<SkillResponse> findAll() {
        return skillRepo.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(this::toResponse)
                .toList();
    }

    public SkillResponse findById(Long id) {
        return skillRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
    }

    @Transactional
    public SkillResponse create(SkillRequest req) {
        String trimmedName = req.name().trim();
        if (skillRepo.existsByNameIgnoreCase(trimmedName)) {
            log.warn("Attempt to create duplicate skill: {}", trimmedName);
            throw new BadRequestException("A skill with name '" + trimmedName + "' already exists.");
        }

        Skill skill = new Skill();
        skill.setName(trimmedName);
        skill.setCategory(req.category() != null && !req.category().isBlank() ? req.category().trim() : "Other");

        Skill saved = skillRepo.save(skill);
        log.info("Skill created: {} (id: {})", saved.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public SkillResponse update(Long id, SkillRequest req) {
        Skill skill = skillRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));

        String trimmedName = req.name().trim();
        if (!skill.getName().equalsIgnoreCase(trimmedName) && skillRepo.existsByNameIgnoreCase(trimmedName)) {
            throw new BadRequestException("Another skill with name '" + trimmedName + "' already exists.");
        }

        skill.setName(trimmedName);
        if (req.category() != null && !req.category().isBlank()) {
            skill.setCategory(req.category().trim());
        }

        Skill updated = skillRepo.save(skill);
        log.info("Skill updated: {} (id: {})", updated.getName(), updated.getId());
        return toResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        Skill skill = skillRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
        skillRepo.delete(skill);
        log.info("Skill deleted: {} (id: {})", skill.getName(), id);
    }

    public SkillResponse toResponse(Skill s) {
        return new SkillResponse(s.getId(), s.getName(), s.getCategory(), s.getCreatedAt());
    }
}
