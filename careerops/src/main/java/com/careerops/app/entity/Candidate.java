package com.careerops.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "candidate")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private String headline;

    private String location;

    @Column(length = 100)
    private String currentCity;

    @Column(length = 100)
    private String currentState;

    @Column(length = 100)
    private String currentCountry = "India";

    @Column(columnDefinition = "TEXT")
    private String education;

    @Column(length = 150)
    private String degree;

    @Column(name = "university_or_college", length = 200)
    private String universityOrCollege;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "target_roles", columnDefinition = "TEXT")
    private String targetRoles;

    @Column(name = "career_interests", columnDefinition = "TEXT")
    private String careerInterests;

    @Column(columnDefinition = "TEXT")
    private String projects;

    @Column(columnDefinition = "TEXT")
    private String certifications;

    @Column(name = "preferred_opportunity_types", length = 255)
    private String preferredOpportunityTypes; // Comma separated: JOB,INTERNSHIP,HACKATHON,CTF_COMPETITION

    @Column(name = "preferred_states", length = 255)
    private String preferredStates;

    @Column(name = "preferred_cities", length = 255)
    private String preferredCities;

    @Column(name = "preferred_work_mode", length = 50)
    private String preferredWorkMode = "Any"; // Online, Offline, Hybrid, Any

    @Column(name = "willing_to_travel")
    private Boolean willingToTravel = true;

    @Column(name = "max_travel_distance_km")
    private Integer maxTravelDistanceKm = 500;

    @Column(name = "resume_file_name")
    private String resumeFileName;

    @Column(name = "resume_text", columnDefinition = "MEDIUMTEXT")
    private String resumeText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "candidate_skill",
            joinColumns = @JoinColumn(name = "candidate_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currentCountry == null || currentCountry.isBlank()) {
            currentCountry = "India";
        }
        if (willingToTravel == null) {
            willingToTravel = true;
        }
        if (maxTravelDistanceKm == null) {
            maxTravelDistanceKm = 500;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Candidate() {}

    public Candidate(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }

    public String getLocation() { return location; }
    public void setLocation(String location) {
        this.location = location;
        if (location != null && currentCity == null) {
            // Heuristic extraction for single city input
            String[] parts = location.split(",");
            this.currentCity = parts[0].trim();
            if (parts.length > 1) this.currentState = parts[1].trim();
        }
    }

    public String getCurrentCity() { return currentCity != null ? currentCity : (location != null ? location.split(",")[0].trim() : null); }
    public void setCurrentCity(String currentCity) { this.currentCity = currentCity; }

    public String getCurrentState() { return currentState; }
    public void setCurrentState(String currentState) { this.currentState = currentState; }

    public String getCurrentCountry() { return currentCountry; }
    public void setCurrentCountry(String currentCountry) { this.currentCountry = currentCountry; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public String getUniversityOrCollege() { return universityOrCollege; }
    public void setUniversityOrCollege(String universityOrCollege) { this.universityOrCollege = universityOrCollege; }

    public Integer getGraduationYear() { return graduationYear; }
    public void setGraduationYear(Integer graduationYear) { this.graduationYear = graduationYear; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }

    public String getTargetRoles() { return targetRoles; }
    public void setTargetRoles(String targetRoles) { this.targetRoles = targetRoles; }

    public String getCareerInterests() { return careerInterests; }
    public void setCareerInterests(String careerInterests) { this.careerInterests = careerInterests; }

    public String getProjects() { return projects; }
    public void setProjects(String projects) { this.projects = projects; }

    public String getCertifications() { return certifications; }
    public void setCertifications(String certifications) { this.certifications = certifications; }

    public String getPreferredOpportunityTypes() { return preferredOpportunityTypes; }
    public void setPreferredOpportunityTypes(String preferredOpportunityTypes) { this.preferredOpportunityTypes = preferredOpportunityTypes; }

    public String getPreferredStates() { return preferredStates; }
    public void setPreferredStates(String preferredStates) { this.preferredStates = preferredStates; }

    public String getPreferredCities() { return preferredCities; }
    public void setPreferredCities(String preferredCities) { this.preferredCities = preferredCities; }

    public String getPreferredWorkMode() { return preferredWorkMode; }
    public void setPreferredWorkMode(String preferredWorkMode) { this.preferredWorkMode = preferredWorkMode; }

    public Boolean getWillingToTravel() { return willingToTravel != null ? willingToTravel : true; }
    public void setWillingToTravel(Boolean willingToTravel) { this.willingToTravel = willingToTravel; }

    public Integer getMaxTravelDistanceKm() { return maxTravelDistanceKm != null ? maxTravelDistanceKm : 500; }
    public void setMaxTravelDistanceKm(Integer maxTravelDistanceKm) { this.maxTravelDistanceKm = maxTravelDistanceKm; }

    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }

    public String getResumeText() { return resumeText; }
    public void setResumeText(String resumeText) { this.resumeText = resumeText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public boolean isWillingToTravel() { return Boolean.TRUE.equals(willingToTravel); }

    public Integer getExperience() { return getExperienceYears(); }
    public void setExperience(Integer exp) { setExperienceYears(exp); }

    public Set<Skill> getSkills() { return skills; }
    public void setSkills(Set<Skill> skills) { this.skills = skills; }
}
