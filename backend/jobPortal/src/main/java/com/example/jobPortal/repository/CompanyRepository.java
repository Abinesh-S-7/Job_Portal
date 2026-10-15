package com.example.jobPortal.repository;

import com.example.jobPortal.models.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company,Long> {
    Optional<Company> findByRecruiterId(Long id);

}
