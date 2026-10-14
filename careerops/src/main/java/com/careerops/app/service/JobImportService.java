package com.careerops.app.service;

import com.careerops.app.dto.match.JobImportResponse;
import com.careerops.app.dto.match.JobSearchCriteria;
import com.careerops.app.entity.Candidate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JobImportService {

    private final OpportunityDiscoveryService discoveryService;

    public JobImportService(OpportunityDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public JobImportResponse importJobs(Candidate candidate, JobSearchCriteria criteria) {
        return discoveryService.runDiscoveryPipeline(candidate, criteria);
    }
}
