package com.cfd.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record TransactionListResponse(
        List<TransactionItem> data,
        int page,
        int size,
        long totalElements,
        int totalPages,
        TransactionSummary summary
) {

    public record TransactionItem(
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
            String status,
            Long predictionId,
            Integer prediction,
            BigDecimal riskScore,
            String riskLevel,
            String modelVersion,
            String predictedAt
    ) {}

    public record TransactionSummary(
            long totalTransactions,
            long fraudTransactions,
            long lowRiskTransactions,
            long mediumRiskTransactions,
            long highRiskTransactions,
            long unpredictedTransactions
    ) {}
}