
package com.cfd.backend.controller;

import com.cfd.backend.dto.AdminUserResponse;
import com.cfd.backend.enums.UserStatus;
import com.cfd.backend.service.AdminUserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminUserService.getUserById(id));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError(exception.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request,
            Authentication authentication) {

        try {
            return ResponseEntity.ok(
                    adminUserService.updateUserStatus(
                            id,
                            request.status(),
                            authentication.getName()
                    )
            );
        } catch (IllegalArgumentException exception) {
            String message = exception.getMessage();

            if (message != null && message.startsWith("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiError(message));
            }

            return ResponseEntity.badRequest()
                    .body(new ApiError(message));
        }
    }

    public record UpdateStatusRequest(UserStatus status) {
    }

    public record ApiError(String message) {
    }
}
