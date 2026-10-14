package com.careerops.app.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notif_candidate_created", columnList = "candidate_id, created_at"),
        @Index(name = "idx_notif_candidate_read", columnList = "candidate_id, is_read")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id")
    private Opportunity opportunity;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "notification_type", length = 50)
    private String notificationType = "NEW_OPPORTUNITY"; // NEW_OPPORTUNITY, DEADLINE_ALERT, STATUS_CHANGE

    @Column(name = "career_fit")
    private Integer careerFit;

    @Column(name = "location_fit")
    private Integer locationFit;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(length = 30)
    private String mode; // Online, Offline, Hybrid

    @Column(name = "travel_required")
    private Boolean travelRequired = false;

    @Column(name = "event_date")
    private LocalDate eventDate;

    private LocalDate deadline;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}

    public Notification(Candidate candidate, Opportunity opportunity, String title, String message,
                        String notificationType, Integer careerFit, Integer locationFit, String locationName,
                        Double distanceKm, String mode, Boolean travelRequired, LocalDate eventDate, LocalDate deadline) {
        this.candidate = candidate;
        this.opportunity = opportunity;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.careerFit = careerFit;
        this.locationFit = locationFit;
        this.locationName = locationName;
        this.distanceKm = distanceKm;
        this.mode = mode;
        this.travelRequired = travelRequired;
        this.eventDate = eventDate;
        this.deadline = deadline;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public Opportunity getOpportunity() { return opportunity; }
    public void setOpportunity(Opportunity opportunity) { this.opportunity = opportunity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public Integer getCareerFit() { return careerFit; }
    public void setCareerFit(Integer careerFit) { this.careerFit = careerFit; }

    public Integer getLocationFit() { return locationFit; }
    public void setLocationFit(Integer locationFit) { this.locationFit = locationFit; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Boolean getTravelRequired() { return travelRequired; }
    public void setTravelRequired(Boolean travelRequired) { this.travelRequired = travelRequired; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
