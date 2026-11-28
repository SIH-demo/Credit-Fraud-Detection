package com.cfd.backend.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.AssertTrue;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cfd.backend.dto.PredictionHistoryResponse;
import com.cfd.backend.service.PredictionHistoryService;

import com.cfd.backend.dto.PredictionApiResponse;
import com.cfd.backend.dto.TransactionPredictionRequest;
import com.cfd.backend.service.PredictionService;
import java.time.LocalDateTime;

import com.cfd.backend.enums.RiskLevel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/transactions")
public class PredictionController {

    private final PredictionService predictionService;
    private final PredictionHistoryService predictionHistoryService;

    public PredictionController(
        PredictionService predictionService,
        PredictionHistoryService predictionHistoryService
) {
    this.predictionService = predictionService;
    this.predictionHistoryService = predictionHistoryService;
}

    @PostMapping("/predict")
    public ResponseEntity<PredictionApiResponse> predict(
            @Valid @RequestBody TransactionPredictionRequest request
    ) {

        PredictionApiResponse response =
                predictionService.predict(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/predictions")
public ResponseEntity<PredictionHistoryResponse> getPredictionHistory(
        @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "Page number cannot be negative.")
        int page,

        @RequestParam(defaultValue = "10")
        @Min(value = 1, message = "Page size must be at least 1.")
        @Max(value = 100, message = "Page size cannot exceed 100.")
        int size,

        @RequestParam(required = false)
        RiskLevel riskLevel,

        @RequestParam(required = false)
        String transactionReference,

        @RequestParam(required = false)
        @Min(value = 0, message = "Prediction must be 0 or 1.")
        @Max(value = 1, message = "Prediction must be 0 or 1.")
        Byte prediction,

        @RequestParam(required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        LocalDateTime startDate,

        @RequestParam(required = false)
        @DateTimeFormat(iso = ISO.DATE_TIME)
        LocalDateTime endDate
) {
    PredictionHistoryResponse response =
            predictionHistoryService.getHistory(
                    page,
                    size,
                    riskLevel,
                    transactionReference,
                    prediction,
                    startDate,
                    endDate
            );

    return ResponseEntity.ok(response);
}


}