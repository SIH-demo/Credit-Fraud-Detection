package com.cfd.backend.dto.auth;

public record AuthUserResponse(
        Long id,
        String fullName,
        String email,
        String role
) {}