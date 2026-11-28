
package com.cfd.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record TransactionDetailsResponse(
        Long id,
        String transactionReference,
        String externalReference,
        BigDecimal amount,
        String currency,
        String merchantName,
        String merchantId,
        String category,
        String country,
        String transactionTime,
        String maskedCardLast4,
        String status,
        PredictionDetails prediction
) {

    public record PredictionDetails(
            Long predictionId,
            Integer prediction,
            BigDecimal riskScore,
            String riskLevel,
            String modelVersion,
            BigDecimal thresholdUsed,
            String predictedAt,
            List<PredictionHistoryResponse.ExplanationItem> explanations
    ) {}
}
