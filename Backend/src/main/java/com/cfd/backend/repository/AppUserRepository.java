package com.cfd.backend.repository;

import com.cfd.backend.entity.AppUser;
import com.cfd.backend.enums.UserRole;
import com.cfd.backend.enums.UserStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRoleAndStatusAndEmailVerifiedTrue(
            UserRole role,
            UserStatus status
    );
}