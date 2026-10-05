package com.careerops.app.dto.application;

import com.careerops.app.dto.opportunity.OpportunityResponse;
import com.careerops.app.entity.ApplicationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long candidateId,
        OpportunityResponse opportunity,
        ApplicationStatus status,
        LocalDate appliedAt,
        LocalDate nextActionDate,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
