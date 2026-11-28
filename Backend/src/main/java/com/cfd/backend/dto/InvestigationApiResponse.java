
package com.cfd.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record InvestigationApiResponse<T>(
        T data
) {

    public record InvestigationSummary(
            Long id,
            String caseId,
            Long transactionId,
            String transactionReference,
            BigDecimal amount,
            String currency,
            BigDecimal riskScore,
            String riskLevel,
            String priority,
            String assignedTo,
            String status,
            String createdAt
    ) {}

    public record InvestigationDetails(
            Long id,
            String caseId,
            String status,
            String priority,
            String assignedTo,
            String outcome,
            String createdAt,
            String updatedAt,
            TransactionDetails transaction,
            PredictionDetails prediction,
            List<NoteDetails> notes,
            List<HistoryDetails> history
    ) {}

    public record TransactionDetails(
            Long id,
            String transactionReference,
            String externalReference,
            BigDecimal amount,
            String currency,
            String transactionTime,
            String merchantName,
            String merchantId,
            String country,
            String maskedCardLast4,
            String status
    ) {}

    public record PredictionDetails(
            Long id,
            Byte prediction,
            BigDecimal riskScore,
            String riskLevel,
            String modelVersion,
            String predictedAt
    ) {}

    public record NoteDetails(
            Long id,
            String author,
            String note,
            String createdAt
    ) {}

    public record HistoryDetails(
            Long id,
            String eventType,
            String description,
            String changedBy,
            String createdAt
    ) {}

    public record UpdateRequest(
            String assignedTo,
            String status,
            String outcome,
            String remarks,
            String changedBy
    ) {}
}
