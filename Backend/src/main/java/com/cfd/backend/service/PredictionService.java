
package com.cfd.backend.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cfd.backend.client.MlServiceClient;
import com.cfd.backend.dto.MlPredictionRequest;
import com.cfd.backend.dto.MlPredictionResponse;
import com.cfd.backend.dto.PredictionApiResponse;
import com.cfd.backend.dto.TransactionPredictionRequest;

import com.cfd.backend.entity.Prediction;

@Service
public class PredictionService {

    private static final double PREDICTION_THRESHOLD = 0.70;

    private final MlServiceClient mlServiceClient;
    private final PredictionPersistenceService persistenceService;

    public PredictionService(
            MlServiceClient mlServiceClient,
            PredictionPersistenceService persistenceService
    ) {
        this.mlServiceClient = mlServiceClient;
        this.persistenceService = persistenceService;
    }

    public PredictionApiResponse predict(
            TransactionPredictionRequest request
    ) {

        String transactionId = StringUtils.hasText(
                request.externalReference()
        )
                ? request.externalReference().trim()
                : "TXN-" + UUID.randomUUID();

        String correlationId = UUID.randomUUID().toString();

        Map<String, Object> features = new LinkedHashMap<>();

        features.put("step", request.step());
        features.put("age", request.age());
        features.put("gender", request.gender());
        features.put("merchant", request.merchant());
        features.put("category", request.category());
        features.put("amount", request.amount().doubleValue());

        MlPredictionRequest mlRequest = new MlPredictionRequest(
                transactionId,
                features,
                correlationId
        );

        MlPredictionResponse mlResponse =
                mlServiceClient.predict(mlRequest);

        // Persist transaction, prediction and SHAP explanations.
        LocalDateTime predictedAtUtc = persistenceService.persist(
                request,
                mlResponse,
                transactionId,
                correlationId,
                PREDICTION_THRESHOLD
        );

        String explanation = formatExplanation(
                mlResponse.explanation()
        );

        PredictionApiResponse.PredictionData predictionData =
                new PredictionApiResponse.PredictionData(
                        mlResponse.transaction_id(),
                        mlResponse.prediction(),
                        mlResponse.risk_score(),
                        mlResponse.risk_level(),
                        mlResponse.model_version(),
                        PREDICTION_THRESHOLD,
                        predictedAtUtc.atOffset(ZoneOffset.UTC).toString(),
                        explanation
                );

        return new PredictionApiResponse(predictionData);
    }

    public PredictionPersistenceResult predictForBatch(
        TransactionPredictionRequest request
) {

    String transactionId = StringUtils.hasText(
            request.externalReference()
    )
            ? request.externalReference().trim()
            : "TXN-" + UUID.randomUUID();

    String correlationId = UUID.randomUUID().toString();

    Map<String, Object> features = new LinkedHashMap<>();

    features.put("step", request.step());
    features.put("age", request.age());
    features.put("gender", request.gender());
    features.put("merchant", request.merchant());
    features.put("category", request.category());
    features.put("amount", request.amount().doubleValue());

    MlPredictionRequest mlRequest = new MlPredictionRequest(
            transactionId,
            features,
            correlationId
    );

    MlPredictionResponse mlResponse =
            mlServiceClient.predict(mlRequest);

    return persistenceService.persistWithResult(
            request,
            mlResponse,
            transactionId,
            correlationId,
            PREDICTION_THRESHOLD
    );
}

    private String formatExplanation(
            MlPredictionResponse.Explanation explanation
    ) {

        if (explanation == null
                || explanation.top_features() == null
                || explanation.top_features().isEmpty()) {

            return "No prediction explanation was provided by the ML service.";
        }

        return explanation.top_features()
                .stream()
                .map(feature ->
                        feature.feature()
                        + ": "
                        + feature.impact().replace("_", " ")
                        + " (contribution: "
                        + feature.contribution()
                        + ")"
                )
                .collect(Collectors.joining("; "));
    }
}
