package com.careerops.app.repository;

import com.careerops.app.entity.Candidate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.lang.NonNull;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    @EntityGraph(attributePaths = {"skills"})
    Optional<Candidate> findByEmailIgnoreCase(String email);

    @Override
    @EntityGraph(attributePaths = {"skills"})
    @NonNull
    Optional<Candidate> findById(@NonNull Long id);

    boolean existsByEmailIgnoreCase(String email);
}
