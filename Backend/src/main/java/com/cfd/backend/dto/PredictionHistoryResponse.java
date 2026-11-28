
package com.cfd.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record PredictionHistoryResponse(
        List<PredictionHistoryItem> data,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public record PredictionHistoryItem(
            Long predictionId,
            String transactionId,
            BigDecimal amount,
            String currency,
            String merchantName,
            String merchantId,
            String category,
            String country,
            String transactionTime,
            Integer prediction,
            BigDecimal riskScore,
            String riskLevel,
            String modelVersion,
            BigDecimal thresholdUsed,
            String correlationId,
            String predictedAt,
            List<ExplanationItem> explanations
    ) {}

    public record ExplanationItem(
            String feature,
            Double contribution,
            String impact,
            Short rank
    ) {}
}
