package com.example.jobPortal.controller;

import com.example.jobPortal.models.User;
import com.example.jobPortal.repository.ApplicationRepository;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    // Platform-wide Metrics
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Long>> getPlatformMetrics() {
        return ResponseEntity.ok(Map.of(
                "totalUsers", userRepository.count(),
                "totalJobs", jobRepository.count(),
                "totalApplications", applicationRepository.count()
        ));
    }

    // List all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Remove spam user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Moderate/Delete any job post across all companies
    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> removeJob(@PathVariable Long id) {
        jobRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
