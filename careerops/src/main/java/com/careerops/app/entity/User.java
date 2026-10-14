package com.careerops.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Column(nullable = false)
    private String name;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be empty")
    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @NotBlank(message = "Password cannot be empty")
    @Column(nullable = false)
    private String password;

    @Column(length = 30)
    private String role = "ROLE_USER";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (role == null) role = "ROLE_USER";
    }

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = "ROLE_USER";
        this.createdAt = LocalDateTime.now();
    }

    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role != null ? role : "ROLE_USER";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private java.util.List<Application> applications = new java.util.ArrayList<>();

    public java.util.List<Application> getApplications() { return applications; }
    public void setApplications(java.util.List<Application> applications) { this.applications = applications; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
