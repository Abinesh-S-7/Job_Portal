package com.example.jobPortal.controller;

import com.example.jobPortal.dtos.company.CompanyRequest;
import com.example.jobPortal.dtos.company.CompanyResponse;
import com.example.jobPortal.models.User;
import com.example.jobPortal.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompanyController {

    private final CompanyService companyService;

    // Recruiter views own company profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<CompanyResponse> getMyCompany(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(companyService.getCompanyByRecruiterId(user.getId()));
    }

    // Recruiter updates company profile
    @PutMapping("/me")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<CompanyResponse> updateMyCompany(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CompanyRequest request
    ) {
        return ResponseEntity.ok(companyService.updateCompany(user.getId(), request));
    }

    // Public: anyone can view company profile
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }
}