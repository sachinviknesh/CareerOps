package com.careerops.app.repository;

import com.careerops.app.entity.Opportunity;
import com.careerops.app.entity.OpportunityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    // Derived queries
    Page<Opportunity> findByType(OpportunityType type, Pageable pageable);

    Page<Opportunity> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Opportunity> findByRoleFamilyContainingIgnoreCase(String roleFamily, Pageable pageable);

    Page<Opportunity> findByTypeAndTitleContainingIgnoreCase(OpportunityType type, String title, Pageable pageable);

    // JPQL query #1 — find by required skill name
    @Query("""
            select distinct o
            from Opportunity o
            join o.requiredSkills s
            where lower(s.name) = lower(:skill)
            """)
    Page<Opportunity> findByRequiredSkill(@Param("skill") String skill, Pageable pageable);

    // JPQL query #2 — closing soon (active opportunities with deadline in next N days)
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
}
