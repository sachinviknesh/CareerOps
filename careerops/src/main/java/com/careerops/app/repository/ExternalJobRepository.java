package com.careerops.app.repository;

import com.careerops.app.entity.ExternalJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalJobRepository extends JpaRepository<ExternalJob, Long> {

    Optional<ExternalJob> findBySourceAndExternalId(String source, String externalId);

    boolean existsBySourceAndExternalId(String source, String externalId);

    boolean existsByNormalizedTitleAndCompanyIgnoreCase(String normalizedTitle, String company);
}
