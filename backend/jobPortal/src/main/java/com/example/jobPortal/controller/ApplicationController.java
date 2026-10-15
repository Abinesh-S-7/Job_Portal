package com.example.jobPortal.controller;

import com.example.jobPortal.dtos.application.ApplicationRequest;
import com.example.jobPortal.dtos.application.ApplicationResponse;
import com.example.jobPortal.dtos.application.ApplicationStatusUpdateRequest;
import com.example.jobPortal.models.Application;
import com.example.jobPortal.models.User;
import com.example.jobPortal.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.ls.LSException;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody ApplicationRequest request,
                                                     @AuthenticationPrincipal User user)
    {
        return new ResponseEntity<>(applicationService.applyForJob(request.jobId(), user.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(@AuthenticationPrincipal User user)
    {
        return new ResponseEntity<>(applicationService.getMyApplications(user.getId()),HttpStatus.OK);
    }

    //to withdraw active application
    @PutMapping("/{id}/withdraw")
    public ResponseEntity<ApplicationResponse> withdrawApplication(@PathVariable Long id, @AuthenticationPrincipal User user)
    {
        return new ResponseEntity<>(applicationService.withdrawApplication(id, user.getId()),HttpStatus.OK);
    }

    //recruiters view applicants for specific job

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsForJob(@PathVariable Long jobId,@AuthenticationPrincipal User user)
    {
        return new ResponseEntity<>(applicationService.getApplicationsForJob(jobId, user.getId()),HttpStatus.OK);

    }

    //recruiter view all applications in posted jobs

    @GetMapping("/recruiter")
    public ResponseEntity<List<ApplicationResponse>> getAllApplicationsForRecruiter(@AuthenticationPrincipal User user)
    {
        return new ResponseEntity<>(applicationService.getAllApplicationForRecruiter(user.getId()),HttpStatus.OK);
    }

    //update Status

    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request,
            @AuthenticationPrincipal User user)
    {
        return new ResponseEntity<>(applicationService.updateApplicationStatus(id,request.status(), user.getId()),HttpStatus.OK);
    }


}
