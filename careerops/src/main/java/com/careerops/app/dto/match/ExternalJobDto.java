package com.careerops.app.dto.match;

import java.time.LocalDate;
import java.util.List;

public record ExternalJobDto(
        String source,
        String externalId,
        String title,
        String company,
        String location,
        String workMode,
        String jobType,
        String description,
        String url,
        LocalDate postedDate,
        LocalDate deadline,
        List<String> requiredSkills,
        String city,
        String state,
        String country,
        String venue,
        String eligibility,
        String preferredSkills,
        Integer experienceRequirement,
        String educationRequirement,
        LocalDate registrationDeadline,
        LocalDate eventStartDate,
        LocalDate eventEndDate
) {
    public ExternalJobDto(
            String source,
            String externalId,
            String title,
            String company,
            String location,
            String workMode,
            String jobType,
            String description,
            String url,
            LocalDate postedDate,
            LocalDate deadline,
            List<String> requiredSkills
    ) {
        this(source, externalId, title, company, location, workMode, jobType, description, url,
                postedDate, deadline, requiredSkills, null, null, "India", null, null, null,
                null, null, deadline, null, null);
    }
}
