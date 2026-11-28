
package com.cfd.backend.service;

import com.cfd.backend.dto.AdminUserResponse;
import com.cfd.backend.entity.AppUser;
import com.cfd.backend.enums.UserRole;
import com.cfd.backend.enums.UserStatus;
import com.cfd.backend.repository.AppUserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminUserService {

    private final AppUserRepository userRepository;

    public AdminUserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long id) {
        AppUser user = findUser(id);
        return AdminUserResponse.from(user);
    }

    @Transactional
    public AdminUserResponse updateUserStatus(
            Long id,
            UserStatus newStatus,
            String actingAdminEmail) {

        AppUser user = findUser(id);

        if (newStatus == null) {
            throw new IllegalArgumentException("User status is required.");
        }

        if (newStatus == UserStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Users cannot be moved back to PENDING.");
        }

        boolean isSelf = user.getEmail().equalsIgnoreCase(actingAdminEmail);

        if (isSelf && newStatus != UserStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "You cannot lock your own Administrator account.");
        }

        boolean isActiveAdmin =
                user.getRole() == UserRole.ADMIN
                && user.getStatus() == UserStatus.ACTIVE
                && Boolean.TRUE.equals(user.getEmailVerified());

        if (isActiveAdmin
                && newStatus != UserStatus.ACTIVE
                && userRepository.countByRoleAndStatusAndEmailVerifiedTrue(
                        UserRole.ADMIN,
                        UserStatus.ACTIVE) <= 1) {

            throw new IllegalArgumentException(
                    "The last active Administrator cannot be locked.");
        }

        user.setStatus(newStatus);

        return AdminUserResponse.from(userRepository.save(user));
    }

    private AppUser findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with id: " + id));
    }
}
