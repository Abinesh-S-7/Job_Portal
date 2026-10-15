package com.example.jobPortal.service;

import com.example.jobPortal.dtos.application.ApplicationResponse;
import com.example.jobPortal.models.*;
import com.example.jobPortal.repository.ApplicationRepository;
import com.example.jobPortal.repository.CandidateRepository;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;

    public ApplicationResponse applyForJob(Long jobId,Long candidateId)
    {
        CandidateProfile candidate = candidateRepository.findByUserId(candidateId)
                .orElseThrow(()-> new RuntimeException("Candidate Not Found"));
        Job job = jobRepository.findJobById(jobId)
                .orElseThrow(()-> new RuntimeException("Job Not Found"));
        if (job.getStatus() != JobStatus.ACTIVE) {
            throw new RuntimeException("This job posting is no longer active.");
        }

        // 4. Rule: Check Application Deadline
        if (job.getDeadline() != null && LocalDateTime.now().isAfter(job.getDeadline())) {
            throw new RuntimeException("The deadline for this job application has passed.");
        }

        // 5. Rule: Check Duplicate Application
        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), job.getId())) {
            throw new RuntimeException("You have already applied for this job.");
        }

        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedDate(LocalDateTime.now());
        return ApplicationResponse.fromEntity(applicationRepository.save(application));
    }

    public List<ApplicationResponse> getMyApplications(Long candidateUserId)
    {
        CandidateProfile candidate = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(()-> new RuntimeException("Candidate Not Found"));
        return applicationRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(ApplicationResponse::fromEntity)
                .toList();
    }

    public ApplicationResponse withdrawApplication(Long applicationId,Long candidateUserId)
    {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(()->new RuntimeException("Application Not Found"));

        Long applicationCandidateUserId = application.getCandidate().getUser().getId();
        if(!application.getCandidate().getUser().getId().equals(candidateUserId))
        {
            throw new RuntimeException("You can only withdraw your own applications");
        }
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new RuntimeException("Application is already withdrawn.");
        }
        if(application.getStatus().equals(ApplicationStatus.SELECTED) ||
                application.getStatus().equals(ApplicationStatus.REJECTED))
        {
            throw new RuntimeException("You can't withdraw application at this stage");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        return ApplicationResponse.fromEntity(applicationRepository.save(application));
    }

    public ApplicationResponse updateApplicationStatus(Long applicationId,ApplicationStatus newStatus,Long recruiterId)
    {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(()-> new RuntimeException("Application Not Found"));

        if(!application.getJob().getCompany().getRecruiter().getId().equals(recruiterId))
        {
            throw new RuntimeException("Unauthorized: You cannot modify applications for other companies' jobs");
        }
        application.setStatus(newStatus);
        return ApplicationResponse.fromEntity(applicationRepository.save(application));
    }

    public List<ApplicationResponse> getApplicationsForJob(Long jobId,Long recruiterId)
    {
        Job job = jobRepository.findJobById(jobId)
                .orElseThrow(()->new RuntimeException("Job Not Found"));
        if(!job.getCompany().getRecruiter().getId().equals(recruiterId))
        {
            throw new RuntimeException("Unauthorized: You cannot view Applications Posted By other Recruiters");
        }
        return applicationRepository.findByJobId(jobId).stream().map(ApplicationResponse::fromEntity).toList();
    }

    public List<ApplicationResponse> getAllApplicationForRecruiter(Long recruiterUserId)
    {
        return applicationRepository.findAll()
                .stream()
                .filter(app-> Objects.equals(app.getJob().getCompany().getRecruiter().getId(),recruiterUserId))
                .map(ApplicationResponse::fromEntity)
                .toList();
    }
}
