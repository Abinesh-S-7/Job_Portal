package com.example.jobPortal.repository;

import com.example.jobPortal.models.Job;
import com.example.jobPortal.models.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findJobById(Long id);

    List<Job> findByCompanyId(Long companyId);
    List<Job> findByStatus(JobStatus status);

    // Search with partial matching (LIKE %title%)
    List<Job> findByTitleContainingIgnoreCaseAndLocationIgnoreCase(String title, String location);
    List<Job> findByTitleContainingIgnoreCaseAndStatus(String title, JobStatus status);
    List<Job> findByLocationContainingIgnoreCaseAndStatus(String location, JobStatus status);
    List<Job> findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCaseAndStatus(
            String title, String location, JobStatus status
    );

    Long id(Long id);
}