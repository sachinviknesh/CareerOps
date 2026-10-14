package com.careerops.app.controller;

import com.careerops.app.dto.PageResponse;
import com.careerops.app.entity.Candidate;
import com.careerops.app.entity.Notification;
import com.careerops.app.entity.NotificationPreference;
import com.careerops.app.service.CandidateService;
import com.careerops.app.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Location-aware notifications and preference management")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;
    private final CandidateService candidateService;

    public NotificationController(NotificationService notificationService,
                                  CandidateService candidateService) {
        this.notificationService = notificationService;
        this.candidateService = candidateService;
    }

    @GetMapping
    @Operation(summary = "List candidate notifications", description = "Returns recent location-aware alerts for the candidate")
    public ResponseEntity<PageResponse<Notification>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Candidate candidate = candidateService.getByEmail(authentication.getName());
        Page<Notification> notifs = notificationService.getNotificationsPage(candidate.getId(), PageRequest.of(page, Math.min(size, 50)));
        return ResponseEntity.ok(PageResponse.from(notifs));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count", description = "Returns badge count for unread alerts")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Candidate candidate = candidateService.getByEmail(authentication.getName());
        long count = notificationService.getUnreadCount(candidate.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id, Authentication authentication) {
        Candidate candidate = candidateService.getByEmail(authentication.getName());
        Notification updated = notificationService.markAsRead(id, candidate.getId());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication authentication) {
        Candidate candidate = candidateService.getByEmail(authentication.getName());
        notificationService.markAllAsRead(candidate.getId());
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<NotificationPreference> getPreferences(Authentication authentication) {
        Candidate candidate = candidateService.getByEmail(authentication.getName());
        NotificationPreference pref = notificationService.getOrCreatePreferences(candidate);
        return ResponseEntity.ok(pref);
    }

    @PostMapping("/preferences")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<NotificationPreference> updatePreferences(
            @RequestBody NotificationPreference req,
            Authentication authentication) {
        Candidate candidate = candidateService.getByEmail(authentication.getName());
        NotificationPreference updated = notificationService.updatePreferences(candidate, req);
        return ResponseEntity.ok(updated);
    }
}
