package com.careerops.app.dto.skill;

import java.time.LocalDateTime;

public record SkillResponse(Long id, String name, String category, LocalDateTime createdAt) {}
