package com.example.jobPortal.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank(message = "Email is Required!")
        @Email
        String email,

        @NotBlank(message = "Password is Required")
        String password) {}
