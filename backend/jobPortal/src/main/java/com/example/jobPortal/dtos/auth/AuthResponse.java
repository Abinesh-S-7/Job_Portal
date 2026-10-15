package com.example.jobPortal.dtos.auth;

import com.example.jobPortal.models.Role;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String name,
        String email,
        Role role
) {
    public AuthResponse
    {
        if (tokenType==null)
            tokenType = "Bearer";
    }
}
