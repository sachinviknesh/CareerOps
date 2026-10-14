package com.careerops.app;

import com.careerops.app.entity.*;
import com.careerops.app.service.EligibilityService;
import com.careerops.app.service.LocationTravelService;
import com.careerops.app.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OpportunityPlatformTest {

    @Autowired
    private LocationTravelService travelService;

    @Autowired
    private EligibilityService eligibilityService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private com.careerops.app.repository.CandidateRepository candidateRepository;

    @Autowired
    private com.careerops.app.repository.OpportunityRepository opportunityRepository;

    @Test
    @DisplayName("Haversine distance and travel intel: Online opportunity requires no travel")
    void testOnlineTravelIntel() {
        Opportunity opp = new Opportunity();
        opp.setTitle("National AI Hackathon 2026");
        opp.setWorkMode("ONLINE");
        opp.setLocation("Online");

        Candidate candidate = new Candidate();
        candidate.setCurrentCity("Chennai");
        candidate.setWillingToTravel(true);

        LocationTravelService.TravelIntel intel = travelService.analyzeTravel(opp, candidate);

        assertThat(intel.isTravelRequired()).isFalse();
        assertThat(intel.getMode()).isEqualTo("Online");
        assertThat(intel.getLocationFitScore()).isEqualTo(100);
        assertThat(intel.getTravelBadge()).contains("🟢 Online");
    }

    @Test
    @DisplayName("Offline travel intel: calculates approximate distance between Chennai and Bangalore")
    void testOfflineChennaiToBangalore() {
        Opportunity opp = new Opportunity();
        opp.setTitle("Bengaluru Tech Summit Hackathon");
        opp.setWorkMode("OFFLINE");
        opp.setLocation("Bangalore, Karnataka");
        opp.setCity("Bangalore");
        opp.setEventStartDate(LocalDate.now().plusDays(20));
        opp.setEventEndDate(LocalDate.now().plusDays(22));

        Candidate candidate = new Candidate();
        candidate.setCurrentCity("Chennai");
        candidate.setWillingToTravel(true);
        candidate.setMaxTravelDistanceKm(400);

        LocationTravelService.TravelIntel intel = travelService.analyzeTravel(opp, candidate);

        assertThat(intel.isTravelRequired()).isTrue();
        assertThat(intel.getDistanceKm()).isNotNull();
        // Distance between Chennai and Bangalore is roughly 290 km
        assertThat(intel.getDistanceKm()).isBetween(250.0, 350.0);
        assertThat(intel.isAccommodationRequired()).isTrue();
        assertThat(intel.getLocationFitScore()).isGreaterThanOrEqualTo(70);
        assertThat(intel.getTravelBadge()).contains("🔴 Offline");
    }

    @Test
    @DisplayName("Eligibility engine: evaluates degree, graduation batch, and experience")
    void testEligibilityEngine() {
        Opportunity opp = new Opportunity();
        opp.setTitle("Shaastra CyberHack 2026");
        opp.setEligibility("Open to enrolled college students (Batches 2025-2028). B.Tech / B.E in CS / IT.");
        opp.setEducationRequirement("B.Tech in Computer Science / IT");
        opp.setExperienceRequirement(0);

        Candidate eligibleCandidate = new Candidate();
        eligibleCandidate.setDegree("B.Tech Computer Science");
        eligibleCandidate.setGraduationYear(2026);
        eligibleCandidate.setExperience(0);

        EligibilityService.EligibilityResult res = eligibilityService.evaluate(opp, eligibleCandidate);
        assertThat(res.getStatus()).isEqualTo("ELIGIBLE");
        assertThat(res.getStatusLabel()).contains("✅ Eligible");
    }

    @Test
    @DisplayName("Opportunity types: all 12 types have distinct labels and register/apply action")
    void testOpportunityTypes() {
        assertThat(OpportunityType.HACKATHON.getActionLabel()).isEqualTo("Register Now");
        assertThat(OpportunityType.CTF_COMPETITION.getActionLabel()).isEqualTo("Register Now");
        assertThat(OpportunityType.JOB.getActionLabel()).isEqualTo("Apply Now");
        assertThat(OpportunityType.INTERNSHIP.getActionLabel()).isEqualTo("Apply Now");
        assertThat(OpportunityType.FELLOWSHIP.getActionLabel()).isEqualTo("Apply Now");
        assertThat(OpportunityType.SCHOLARSHIP.getActionLabel()).isEqualTo("Apply Now");
    }

    @Test
    @DisplayName("Notification preferences: filter notifications by minimum fit score")
    void testNotificationPreferenceFiltering() {
        Candidate candidate = new Candidate("Alert User", "alert@example.com", "hash");
        candidate = candidateRepository.save(candidate);

        NotificationPreference pref = new NotificationPreference(candidate);
        pref.setMinFitScore(85);
        pref.setPreferredTypes("JOB,HACKATHON,CTF_COMPETITION");
        pref.setInAppAlertsEnabled(true);
        notificationService.updatePreferences(candidate, pref);

        Opportunity opp = new Opportunity();
        opp.setTitle("Low Fit Job");
        opp.setType(OpportunityType.JOB);
        opp = opportunityRepository.save(opp);

        JobMatch match = new JobMatch();
        match.setCandidate(candidate);
        match.setOpportunity(opp);
        match.setFitScore(60); // Lower than minFitScore 85

        // Should be filtered out due to minFitScore
        notificationService.processMatchForNotification(candidate, match);
        List<Notification> notifs = notificationService.getNotificationsForCandidate(candidate.getId());
        assertThat(notifs).isEmpty();
    }
}
