package com.cfd.backend.dto;

public record PredictionApiResponse(
        PredictionData data
) {

    public record PredictionData(
            String transactionId,
            Integer prediction,
            Double riskScore,
            String riskLevel,
            String modelVersion,
            Double thresholdUsed,
            String predictedAt,
            String explanation
    ) {}
}