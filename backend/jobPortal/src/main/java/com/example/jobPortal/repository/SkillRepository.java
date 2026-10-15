package com.example.jobPortal.repository;

import com.example.jobPortal.models.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill,Long> {
    Optional<Skill> findByName(String name);
    Optional<Skill> findAllById(Long id);
    Optional<Skill> findByNameIgnoreCase(String name);
}
