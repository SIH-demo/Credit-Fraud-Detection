
package com.cfd.backend.dto;

import java.util.List;

public record BatchApiResponse<T>(
        T data
) {

    public record UploadData(
            String batchId,
            String status,
            int totalRows,
            String message
    ) {}

    public record ResultData(
            String batchId,
            String status,
            int totalTransactions,
            int totalRows,
            int processedRows,
            int errorCount,
            int skippedCount,
            int fraudCount,
            int normalCount,
            double fraudPercentage,
            int lowRiskCount,
            int mediumRiskCount,
            int highRiskCount,
            List<TransactionResult> results,
            List<RowError> errors,
            List<RowSkipped> skippedRows
    ) {}

    public record TransactionResult(
            String transactionId,
            double amount,
            String currency,
            String transactionTime,
            boolean isFraud,
            String riskLevel,
            double riskScore,
            String modelVersion
    ) {}

    public record RowError(
            long sourceRowNumber,
            String errorMessage
    ) {}

    public record RowSkipped(
            long sourceRowNumber,
            String message
    ) {}

    public record HistoryData(
            List<BatchSummary> batches,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    public record BatchSummary(
            String batchId,
            String status,
            int totalRows,
            int processedRows,
            int errorCount,
            String createdAt,
            String completedAt
    ) {}
}
