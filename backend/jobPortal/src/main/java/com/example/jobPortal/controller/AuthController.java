package com.example.jobPortal.controller;

import com.example.jobPortal.dtos.auth.AuthResponse;
import com.example.jobPortal.dtos.auth.LoginRequest;
import com.example.jobPortal.dtos.auth.RegisterRequest;
import com.example.jobPortal.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request)
    {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request)
    {
        AuthResponse response = authService.login(request);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
