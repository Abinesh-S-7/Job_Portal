package com.example.jobPortal.dtos.application;

import com.example.jobPortal.models.Application;
import com.example.jobPortal.models.ApplicationStatus;
import com.example.jobPortal.models.Skill;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record ApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        String companyName,
        Long candidateId,
        String candidateName,
        String candidateEmail,
        String candidatePhone,
        String resumeUrl,
        String education,
        Integer experience,
        Set<String> skills,
        LocalDateTime appliedDate,
        ApplicationStatus status
) {
    public static ApplicationResponse fromEntity(Application app) {
        Set<String> skillNames = app.getCandidate().getSkills() == null ? Set.of() :
                app.getCandidate().getSkills().stream()
                        .map(Skill::getName)
                        .collect(Collectors.toSet());

        return new ApplicationResponse(
                app.getId(),
                app.getJob().getId(),
                app.getJob().getTitle(),
                app.getJob().getCompany().getCompanyName(),
                app.getCandidate().getId(),
                app.getCandidate().getUser().getName(),
                app.getCandidate().getUser().getEmail(),
                app.getCandidate().getPhoneNumber(),
                app.getCandidate().getResumeUrl(),
                app.getCandidate().getEducation(),
                app.getCandidate().getExperience(),
                skillNames,
                app.getAppliedDate(),
                app.getStatus()
        );
    }
}