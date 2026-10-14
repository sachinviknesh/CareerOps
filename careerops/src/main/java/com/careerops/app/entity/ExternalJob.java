package com.careerops.app.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_job", indexes = {
        @Index(name = "idx_extjob_source_id", columnList = "source, external_id", unique = true),
        @Index(name = "idx_extjob_norm_title_company", columnList = "normalized_title, company")
})
public class ExternalJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "external_id", nullable = false, length = 100)
    private String externalId;

    @Column(nullable = false)
    private String title;

    @Column(name = "normalized_title")
    private String normalizedTitle;

    private String company;

    private String location;

    @Column(name = "work_mode", length = 30)
    private String workMode;

    @Column(name = "job_type", length = 30)
    private String jobType;

    @Column(name = "raw_description", columnDefinition = "TEXT")
    private String rawDescription;

    private String url;

    @Column(name = "posted_date")
    private LocalDate postedDate;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(length = 30)
    private String status = "IMPORTED";

    @Column(name = "opportunity_id")
    private Long opportunityId;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt = LocalDateTime.now();

    public ExternalJob() {}

    public ExternalJob(String source, String externalId, String title, String company, String location,
                       String workMode, String jobType, String rawDescription, String url,
                       LocalDate postedDate, LocalDate deadline) {
        this.source = source;
        this.externalId = externalId;
        this.title = title;
        this.normalizedTitle = title != null ? title.trim().toLowerCase() : null;
        this.company = company;
        this.location = location;
        this.workMode = workMode;
        this.jobType = jobType;
        this.rawDescription = rawDescription;
        this.url = url;
        this.postedDate = postedDate != null ? postedDate : LocalDate.now();
        this.deadline = deadline;
        this.fetchedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        this.normalizedTitle = title != null ? title.trim().toLowerCase() : null;
    }

    public String getNormalizedTitle() { return normalizedTitle; }
    public void setNormalizedTitle(String normalizedTitle) { this.normalizedTitle = normalizedTitle; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getRawDescription() { return rawDescription; }
    public void setRawDescription(String rawDescription) { this.rawDescription = rawDescription; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public LocalDate getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }

    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
}
