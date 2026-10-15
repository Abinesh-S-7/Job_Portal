package com.example.jobPortal.service;

import com.example.jobPortal.dtos.job.JobRequest;
import com.example.jobPortal.dtos.job.JobResponse;
import com.example.jobPortal.models.Company;
import com.example.jobPortal.models.Job;
import com.example.jobPortal.models.JobStatus;
import com.example.jobPortal.models.Skill;
import com.example.jobPortal.repository.CompanyRepository;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.SkillRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public JobResponse createJob(JobRequest request, Long recruiterUserId) {
        Company company = companyRepository.findByRecruiterId(recruiterUserId)
                .orElseThrow(() -> new RuntimeException("Company profile not found for recruiter"));

        Job job = new Job();
        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setLocation(request.location());
        job.setSalary(request.salary());
        job.setJobType(request.jobType());
        job.setExperienceRequired(request.experienceRequired());
        job.setDeadline(request.deadline());
        job.setCompany(company);
        job.setStatus(JobStatus.ACTIVE);

        job.setRequiredSkills(resolveSkills(request.requiredSkills()));

        Job saved = jobRepository.save(job);
        return JobResponse.fromEntity(saved);
    }

    public List<JobResponse> getAllJobs(String title, String location) {
        boolean hasTitle = title != null && !title.trim().isEmpty();
        boolean hasLocation = location != null && !location.trim().isEmpty();

        List<Job> jobs;

        if (hasTitle && hasLocation) {
            jobs = jobRepository.findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCaseAndStatus(
                    title.trim(), location.trim(), JobStatus.ACTIVE);
        } else if (hasTitle) {
            jobs = jobRepository.findByTitleContainingIgnoreCaseAndStatus(title.trim(), JobStatus.ACTIVE);
        } else if (hasLocation) {
            jobs = jobRepository.findByLocationContainingIgnoreCaseAndStatus(location.trim(), JobStatus.ACTIVE);
        } else {
            jobs = jobRepository.findByStatus(JobStatus.ACTIVE);
        }

        return jobs.stream()
                .map(JobResponse::fromEntity)
                .toList();
    }

    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
        return JobResponse.fromEntity(job);
    }

    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, Long recruiterUserId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        // Rule 4 & 6: Ownership check
        Long ownerId = job.getCompany().getRecruiter().getId();
        if (!Objects.equals(ownerId, recruiterUserId)) {
            throw new RuntimeException("Unauthorized: You can only edit jobs posted by your company");
        }

        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setLocation(request.location());
        job.setSalary(request.salary());
        job.setJobType(request.jobType());
        job.setExperienceRequired(request.experienceRequired());
        job.setDeadline(request.deadline());

        if (request.requiredSkills() != null) {
            job.setRequiredSkills(resolveSkills(request.requiredSkills()));
        }

        Job updated = jobRepository.save(job);
        return JobResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteJob(Long jobId, Long recruiterUserId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

        Long ownerId = job.getCompany().getRecruiter().getId();
        if (!Objects.equals(ownerId, recruiterUserId)) {
            throw new RuntimeException("Unauthorized: You can only delete jobs posted by your company");
        }

        // Soft-delete to keep historical application data intact
        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
    }

    // Helper method to resolve and persist skills cleanly
    private Set<Skill> resolveSkills(Set<String> skillNames) {
        Set<Skill> skills = new HashSet<>();
        if (skillNames == null || skillNames.isEmpty()) {
            return skills;
        }

        for (String name : skillNames) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                Skill skill = skillRepository.findByName(trimmed)
                        .orElseGet(() -> skillRepository.save(new Skill(null, trimmed)));
                skills.add(skill);
            }
        }
        return skills;
    }
}