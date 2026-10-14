package com.careerops.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "application",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_candidate_opportunity",
               columnNames = {"candidate_id", "opportunity_id"}
       ))
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "varchar(30)")
    private ApplicationStatus status;

    @Column(name = "applied_at")
    private LocalDate appliedAt;

    @Column(name = "next_action_date")
    private LocalDate nextActionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public Opportunity getOpportunity() { return opportunity; }
    public void setOpportunity(Opportunity opportunity) { this.opportunity = opportunity; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDate getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDate appliedAt) { this.appliedAt = appliedAt; }

    public LocalDate getNextActionDate() { return nextActionDate; }
    public void setNextActionDate(LocalDate nextActionDate) { this.nextActionDate = nextActionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
