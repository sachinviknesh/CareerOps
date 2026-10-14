package com.careerops.app.service.jobsource;

import com.careerops.app.dto.match.ExternalJobDto;
import com.careerops.app.dto.match.JobSearchCriteria;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;

import java.util.List;

public interface OpportunitySourceProvider {

    String getSourceCode();

    String getSourceName();

    String getSourceType(); // COLLEGE_WEBSITE, UNIVERSITY_WEBSITE, EVENT_PLATFORM, API, RSS, OFFICIAL_PORTAL, OTHER

    String getBaseUrl();

    boolean isEnabled();

    List<ExternalJobDto> fetch();

    List<ExternalJobDto> search(JobSearchCriteria criteria);

    Opportunity normalize(ExternalJobDto raw);

    boolean validate(ExternalJobDto raw);

    boolean supportsOpportunityType(OpportunityType type);
}
