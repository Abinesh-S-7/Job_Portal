package com.example.jobPortal.controller;

import com.example.jobPortal.dtos.candidate.CandidateProfileRequest;
import com.example.jobPortal.dtos.candidate.CandidateProfileResponse;
import com.example.jobPortal.models.User;
import com.example.jobPortal.service.CandidateProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidate-profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CandidateProfileController {

    private final CandidateProfileService profileService;

    // Candidate views own profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CandidateProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(profileService.getProfileByUserId(user.getId()));
    }

    // Candidate updates own profile
    @PutMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<CandidateProfileResponse> updateMyProfile(
            @AuthenticationPrincipal User user,
            @RequestBody CandidateProfileRequest request
    ) {
        return ResponseEntity.ok(profileService.updateProfile(user.getId(), request));
    }

    // Recruiter or Admin views candidate profile
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ResponseEntity<CandidateProfileResponse> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }
}