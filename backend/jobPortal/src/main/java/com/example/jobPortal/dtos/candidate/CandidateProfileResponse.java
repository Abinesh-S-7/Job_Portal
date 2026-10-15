package com.example.jobPortal.dtos.candidate;

import java.util.Set;

public record CandidateProfileResponse(
        Long id,
        Long userId,
        String name,
        String email,
        String education,
        Integer experience,
        String location,
        String phoneNumber,
        String about,
        String resumeUrl,
        Set<String> skills
) {}