package com.cfd.backend.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.cfd.backend.dto.analytics.AnalyticsApiResponse;
import com.cfd.backend.dto.analytics.DashboardRequest;
import com.cfd.backend.dto.analytics.DashboardResponse;
import com.cfd.backend.dto.analytics.ModelEvaluationRequest;
import com.cfd.backend.dto.analytics.ModelEvaluationResponse;
import com.cfd.backend.dto.analytics.BatchAnalyticsRequest;
import com.cfd.backend.dto.analytics.BatchAnalyticsResponse;
import com.cfd.backend.service.AnalyticsService;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(
            AnalyticsService analyticsService
    ) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/dashboard/summary")
    public ResponseEntity<AnalyticsApiResponse<DashboardResponse>>
            getDashboardSummary(
                    @Valid @RequestBody DashboardRequest request
            ) {

        AnalyticsApiResponse<DashboardResponse> response =
                analyticsService.getDashboardSummary(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/model/evaluate")
    public ResponseEntity<AnalyticsApiResponse<ModelEvaluationResponse>>
            evaluateModel(
                    @Valid @RequestBody ModelEvaluationRequest request
            ) {

        AnalyticsApiResponse<ModelEvaluationResponse> response =
                analyticsService.evaluateModel(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/batches/analyze")
public ResponseEntity<AnalyticsApiResponse<BatchAnalyticsResponse>>
        analyzeBatch(
                @Valid @RequestBody BatchAnalyticsRequest request
        ) {

    AnalyticsApiResponse<BatchAnalyticsResponse> response =
            analyticsService.analyzeBatch(request);

    return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
}

    @PostMapping("/batches/report")
public ResponseEntity<byte[]> generateBatchReport(
        @Valid @RequestBody BatchAnalyticsRequest request
) {
    ResponseEntity<byte[]> response =
            analyticsService.generateBatchReport(request);

    return ResponseEntity
            .status(response.getStatusCode())
            .contentType(response.getHeaders().getContentType())
            .header(
                    "Content-Disposition",
                    response.getHeaders().getFirst("Content-Disposition")
            )
            .body(response.getBody());
}

}