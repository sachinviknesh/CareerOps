package com.careerops.app.service;

import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.JobMatch;
import com.careerops.app.entity.Notification;
import com.careerops.app.entity.NotificationPreference;
import com.careerops.app.entity.Opportunity;
import com.careerops.app.repository.NotificationPreferenceRepository;
import com.careerops.app.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifRepo;
    private final NotificationPreferenceRepository prefRepo;
    private final LocationTravelService travelService;

    public NotificationService(NotificationRepository notifRepo,
                               NotificationPreferenceRepository prefRepo,
                               LocationTravelService travelService) {
        this.notifRepo = notifRepo;
        this.prefRepo = prefRepo;
        this.travelService = travelService;
    }

    public NotificationPreference getOrCreatePreferences(Candidate candidate) {
        return prefRepo.findByCandidateId(candidate.getId())
                .orElseGet(() -> prefRepo.save(new NotificationPreference(candidate)));
    }

    public NotificationPreference updatePreferences(Candidate candidate, NotificationPreference updated) {
        NotificationPreference current = getOrCreatePreferences(candidate);
        if (updated.getPreferredTypes() != null) current.setPreferredTypes(updated.getPreferredTypes());
        if (updated.getMinFitScore() != null) current.setMinFitScore(updated.getMinFitScore());
        if (updated.getLocationScope() != null) current.setLocationScope(updated.getLocationScope());
        if (updated.getTravelPreference() != null) current.setTravelPreference(updated.getTravelPreference());
        if (updated.getFrequency() != null) current.setFrequency(updated.getFrequency());
        current.setEmailAlertsEnabled(updated.isEmailAlertsEnabled());
        current.setInAppAlertsEnabled(updated.isInAppAlertsEnabled());
        return prefRepo.save(current);
    }

    public void processMatchForNotification(Candidate candidate, JobMatch match) {
        if (candidate == null || match == null || match.getOpportunity() == null) return;

        Opportunity opp = match.getOpportunity();
        NotificationPreference pref = getOrCreatePreferences(candidate);

        if (!pref.isInAppAlertsEnabled()) {
            return;
        }

        // Check 1: Minimum Fit Score
        if (match.getFitScore() < pref.getMinFitScore()) {
            return;
        }

        // Check 2: Preferred Types
        if (pref.getPreferredTypes() != null && opp.getType() != null) {
            String types = pref.getPreferredTypes().toUpperCase();
            String oppTypeStr = opp.getType().name();
            boolean typeMatched = Arrays.stream(types.split(","))
                    .map(String::trim)
                    .anyMatch(t -> t.equalsIgnoreCase(oppTypeStr) || (oppTypeStr.equals("CTF_COMPETITION") && t.equals("CTF")));
            if (!typeMatched) {
                return;
            }
        }

        // Check 3: Travel preference
        LocationTravelService.TravelIntel travel = travelService.analyzeTravel(opp, candidate);
        if ("NO_TRAVEL_ONLY".equalsIgnoreCase(pref.getTravelPreference()) && travel.isTravelRequired()) {
            return;
        }

        // Check 4: Duplicate Notification
        if (notifRepo.existsByCandidateIdAndOpportunityIdAndNotificationType(candidate.getId(), opp.getId(), "NEW_OPPORTUNITY")) {
            return;
        }

        // Create Location-Aware Notification
        String locationDisplay = opp.getLocation() != null ? opp.getLocation() : "India";
        String mode = travel.getMode();
        boolean travelReq = travel.isTravelRequired();

        String message = String.format("%s • %s • %s",
                opp.getOrganization(),
                locationDisplay,
                travelReq ? (travel.getDistanceLabel() != null ? travel.getDistanceLabel() : "Travel required") : "No travel required");

        Notification notif = new Notification(
                candidate,
                opp,
                opp.getTitle(),
                message,
                "NEW_OPPORTUNITY",
                match.getFitScore(),
                match.getLocationFit() > 0 ? match.getLocationFit() : travel.getLocationFitScore(),
                locationDisplay,
                travel.getDistanceKm(),
                mode,
                travelReq,
                opp.getEventStartDate(),
                opp.getDeadline()
        );

        notifRepo.save(notif);
        log.info("Location-aware notification created for candidate {} on opportunity '{}' (CareerFit: {}%, LocFit: {}%)",
                candidate.getEmail(), opp.getTitle(), match.getFitScore(), notif.getLocationFit());
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForCandidate(Long candidateId) {
        return notifRepo.findByCandidateIdOrderByCreatedAtDesc(candidateId);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsPage(Long candidateId, Pageable pageable) {
        return notifRepo.findByCandidateIdOrderByCreatedAtDesc(candidateId, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long candidateId) {
        return notifRepo.countByCandidateIdAndReadFalse(candidateId);
    }

    public Notification markAsRead(Long notifId, Long candidateId) {
        Notification notif = notifRepo.findByIdAndCandidateId(notifId, candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notifId));
        notif.setRead(true);
        return notifRepo.save(notif);
    }

    public void markAllAsRead(Long candidateId) {
        notifRepo.markAllAsRead(candidateId);
    }
}
