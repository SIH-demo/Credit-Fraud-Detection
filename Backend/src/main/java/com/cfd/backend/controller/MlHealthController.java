package com.cfd.backend.controller;

import com.cfd.backend.dto.MlHealthResponse;
import com.cfd.backend.service.MlHealthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class MlHealthController {

    private final MlHealthService mlHealthService;

    public MlHealthController(MlHealthService mlHealthService) {
        this.mlHealthService = mlHealthService;
    }

    @GetMapping("/ml")
    public ResponseEntity<MlHealthResponse> getMlHealth() {

        MlHealthResponse response = mlHealthService.getHealth();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}