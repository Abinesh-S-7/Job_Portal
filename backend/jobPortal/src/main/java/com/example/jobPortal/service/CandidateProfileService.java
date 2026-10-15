package com.example.jobPortal.service;

import com.example.jobPortal.dtos.candidate.CandidateProfileRequest;
import com.example.jobPortal.dtos.candidate.CandidateProfileResponse;
import com.example.jobPortal.models.CandidateProfile;
import com.example.jobPortal.models.Skill;
import com.example.jobPortal.repository.CandidateRepository;
import com.example.jobPortal.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateProfileService {

    private final CandidateRepository candidateRepository;
    private final SkillRepository skillRepository;

    public CandidateProfileResponse getProfileByUserId(Long userId) {
        CandidateProfile profile = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found for user: " + userId));
        return mapToResponse(profile);
    }

    public CandidateProfileResponse getProfileById(Long profileId) {
        CandidateProfile profile = candidateRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found with id: " + profileId));
        return mapToResponse(profile);
    }

    @Transactional
    public CandidateProfileResponse updateProfile(Long userId, CandidateProfileRequest request) {
        CandidateProfile profile = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Candidate profile not found for user: " + userId));

        profile.setEducation(request.education());
        profile.setExperience(request.experience());
        profile.setLocation(request.location());
        profile.setPhoneNumber(request.phoneNumber());
        profile.setAbout(request.about());
        profile.setResumeUrl(request.resumeUrl());

        // Manage skills association
        if (request.skills() != null) {
            Set<Skill> skills = request.skills().stream()
                    .map(name -> skillRepository.findByNameIgnoreCase(name.trim())
                            .orElseGet(() -> skillRepository.save(new Skill(null, name.trim()))))
                    .collect(Collectors.toSet());
            profile.setSkills(skills);
        }

        CandidateProfile updated = candidateRepository.save(profile);
        return mapToResponse(updated);
    }

    private CandidateProfileResponse mapToResponse(CandidateProfile profile) {
        Set<String> skillNames = profile.getSkills() == null ? Set.of() :
                profile.getSkills().stream().map(Skill::getName).collect(Collectors.toSet());

        return new CandidateProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getEmail(),
                profile.getEducation(),
                profile.getExperience(),
                profile.getLocation(),
                profile.getPhoneNumber(),
                profile.getAbout(),
                profile.getResumeUrl(),
                skillNames
        );
    }
}