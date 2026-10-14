package com.careerops.app.repository;

import com.careerops.app.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);

    Page<Notification> findByCandidateIdOrderByCreatedAtDesc(Long candidateId, Pageable pageable);

    long countByCandidateIdAndReadFalse(Long candidateId);

    Optional<Notification> findByIdAndCandidateId(Long id, Long candidateId);

    boolean existsByCandidateIdAndOpportunityIdAndNotificationType(Long candidateId, Long opportunityId, String notificationType);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.candidate.id = :candidateId")
    void markAllAsRead(@Param("candidateId") Long candidateId);
}
