package com.careerops.app.repository;

import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    @Override
    @EntityGraph(attributePaths = {"requiredSkills"})
    @NonNull
    Optional<Opportunity> findById(@NonNull Long id);

    // Derived queries (Requirement 8)
    Page<Opportunity> findByType(OpportunityType type, Pageable pageable);

    List<Opportunity> findByType(OpportunityType type);

    default List<Opportunity> findByOpportunityType(OpportunityType type) {
        return findByType(type);
    }

    Page<Opportunity> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    List<Opportunity> findByLocationContainingIgnoreCase(String location);

    default List<Opportunity> findByLocation(String location) {
        return findByLocationContainingIgnoreCase(location);
    }

    Page<Opportunity> findByOrganizationContainingIgnoreCase(String organization, Pageable pageable);

    List<Opportunity> findByOrganizationContainingIgnoreCase(String organization);

    default List<Opportunity> findByOrganization(String organization) {
        return findByOrganizationContainingIgnoreCase(organization);
    }

    Page<Opportunity> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    List<Opportunity> findByTitleContainingIgnoreCase(String title);

    List<Opportunity> findByDeadlineBefore(LocalDate deadline);

    Page<Opportunity> findByRoleFamilyContainingIgnoreCase(String roleFamily, Pageable pageable);

    Page<Opportunity> findByTypeAndTitleContainingIgnoreCase(OpportunityType type, String title, Pageable pageable);

    // JPQL query #1 (Requirement 9) — Find active opportunities by type
    @Query("""
            SELECT o FROM Opportunity o
            WHERE o.type = :type
              AND o.active = true
              AND (o.deadline IS NULL OR o.deadline >= CURRENT_DATE)
            """)
    List<Opportunity> findActiveByType(@Param("type") OpportunityType type);

    // JPQL query #2 — find by required skill name
    @Query("""
            select distinct o
            from Opportunity o
            join o.requiredSkills s
            where lower(s.name) = lower(:skill)
            """)
    Page<Opportunity> findByRequiredSkill(@Param("skill") String skill, Pageable pageable);

    // JPQL query #3 — closing soon (active opportunities with deadline in next N days)
    @Query("""
            select o
            from Opportunity o
            where o.active = true
              and o.deadline is not null
              and o.deadline between :today and :cutoff
            order by o.deadline asc
            """)
    List<Opportunity> findClosingSoon(@Param("today") LocalDate today,
                                     @Param("cutoff") LocalDate cutoff);

    // Duplicate detection methods
    boolean existsBySourceAndExternalJobId(String source, String externalJobId);

    Optional<Opportunity> findBySourceAndExternalJobId(String source, String externalJobId);

    boolean existsByNormalizedTitleAndOrganizationIgnoreCase(String normalizedTitle, String organization);

    Optional<Opportunity> findByNormalizedTitleAndOrganizationIgnoreCase(String normalizedTitle, String organization);

    @Query("select o from Opportunity o where o.active = true")
    List<Opportunity> findAllActive();
}
