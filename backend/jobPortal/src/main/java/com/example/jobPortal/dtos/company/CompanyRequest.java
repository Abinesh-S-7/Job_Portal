package com.example.jobPortal.dtos.company;

import jakarta.validation.constraints.NotBlank;

public record CompanyRequest(
        @NotBlank(message = "Company name is required")
        String companyName,
        String description,
        String location,
        String website
) {}