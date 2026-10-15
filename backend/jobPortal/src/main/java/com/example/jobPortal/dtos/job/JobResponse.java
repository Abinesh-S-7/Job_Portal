package com.example.jobPortal.dtos.job;

import com.example.jobPortal.models.Job;
import com.example.jobPortal.models.JobStatus;
import com.example.jobPortal.models.JobType;
import com.example.jobPortal.models.Skill;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record JobResponse(
        Long id,
        Long companyId,
        String companyName,
        String companyLocation,
        String title,
        String description,
        String location,
        Integer experienceRequired,
        Double salary,
        JobType jobType,
        LocalDateTime postedDate,
        LocalDateTime deadline,
        JobStatus status,
        Set<String> skills

) {
    public static JobResponse fromEntity(Job job)
    {
        return new JobResponse(
                job.getId(),
                job.getCompany().getId(),
                job.getCompany().getCompanyName(),
                job.getCompany().getLocation(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getExperienceRequired(),
                job.getSalary(),
                job.getJobType(),
                job.getPostedDate(),
                job.getDeadline(),
                job.getStatus(),
                job.getRequiredSkills()!=null
                    ? job.getRequiredSkills().stream().map(Skill::getName).collect(Collectors.toSet())
                        : Set.of()
        );
    }

}
