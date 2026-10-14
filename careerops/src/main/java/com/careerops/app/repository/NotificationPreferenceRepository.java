package com.careerops.app.repository;

import com.careerops.app.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByCandidateId(Long candidateId);

    boolean existsByCandidateId(Long candidateId);
}
