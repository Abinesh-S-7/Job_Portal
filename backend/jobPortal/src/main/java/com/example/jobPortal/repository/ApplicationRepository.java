package com.example.jobPortal.repository;

import com.example.jobPortal.models.Application;
import com.example.jobPortal.models.ApplicationStatus;
import com.example.jobPortal.models.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application,Long> {
    List<Application> findByCandidateId(Long candidateId);
    List<Application> findByJobId(Long jobId);
    List<Application> findByStatus(ApplicationStatus status);

    // For Recruiter viewing all applications for their company's jobs
    List<Application> findByJob_Company_Id(Long companyId);

    // Business rule check & fetching specific application
    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);
    Optional<Application> findByCandidateIdAndJobId(Long candidateId, Long jobId);

    // Dashboard metrics
    long countByJobId(Long jobId);
    long countByCandidateId(Long candidateId);
    long countByCandidateIdAndStatus(Long candidateId, ApplicationStatus status);
}
