package com.cfd.backend.dto;

import com.cfd.backend.entity.AppUser;
import com.cfd.backend.enums.UserRole;
import com.cfd.backend.enums.UserStatus;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String fullName,
        String email,
        UserRole role,
        UserStatus status,
        Boolean emailVerified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AdminUserResponse from(AppUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}