
package com.cfd.backend.controller;

import com.cfd.backend.dto.InvestigationApiResponse;
import com.cfd.backend.service.InvestigationManagementService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/investigations")
public class InvestigationController {

    private final InvestigationManagementService investigationService;

    public InvestigationController(
            InvestigationManagementService investigationService
    ) {
        this.investigationService = investigationService;
    }

    @GetMapping
    public ResponseEntity<?> getAllInvestigations() {

        return ResponseEntity.ok(
                investigationService.getAllInvestigations()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInvestigationDetails(
            @PathVariable Long id
    ) {

        try {
            return ResponseEntity.ok(
                    investigationService.getInvestigationDetails(id)
            );

        } catch (IllegalArgumentException exception) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiError(exception.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInvestigation(
            @PathVariable Long id,
            @RequestBody InvestigationApiResponse.UpdateRequest request
    ) {

        try {
            return ResponseEntity.ok(
                    investigationService.updateInvestigation(id, request)
            );

        } catch (IllegalArgumentException exception) {

            if (exception.getMessage() != null &&
                    exception.getMessage().startsWith("Investigation not found")) {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiError(exception.getMessage()));
            }

            return ResponseEntity.badRequest()
                    .body(new ApiError(exception.getMessage()));
        }
    }

    public record ApiError(String message) {
    }
}
