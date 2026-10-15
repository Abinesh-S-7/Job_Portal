package com.example.jobPortal.dtos.job;

import com.example.jobPortal.models.JobType;
import com.example.jobPortal.models.Skill;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public record JobRequest(
        @NotBlank(message = "Job Title is Required")
        String title,

        @NotBlank(message = "Job Description is Required")
        String description,

        @NotBlank(message = "Job Location is Required")
        String location,

        @PositiveOrZero(message = "Experience must be zero or positive")
        Integer experienceRequired,

        @PositiveOrZero(message = "Salary must be zero or positive")
        Double salary,

        @NotNull(message = "Job type is required")
        JobType jobType,

        @Future(message = "Deadline must be in future")
        LocalDateTime deadline,

        Set<String> requiredSkills
) {}
