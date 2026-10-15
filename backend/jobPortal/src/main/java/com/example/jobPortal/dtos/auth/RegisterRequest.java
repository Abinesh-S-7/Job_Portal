package com.example.jobPortal.dtos.auth;

import com.example.jobPortal.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name is Required!")
        String name,
        @Email(message = "Invalid Email")
        String email,
        @Size(min = 6 , message = "Password must contain minimum 6 characters")
        String password,
        @NotNull
        Role role) {}
