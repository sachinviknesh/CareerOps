package com.careerops.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "opportunity", indexes = {
        @Index(name = "idx_opp_type", columnList = "type"),
        @Index(name = "idx_opp_source_extid", columnList = "source, external_job_id"),
        @Index(name = "idx_opp_norm_title_org", columnList = "normalized_title, organization"),
        @Index(name = "idx_opp_city_state", columnList = "city, state"),
        @Index(name = "idx_opp_deadline", columnList = "deadline"),
        @Index(name = "idx_opp_event_date", columnList = "event_start_date")
})
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    private String organization;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, columnDefinition = "varchar(50)")
    private OpportunityType type;

    @Column(name = "role_family")
    private String roleFamily;

    @Size(max = 10000)
    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country = "India";

    private String venue;

    @Column(name = "work_mode", length = 30)
    private String workMode; // Online, Offline, Hybrid

    @Column(columnDefinition = "TEXT")
    private String eligibility;

    @Column(name = "preferred_skills", columnDefinition = "TEXT")
    private String preferredSkills;

    @Column(name = "experience_requirement")
    private String experienceRequirement;

    @Column(name = "education_requirement")
    private String educationRequirement;

    private String url;

    private LocalDate deadline;

    @Column(name = "registration_deadline")
    private LocalDate registrationDeadline;

    @Column(name = "event_start_date")
    private LocalDate eventStartDate;

    @Column(name = "event_end_date")
    private LocalDate eventEndDate;

    @Column(name = "posted_at")
    private LocalDate postedAt;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 50)
    private String status = "ACTIVE";

    @Column(length = 50)
    private String source = "MANUAL";

    @Column(name = "external_job_id")
    private String externalJobId;

    @Column(name = "normalized_title")
    private String normalizedTitle;

    private Double latitude;

    private Double longitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "opportunity_skill",
            joinColumns = @JoinColumn(name = "opportunity_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> requiredSkills = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (title != null) {
            normalizedTitle = title.trim().toLowerCase();
        }
        if (source == null || source.isBlank()) {
            source = "MANUAL";
        }
        if (country == null || country.isBlank()) {
            country = "India";
        }
        if (status == null || status.isBlank()) {
            status = "ACTIVE";
        }
        if (deadline == null && registrationDeadline != null) {
            deadline = registrationDeadline;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (title != null) {
            normalizedTitle = title.trim().toLowerCase();
        }
        if (deadline == null && registrationDeadline != null) {
            deadline = registrationDeadline;
        }
    }

    public Opportunity() {}

    public Opportunity(String title, String organization, OpportunityType type, String roleFamily, String description) {
        this.title = title;
        this.organization = organization;
        this.type = type;
        this.roleFamily = roleFamily;
        this.description = description;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        if (title != null) {
            this.normalizedTitle = title.trim().toLowerCase();
        }
    }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public OpportunityType getType() { return type; }
    public void setType(OpportunityType type) { this.type = type; }

    public String getRoleFamily() { return roleFamily; }
    public void setRoleFamily(String roleFamily) { this.roleFamily = roleFamily; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }

    public String getEligibility() { return eligibility; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }

    public String getPreferredSkills() { return preferredSkills; }
    public void setPreferredSkills(String preferredSkills) { this.preferredSkills = preferredSkills; }

    public OpportunityType getOpportunityType() { return type; }
    public void setOpportunityType(OpportunityType type) { this.type = type; }

    public String getExternalId() { return externalJobId; }
    public void setExternalId(String externalId) { this.externalJobId = externalId; }

    public String getExperienceRequirement() { return experienceRequirement; }
    public void setExperienceRequirement(String experienceRequirement) { this.experienceRequirement = experienceRequirement; }
    public void setExperienceRequirement(Integer experienceRequirement) {
        this.experienceRequirement = experienceRequirement != null ? String.valueOf(experienceRequirement) : null;
    }

    public Integer getExperienceYearsRequirement() {
        if (experienceRequirement == null) return null;
        try {
            return Integer.parseInt(experienceRequirement.trim());
        } catch (Exception e) {
            return null;
        }
    }

    public String getEducationRequirement() { return educationRequirement; }
    public void setEducationRequirement(String educationRequirement) { this.educationRequirement = educationRequirement; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public LocalDate getRegistrationDeadline() { return registrationDeadline != null ? registrationDeadline : deadline; }
    public void setRegistrationDeadline(LocalDate registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
        if (this.deadline == null) this.deadline = registrationDeadline;
    }

    public LocalDate getEventStartDate() { return eventStartDate; }
    public void setEventStartDate(LocalDate eventStartDate) { this.eventStartDate = eventStartDate; }

    public LocalDate getEventEndDate() { return eventEndDate; }
    public void setEventEndDate(LocalDate eventEndDate) { this.eventEndDate = eventEndDate; }

    public LocalDate getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDate postedAt) { this.postedAt = postedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getExternalJobId() { return externalJobId; }
    public void setExternalJobId(String externalJobId) { this.externalJobId = externalJobId; }

    public String getNormalizedTitle() { return normalizedTitle; }
    public void setNormalizedTitle(String normalizedTitle) { this.normalizedTitle = normalizedTitle; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Application> applications = new java.util.ArrayList<>();

    public java.util.List<Application> getApplications() { return applications; }
    public void setApplications(java.util.List<Application> applications) { this.applications = applications; }

    public String getExternalUrl() { return url; }
    public void setExternalUrl(String externalUrl) { this.url = externalUrl; }

    public Set<Skill> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(Set<Skill> requiredSkills) { this.requiredSkills = requiredSkills; }
}
