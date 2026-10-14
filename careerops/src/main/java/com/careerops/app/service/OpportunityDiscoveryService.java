package com.careerops.app.service;

import com.careerops.app.dto.match.ExternalJobDto;
import com.careerops.app.dto.match.JobImportResponse;
import com.careerops.app.dto.match.JobSearchCriteria;
import com.careerops.app.entity.*;
import com.careerops.app.repository.*;
import com.careerops.app.service.jobsource.OpportunitySourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class OpportunityDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(OpportunityDiscoveryService.class);

    private final List<OpportunitySourceProvider> providers;
    private final JobSourceRepository sourceRepo;
    private final ExternalJobRepository externalJobRepo;
    private final OpportunityRepository opportunityRepo;
    private final SkillRepository skillRepo;
    private final JobMatchingService matchingService;
    private final NotificationService notificationService;
    private final CandidateRepository candidateRepo;

    public OpportunityDiscoveryService(List<OpportunitySourceProvider> providers,
                                       JobSourceRepository sourceRepo,
                                       ExternalJobRepository externalJobRepo,
                                       OpportunityRepository opportunityRepo,
                                       SkillRepository skillRepo,
                                       JobMatchingService matchingService,
                                       NotificationService notificationService,
                                       CandidateRepository candidateRepo) {
        this.providers = providers;
        this.sourceRepo = sourceRepo;
        this.externalJobRepo = externalJobRepo;
        this.opportunityRepo = opportunityRepo;
        this.skillRepo = skillRepo;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
        this.candidateRepo = candidateRepo;
    }

    public JobImportResponse runDiscoveryPipeline(Candidate candidate, JobSearchCriteria criteria) {
        log.info("Starting Central Opportunity Discovery Pipeline across {} providers", providers.size());

        int totalFetched = 0;
        int importedCount = 0;
        int duplicateCount = 0;
        int matchedCount = 0;
        List<String> sourcesUsed = new ArrayList<>();
        List<Opportunity> newlyDiscoveredOpps = new ArrayList<>();

        for (OpportunitySourceProvider provider : providers) {
            // STEP 1: SOURCE CHECK
            if (!provider.isEnabled()) {
                log.info("Source {} is disabled, skipping.", provider.getSourceCode());
                continue;
            }

            sourcesUsed.add(provider.getSourceCode());
            JobSource sourceEntity = sourceRepo.findByCode(provider.getSourceCode())
                    .orElseGet(() -> new JobSource(provider.getSourceCode(), provider.getSourceName(), true, provider.getBaseUrl(),
                            provider.getSourceType(), "JOB,INTERNSHIP,HACKATHON,CTF_COMPETITION,FELLOWSHIP"));

            try {
                // STEP 2: FETCH
                List<ExternalJobDto> rawOpportunities = (criteria != null)
                        ? provider.search(criteria)
                        : provider.fetch();
                totalFetched += rawOpportunities.size();
                sourceEntity.setLastChecked(LocalDateTime.now());
                sourceEntity.setStatus("ACTIVE");
                sourceEntity.setLastError(null);

                for (ExternalJobDto raw : rawOpportunities) {
                    // STEP 3: VALIDATE
                    if (!provider.validate(raw)) {
                        log.debug("Validation failed for opportunity item: {}", raw);
                        continue;
                    }

                    // STEP 4: DEDUPLICATE (Source + ExternalId, Canonical URL, Normalized Title + Org)
                    if (isDuplicate(raw)) {
                        duplicateCount++;
                        continue;
                    }

                    // STEP 5: NORMALIZE
                    Opportunity opp = provider.normalize(raw);

                    // Map or associate required skills
                    if (raw.requiredSkills() != null && !raw.requiredSkills().isEmpty()) {
                        Set<Skill> skills = new HashSet<>();
                        for (String skillName : raw.requiredSkills()) {
                            String cleanName = skillName.trim();
                            if (!cleanName.isEmpty()) {
                                Skill skill = skillRepo.findByNameIgnoreCase(cleanName)
                                        .orElseGet(() -> skillRepo.save(new Skill(cleanName, "General")));
                                skills.add(skill);
                            }
                        }
                        opp.setRequiredSkills(skills);
                    }

                    // STEP 6: STORE
                    Opportunity savedOpp = opportunityRepo.save(opp);
                    importedCount++;
                    newlyDiscoveredOpps.add(savedOpp);

                    // Track provenance
                    ExternalJob extJob = new ExternalJob(
                            raw.source(), raw.externalId(), raw.title(), raw.company(),
                            raw.location(), raw.workMode(), raw.jobType(), raw.description(),
                            raw.url(), raw.postedDate(), raw.deadline()
                    );
                    extJob.setOpportunityId(savedOpp.getId());
                    extJob.setStatus("IMPORTED");
                    externalJobRepo.save(extJob);
                }

                sourceEntity.setTotalImported(sourceEntity.getTotalImported() + newlyDiscoveredOpps.size());
                sourceEntity.setLastSyncAt(LocalDateTime.now());
                sourceRepo.save(sourceEntity);

            } catch (Exception e) {
                log.error("Discovery error from source {}: {}", provider.getSourceCode(), e.getMessage());
                sourceEntity.setStatus("ERROR");
                sourceEntity.setLastError(e.getMessage());
                sourceEntity.setLastChecked(LocalDateTime.now());
                sourceRepo.save(sourceEntity);
            }
        }

        // STEP 7-10: ELIGIBILITY CHECK, FIT ENGINE, LOCATION ANALYSIS, RANKING & NOTIFICATIONS
        List<Candidate> candidatesToNotify = (candidate != null)
                ? List.of(candidate)
                : candidateRepo.findAll();

        List<Opportunity> allActiveOpportunities = opportunityRepo.findAllActive();

        for (Candidate cand : candidatesToNotify) {
            for (Opportunity opp : allActiveOpportunities) {
                // Evaluates Eligibility, Fit Engine, Location & Travel, then stores JobMatch
                JobMatch match = matchingService.computeAndSaveMatch(cand, opp);
                matchedCount++;

                // STEP 11: NOTIFICATION PIPELINE
                // Trigger location-aware alerts for high-fit/recent opportunities
                notificationService.processMatchForNotification(cand, match);
            }
        }

        String msg = String.format("Opportunity discovery pipeline executed successfully. Fetched: %d, Discovered: %d, Duplicates: %d, Matches Evaluated: %d.",
                totalFetched, importedCount, duplicateCount, matchedCount);
        log.info(msg);

        return new JobImportResponse(totalFetched, importedCount, duplicateCount, matchedCount, sourcesUsed, msg);
    }

    private boolean isDuplicate(ExternalJobDto dto) {
        if (dto.externalId() != null &&
                (opportunityRepo.existsBySourceAndExternalJobId(dto.source(), dto.externalId())
                        || externalJobRepo.existsBySourceAndExternalId(dto.source(), dto.externalId()))) {
            return true;
        }

        String normTitle = dto.title() != null ? dto.title().trim().toLowerCase() : "";
        String company = dto.company() != null ? dto.company().trim() : "";
        if (opportunityRepo.existsByNormalizedTitleAndOrganizationIgnoreCase(normTitle, company)
                || externalJobRepo.existsByNormalizedTitleAndCompanyIgnoreCase(normTitle, company)) {
            return true;
        }

        return false;
    }
}
