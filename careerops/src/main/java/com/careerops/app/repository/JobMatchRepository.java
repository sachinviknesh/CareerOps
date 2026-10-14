package com.careerops.app.repository;

import com.careerops.app.entity.JobMatch;
import com.careerops.app.entity.OpportunityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobMatchRepository extends JpaRepository<JobMatch, Long> {

    @EntityGraph(attributePaths = {"opportunity", "opportunity.requiredSkills"})
    Optional<JobMatch> findByCandidateIdAndOpportunityId(Long candidateId, Long opportunityId);

    @EntityGraph(attributePaths = {"opportunity", "opportunity.requiredSkills"})
    List<JobMatch> findByCandidateIdOrderByFitScoreDesc(Long candidateId);

    @EntityGraph(attributePaths = {"opportunity", "opportunity.requiredSkills"})
    List<JobMatch> findTop10ByCandidateIdOrderByFitScoreDesc(Long candidateId);

    @Query("""
            select distinct m
            from JobMatch m
            join fetch m.opportunity o
            left join fetch o.requiredSkills s
            where m.candidate.id = :candidateId
              and (:minScore is null or m.fitScore >= :minScore)
              and (:type is null or o.type = :type)
              and (:workMode is null or lower(o.workMode) = lower(:workMode))
              and (:location is null or lower(o.location) like lower(concat('%', :location, '%')))
              and (:deadline is null or o.deadline >= :deadline)
              and (:skill is null or lower(s.name) = lower(:skill))
            order by m.fitScore desc
            """)
    Page<JobMatch> findMatchesWithFilters(@Param("candidateId") Long candidateId,
                                          @Param("minScore") Integer minScore,
                                          @Param("type") OpportunityType type,
                                          @Param("workMode") String workMode,
                                          @Param("location") String location,
                                          @Param("deadline") LocalDate deadline,
                                          @Param("skill") String skill,
                                          Pageable pageable);

    void deleteByOpportunityId(Long opportunityId);
}
