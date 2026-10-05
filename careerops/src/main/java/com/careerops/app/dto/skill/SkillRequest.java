package com.careerops.app.dto.skill;

import jakarta.validation.constraints.NotBlank;

public record SkillRequest(
        @NotBlank(message = "Skill name is required") String name,
        String category
) {}
