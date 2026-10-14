package com.careerops.app.service.jobsource;

import com.careerops.app.dto.match.ExternalJobDto;
import com.careerops.app.dto.match.JobSearchCriteria;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;

import java.util.List;

public interface JobSourceProvider extends OpportunitySourceProvider {

    List<ExternalJobDto> fetchJobs();

    List<ExternalJobDto> searchJobs(JobSearchCriteria criteria);

    Opportunity normalizeJob(ExternalJobDto externalJob);

    @Override
    default List<ExternalJobDto> fetch() {
        return fetchJobs();
    }

    @Override
    default List<ExternalJobDto> search(JobSearchCriteria criteria) {
        return searchJobs(criteria);
    }

    @Override
    default Opportunity normalize(ExternalJobDto raw) {
        return normalizeJob(raw);
    }

    @Override
    default boolean validate(ExternalJobDto raw) {
        return raw != null && raw.title() != null && !raw.title().isBlank()
                && raw.company() != null && !raw.company().isBlank();
    }

    @Override
    default boolean supportsOpportunityType(OpportunityType type) {
        return true;
    }

    @Override
    default String getSourceType() {
        return "EVENT_PLATFORM";
    }

    @Override
    default String getBaseUrl() {
        return "https://careerops.platform";
    }
}
