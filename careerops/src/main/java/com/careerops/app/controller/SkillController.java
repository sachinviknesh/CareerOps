package com.careerops.app.controller;

import com.careerops.app.dto.skill.SkillRequest;
import com.careerops.app.dto.skill.SkillResponse;
import com.careerops.app.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@Tag(name = "Skills", description = "Skill catalog management with duplicate detection")
@SecurityRequirement(name = "bearerAuth")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    @Operation(summary = "List all skills", description = "Retrieves all available skills ordered alphabetically")
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        return ResponseEntity.ok(skillService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill by ID", description = "Retrieves a single skill record")
    public ResponseEntity<SkillResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create skill", description = "Creates a new skill name and category")
    public ResponseEntity<SkillResponse> create(@Valid @RequestBody SkillRequest req) {
        SkillResponse created = skillService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update skill", description = "Updates a skill's name and category")
    public ResponseEntity<SkillResponse> update(@PathVariable Long id, @Valid @RequestBody SkillRequest req) {
        return ResponseEntity.ok(skillService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete skill", description = "Removes a skill from the catalog")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
