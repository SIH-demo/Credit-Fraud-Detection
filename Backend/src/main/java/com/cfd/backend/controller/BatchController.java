
package com.cfd.backend.controller;

import com.cfd.backend.dto.BatchApiResponse;
import com.cfd.backend.entity.BatchJob;
import com.cfd.backend.service.BatchCsvParser;
import com.cfd.backend.service.BatchProcessingService;
import com.cfd.backend.service.BatchService;
import com.cfd.backend.service.BatchResultService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.cfd.backend.service.BatchHistoryService;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;

import com.cfd.backend.service.BatchAnalyticsService;
import com.cfd.backend.dto.analytics.AnalyticsApiResponse;
import com.cfd.backend.dto.analytics.BatchAnalyticsResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@Validated
@RequestMapping("/api/v1/batches")
public class BatchController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final BatchCsvParser batchCsvParser;
    private final BatchService batchService;
    private final BatchProcessingService batchProcessingService;
    private final BatchResultService batchResultService;
    private final BatchHistoryService batchHistoryService;
    private final BatchAnalyticsService batchAnalyticsService;

    public BatchController(
            BatchCsvParser batchCsvParser,
            BatchService batchService,
            BatchProcessingService batchProcessingService,
            BatchResultService batchResultService,
            BatchHistoryService batchHistoryService,
            BatchAnalyticsService batchAnalyticsService
    ) {
        this.batchCsvParser = batchCsvParser;
        this.batchService = batchService;
        this.batchProcessingService = batchProcessingService;
        this.batchResultService = batchResultService;
        this.batchHistoryService = batchHistoryService;
        this.batchAnalyticsService = batchAnalyticsService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBatch(
            @RequestParam("file") MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new ApiError("Please select a non-empty CSV file.")
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(
                    new ApiError("CSV file cannot exceed 10 MB.")
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !filename.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(
                    new ApiError("Only CSV files are supported.")
            );
        }

        try {
            BatchCsvParser.ParseResult parseResult =
                    batchCsvParser.parse(file.getInputStream());

            BatchJob batchJob = batchService.createBatch(parseResult);

            if (parseResult.validRows() == 0) {
                batchJob.setStatus(
                        com.cfd.backend.enums.BatchJobStatus.FAILED
                );
                batchJob.setCompletedAt(java.time.LocalDateTime.now());
            } else {
                batchProcessingService.processBatch(
                        batchJob.getBatchId(),
                        parseResult
                );
            }

            BatchApiResponse<BatchApiResponse.UploadData> response =
                    new BatchApiResponse<>(
                            new BatchApiResponse.UploadData(
                                    batchJob.getBatchId(),
                                    batchJob.getStatus().name(),
                                    parseResult.totalRows(),
                                    "Batch uploaded successfully. Processing started."
                            )
                    );

            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(response);

        } catch (IllegalArgumentException exception) {

            return ResponseEntity.badRequest().body(
                    new ApiError(exception.getMessage())
            );

        } catch (Exception exception) {

            return ResponseEntity.internalServerError().body(
                    new ApiError("Could not process the uploaded CSV file.")
            );
        }
    }

    @GetMapping
public ResponseEntity<?> getBatchHistory(
        @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "Page number cannot be negative.")
        int page,

        @RequestParam(defaultValue = "10")
        @Min(value = 1, message = "Page size must be at least 1.")
        @Max(value = 100, message = "Page size cannot exceed 100.")
        int size
) {
    return ResponseEntity.ok(
            batchHistoryService.getBatchHistory(page, size)
    );
}

    public record ApiError(String message) {
    }

    @GetMapping("/{batchId}")
public ResponseEntity<?> getBatchResult(
        @PathVariable String batchId
) {
    try {
        return ResponseEntity.ok(
                batchResultService.getBatchResult(batchId)
        );
    } catch (IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(exception.getMessage()));
    }
}

@GetMapping("/{batchId}/analytics")
public ResponseEntity<?> getBatchAnalytics(
        @PathVariable String batchId
) {
    try {
        AnalyticsApiResponse<BatchAnalyticsResponse> response =
                batchAnalyticsService.analyzeBatch(batchId);

        return ResponseEntity.ok(response);

    } catch (IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(exception.getMessage()));
    }
}

@GetMapping("/{batchId}/report")
public ResponseEntity<?> downloadBatchReport(
        @PathVariable String batchId
) {
    try {
        ResponseEntity<byte[]> report =
                batchAnalyticsService.generateBatchReport(batchId);

        return ResponseEntity
                .status(report.getStatusCode())
                .contentType(report.getHeaders().getContentType())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        report.getHeaders().getFirst(
                                HttpHeaders.CONTENT_DISPOSITION
                        )
                )
                .body(report.getBody());

    } catch (IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(exception.getMessage()));
    }
}

}
