package com.careerops.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_source")
public class JobSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "source_type", length = 50)
    private String sourceType = "EVENT_PLATFORM"; // COLLEGE_WEBSITE, UNIVERSITY_WEBSITE, EVENT_PLATFORM, API, RSS, OFFICIAL_PORTAL, OTHER

    @Column(length = 30)
    private String status = "ACTIVE"; // ACTIVE, DISABLED, ERROR

    @Column(name = "supported_opportunity_types", length = 500)
    private String supportedOpportunityTypes = "JOB,INTERNSHIP,HACKATHON,CTF_COMPETITION,FELLOWSHIP,CODING_COMPETITION";

    @Column(name = "last_checked")
    private LocalDateTime lastChecked;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "total_imported")
    private int totalImported = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public JobSource() {}

    public JobSource(String code, String name, boolean enabled, String baseUrl) {
        this.code = code;
        this.name = name;
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.status = enabled ? "ACTIVE" : "DISABLED";
        this.createdAt = LocalDateTime.now();
    }

    public JobSource(String code, String name, boolean enabled, String baseUrl, String sourceType, String supportedOpportunityTypes) {
        this.code = code;
        this.name = name;
        this.enabled = enabled;
        this.baseUrl = baseUrl;
        this.sourceType = sourceType;
        this.supportedOpportunityTypes = supportedOpportunityTypes;
        this.status = enabled ? "ACTIVE" : "DISABLED";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.status = enabled ? "ACTIVE" : "DISABLED";
    }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSupportedOpportunityTypes() { return supportedOpportunityTypes; }
    public void setSupportedOpportunityTypes(String supportedOpportunityTypes) { this.supportedOpportunityTypes = supportedOpportunityTypes; }

    public LocalDateTime getLastChecked() { return lastChecked; }
    public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }

    public int getTotalImported() { return totalImported; }
    public void setTotalImported(int totalImported) { this.totalImported = totalImported; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
