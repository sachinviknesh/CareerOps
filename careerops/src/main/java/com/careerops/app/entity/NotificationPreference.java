package com.careerops.app.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_preference")
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    private Candidate candidate;

    @Column(name = "preferred_types", length = 500)
    private String preferredTypes = "JOB,INTERNSHIP,HACKATHON,CTF_COMPETITION,FELLOWSHIP,CODING_COMPETITION,QUIZ,INNOVATION_CHALLENGE,RESEARCH_COMPETITION,STUDENT_COMPETITION,SCHOLARSHIP";

    @Column(name = "min_fit_score")
    private Integer minFitScore = 70;

    @Column(name = "location_scope", length = 50)
    private String locationScope = "ANY"; // ANY, NEAR_ME, SELECTED_CITY, SELECTED_STATE, INDIA

    @Column(name = "travel_preference", length = 50)
    private String travelPreference = "ANY"; // NO_TRAVEL_ONLY, WILLING_TO_TRAVEL, ANY

    @Column(length = 30)
    private String frequency = "IMMEDIATE"; // IMMEDIATE, DAILY_DIGEST, WEEKLY_DIGEST

    @Column(name = "email_alerts_enabled")
    private boolean emailAlertsEnabled = true;

    @Column(name = "in_app_alerts_enabled")
    private boolean inAppAlertsEnabled = true;

    public NotificationPreference() {}

    public NotificationPreference(Candidate candidate) {
        this.candidate = candidate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public String getPreferredTypes() { return preferredTypes; }
    public void setPreferredTypes(String preferredTypes) { this.preferredTypes = preferredTypes; }

    public Integer getMinFitScore() { return minFitScore != null ? minFitScore : 70; }
    public void setMinFitScore(Integer minFitScore) { this.minFitScore = minFitScore; }

    public String getLocationScope() { return locationScope != null ? locationScope : "ANY"; }
    public void setLocationScope(String locationScope) { this.locationScope = locationScope; }

    public String getTravelPreference() { return travelPreference != null ? travelPreference : "ANY"; }
    public void setTravelPreference(String travelPreference) { this.travelPreference = travelPreference; }

    public String getFrequency() { return frequency != null ? frequency : "IMMEDIATE"; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public boolean isEmailAlertsEnabled() { return emailAlertsEnabled; }
    public void setEmailAlertsEnabled(boolean emailAlertsEnabled) { this.emailAlertsEnabled = emailAlertsEnabled; }

    public boolean isInAppAlertsEnabled() { return inAppAlertsEnabled; }
    public void setInAppAlertsEnabled(boolean inAppAlertsEnabled) { this.inAppAlertsEnabled = inAppAlertsEnabled; }
}
