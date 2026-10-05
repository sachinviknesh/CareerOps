package com.careerops.app.dto.application;

import com.careerops.app.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ApplicationRequest(
        Long opportunityId,
        @NotNull(message = "status is required") ApplicationStatus status,
        LocalDate appliedAt,
        LocalDate nextActionDate,
        String notes
) {}
