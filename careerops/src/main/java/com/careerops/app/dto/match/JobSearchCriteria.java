package com.careerops.app.dto.match;

public record JobSearchCriteria(
        String keyword,
        String location,
        String workMode,
        String jobType,
        Integer limit
) {
    public static JobSearchCriteria defaults() {
        return new JobSearchCriteria(null, null, null, null, 25);
    }
}
