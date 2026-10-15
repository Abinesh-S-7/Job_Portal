package com.example.jobPortal.service;

import com.example.jobPortal.dtos.company.CompanyRequest;
import com.example.jobPortal.dtos.company.CompanyResponse;
import com.example.jobPortal.models.Company;
import com.example.jobPortal.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyResponse getCompanyByRecruiterId(Long recruiterId) {
        Company company = companyRepository.findByRecruiterId(recruiterId)
                .orElseThrow(() -> new RuntimeException("Company profile not found for recruiter: " + recruiterId));
        return mapToResponse(company);
    }

    public CompanyResponse getCompanyById(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + companyId));
        return mapToResponse(company);
    }

    @Transactional
    public CompanyResponse updateCompany(Long recruiterId, CompanyRequest request) {
        Company company = companyRepository.findByRecruiterId(recruiterId)
                .orElseThrow(() -> new RuntimeException("Company profile not found for recruiter: " + recruiterId));

        company.setCompanyName(request.companyName());
        company.setDescription(request.description());
        company.setLocation(request.location());
        company.setWebsite(request.website());

        Company updated = companyRepository.save(company);
        return mapToResponse(updated);
    }

    private CompanyResponse mapToResponse(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getRecruiter().getId(),
                company.getRecruiter().getName(),
                company.getCompanyName(),
                company.getDescription(),
                company.getLocation(),
                company.getWebsite()
        );
    }
}