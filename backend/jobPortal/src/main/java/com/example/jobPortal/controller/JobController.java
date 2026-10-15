package com.example.jobPortal.controller;

import com.example.jobPortal.dtos.job.JobRequest;
import com.example.jobPortal.dtos.job.JobResponse;
import com.example.jobPortal.models.User;
import com.example.jobPortal.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs(@RequestParam(required = false) String title, @RequestParam(required = false) String location) {
        List<JobResponse> jobs = jobService.getAllJobs(title, location);
        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        JobResponse job = jobService.getJobById(id);
        return new ResponseEntity<>(job, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request, @AuthenticationPrincipal User user)
    {
        return new ResponseEntity<>(jobService.createJob(request, user.getId()),HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id,@Valid @RequestBody JobRequest request, @AuthenticationPrincipal User user)
    {
        return new ResponseEntity<>(jobService.updateJob(id,request, user.getId()),HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJob(@PathVariable Long id,@AuthenticationPrincipal User user)
    {
        jobService.deleteJob(id, user.getId());
        return new ResponseEntity<>("Deleted Successfully",HttpStatus.OK);
    }
}
