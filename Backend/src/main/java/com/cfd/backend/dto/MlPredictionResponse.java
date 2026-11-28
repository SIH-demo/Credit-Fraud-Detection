package com.cfd.backend.dto;

import java.util.List;

public record MlPredictionResponse(

        String transaction_id,

        Integer prediction,

        Double risk_score,

        String risk_level,

        String model_version,

        String correlation_id,

        Explanation explanation

) {

    public record Explanation(
            List<TopFeature> top_features
    ) {}

    public record TopFeature(
            String feature,
            Double contribution,
            String impact
    ) {}
}