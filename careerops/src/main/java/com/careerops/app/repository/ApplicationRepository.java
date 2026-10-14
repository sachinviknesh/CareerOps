package com.careerops.app.repository;

import com.careerops.app.entity.Application;
import com.careerops.app.entity.ApplicationStatus;
import com.careerops.app.entity.OpportunityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Derived queries
    Page<Application> findByCandidateId(Long candidateId, Pageable pageable);

    Page<Application> findByCandidateIdAndStatus(Long candidateId,
                                                  ApplicationStatus status,
                                                  Pageable pageable);

    Optional<Application> findByCandidateIdAndOpportunityId(Long candidateId, Long opportunityId);

    boolean existsByCandidateIdAndOpportunityId(Long candidateId, Long opportunityId);

    long countByCandidateIdAndStatus(Long candidateId, ApplicationStatus status);

    // Derived queries for College Requirements (Requirement 8)
    List<Application> findByStatus(ApplicationStatus status);

    List<Application> findByUserId(Long userId);

    List<Application> findByOpportunityId(Long opportunityId);

    // JPQL query (Requirement 9)
    @Query("SELECT a FROM Application a WHERE a.user.id = :userId")
    List<Application> findApplicationsByUserId(@Param("userId") Long userId);

    // JPQL query — filter by opportunity type for a candidate
    @Query("""
            select a
            from Application a
            where a.candidate.id = :candidateId
              and a.opportunity.type = :type
            """)
    Page<Application> findApplicationsByType(@Param("candidateId") Long candidateId,
                                             @Param("type") OpportunityType type,
                                             Pageable pageable);
}
