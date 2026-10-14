package com.careerops.app.repository;

import com.careerops.app.entity.JobSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobSourceRepository extends JpaRepository<JobSource, Long> {

    Optional<JobSource> findByCode(String code);

    boolean existsByCode(String code);

    List<JobSource> findByEnabledTrue();
}
