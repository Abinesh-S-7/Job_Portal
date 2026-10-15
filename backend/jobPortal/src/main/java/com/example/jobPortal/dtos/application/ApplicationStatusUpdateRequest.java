package com.example.jobPortal.dtos.application;


import com.example.jobPortal.models.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusUpdateRequest(
        @NotNull(message = "Application status is required")
        ApplicationStatus status
) {}