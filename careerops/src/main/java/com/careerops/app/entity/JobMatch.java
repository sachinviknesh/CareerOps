package com.careerops.app.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_match",
        indexes = {
                @Index(name = "idx_jobmatch_candidate_score", columnList = "candidate_id, fit_score"),
                @Index(name = "idx_jobmatch_opp", columnList = "opportunity_id"),
                @Index(name = "idx_jobmatch_loc_fit", columnList = "candidate_id, location_fit")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_candidate_opportunity_match", columnNames = {"candidate_id", "opportunity_id"})
        }
)
public class JobMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Column(name = "fit_score", nullable = false)
    private int fitScore;

    @Column(name = "location_fit")
    private int locationFit = 100;

    @Column(name = "eligibility_status", length = 30)
    private String eligibilityStatus = "UNKNOWN";

    @Column(name = "travel_required", length = 20)
    private String travelRequired = "NONE"; // NONE, OPTIONAL, REQUIRED

    @Column(name = "estimated_distance_km")
    private Double estimatedDistanceKm;

    @Column(length = 20)
    private String confidence;

    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private String matchedSkills;

    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills;

    @Column(name = "experience_match", length = 30)
    private String experienceMatch;

    @Column(name = "resume_quality_score")
    private double resumeQualityScore;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt = LocalDateTime.now();

    public JobMatch() {}

    public JobMatch(Candidate candidate, Opportunity opportunity, int fitScore, String confidence,
                    String matchedSkills, String missingSkills, String experienceMatch,
                    double resumeQualityScore, String recommendations) {
        this.candidate = candidate;
        this.opportunity = opportunity;
        this.fitScore = fitScore;
        this.confidence = confidence;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.experienceMatch = experienceMatch;
        this.resumeQualityScore = resumeQualityScore;
        this.recommendations = recommendations;
        this.calculatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public Opportunity getOpportunity() { return opportunity; }
    public void setOpportunity(Opportunity opportunity) { this.opportunity = opportunity; }

    public int getFitScore() { return fitScore; }
    public void setFitScore(int fitScore) { this.fitScore = fitScore; }

    public int getLocationFit() { return locationFit; }
    public void setLocationFit(int locationFit) { this.locationFit = locationFit; }

    public String getEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(String eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }

    public String getTravelRequired() { return travelRequired; }
    public void setTravelRequired(String travelRequired) { this.travelRequired = travelRequired; }

    public Double getEstimatedDistanceKm() { return estimatedDistanceKm; }
    public void setEstimatedDistanceKm(Double estimatedDistanceKm) { this.estimatedDistanceKm = estimatedDistanceKm; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getMatchedSkills() { return matchedSkills; }
    public void setMatchedSkills(String matchedSkills) { this.matchedSkills = matchedSkills; }

    public String getMissingSkills() { return missingSkills; }
    public void setMissingSkills(String missingSkills) { this.missingSkills = missingSkills; }

    public String getExperienceMatch() { return experienceMatch; }
    public void setExperienceMatch(String experienceMatch) { this.experienceMatch = experienceMatch; }

    public double getResumeQualityScore() { return resumeQualityScore; }
    public void setResumeQualityScore(double resumeQualityScore) { this.resumeQualityScore = resumeQualityScore; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
