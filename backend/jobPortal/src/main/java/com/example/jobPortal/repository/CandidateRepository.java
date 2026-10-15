package com.example.jobPortal.repository;

import com.example.jobPortal.models.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<CandidateProfile,Long> {
    Optional<CandidateProfile> findByUserId(Long id);
}
