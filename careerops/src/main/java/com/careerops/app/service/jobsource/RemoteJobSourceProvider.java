package com.careerops.app.service.jobsource;

import com.careerops.app.dto.match.ExternalJobDto;
import com.careerops.app.dto.match.JobSearchCriteria;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Component
public class RemoteJobSourceProvider implements JobSourceProvider {

    private static final Logger log = LoggerFactory.getLogger(RemoteJobSourceProvider.class);

    public static final String SOURCE_CODE = "REMOTE_API";
    public static final String SOURCE_NAME = "External Authorized Job API Feed";

    @Value("${jobs.sources.remote.enabled:false}")
    private boolean enabled;

    @Value("${jobs.sources.remote.base-url:}")
    private String baseUrl;

    @Value("${jobs.sources.remote.api-key:}")
    private String apiKey;

    private final HttpClient httpClient;

    public RemoteJobSourceProvider() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String getSourceCode() {
        return SOURCE_CODE;
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public boolean isEnabled() {
        return enabled && baseUrl != null && !baseUrl.isBlank();
    }

    @Override
    public List<ExternalJobDto> fetchJobs() {
        return searchJobs(JobSearchCriteria.defaults());
    }

    @Override
    public List<ExternalJobDto> searchJobs(JobSearchCriteria criteria) {
        if (!isEnabled()) {
            log.debug("Remote job source is disabled or baseUrl is not configured.");
            return List.of();
        }

        try {
            String url = baseUrl + (baseUrl.contains("?") ? "&" : "?") + "format=json";
            if (criteria != null && criteria.keyword() != null && !criteria.keyword().isBlank()) {
                url += "&q=" + java.net.URLEncoder.encode(criteria.keyword(), java.nio.charset.StandardCharsets.UTF_8);
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json");

            if (apiKey != null && !apiKey.isBlank()) {
                builder.header("Authorization", "Bearer " + apiKey);
            }

            HttpResponse<String> response = httpClient.send(builder.GET().build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Successfully received response from remote job feed: {}", baseUrl);
                return List.of();
            } else {
                log.warn("Remote job source returned status {}", response.statusCode());
                return List.of();
            }
        } catch (Exception e) {
            log.error("Failed to communicate with remote job source {}: {}", baseUrl, e.getMessage());
            return List.of();
        }
    }

    @Override
    public Opportunity normalizeJob(ExternalJobDto externalJob) {
        Opportunity opp = new Opportunity();
        opp.setTitle(externalJob.title().trim());
        opp.setOrganization(externalJob.company() != null ? externalJob.company().trim() : "Unknown");
        opp.setType(OpportunityType.JOB);
        opp.setDescription(externalJob.description());
        opp.setLocation(externalJob.location());
        opp.setWorkMode(externalJob.workMode());
        opp.setUrl(externalJob.url());
        opp.setPostedAt(externalJob.postedDate() != null ? externalJob.postedDate() : LocalDate.now());
        opp.setDeadline(externalJob.deadline() != null ? externalJob.deadline() : LocalDate.now().plusDays(30));
        opp.setActive(true);
        opp.setSource(SOURCE_CODE);
        opp.setExternalJobId(externalJob.externalId());
        opp.setNormalizedTitle(externalJob.title().trim().toLowerCase());
        opp.setRoleFamily("Software Engineering");
        return opp;
    }
}
