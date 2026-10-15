package com.example.jobPortal.dtos.company;

public record CompanyResponse(
        Long id,
        Long recruiterId,
        String recruiterName,
        String companyName,
        String description,
        String location,
        String website
) {}