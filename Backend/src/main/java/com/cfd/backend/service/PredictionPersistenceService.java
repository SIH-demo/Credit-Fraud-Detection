
package com.cfd.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cfd.backend.dto.MlPredictionResponse;
import com.cfd.backend.dto.TransactionPredictionRequest;
import com.cfd.backend.entity.Prediction;
import com.cfd.backend.entity.PredictionExplanation;
import com.cfd.backend.entity.Transaction;
import com.cfd.backend.enums.RiskLevel;
import com.cfd.backend.repository.PredictionExplanationRepository;
import com.cfd.backend.repository.PredictionRepository;
import com.cfd.backend.repository.TransactionRepository;

@Service
public class PredictionPersistenceService {

    private final TransactionRepository transactionRepository;
    private final PredictionRepository predictionRepository;
    private final PredictionExplanationRepository explanationRepository;
    private final InvestigationService investigationService;

    public PredictionPersistenceService(
        TransactionRepository transactionRepository,
        PredictionRepository predictionRepository,
        PredictionExplanationRepository explanationRepository,
        InvestigationService investigationService
) {
    this.transactionRepository = transactionRepository;
    this.predictionRepository = predictionRepository;
    this.explanationRepository = explanationRepository;
    this.investigationService = investigationService;
}

    @Transactional
    public LocalDateTime persist(
            TransactionPredictionRequest request,
            MlPredictionResponse mlResponse,
            String transactionReference,
            String correlationId,
            double threshold
    ) {
        return persistInternal(
                request,
                mlResponse,
                transactionReference,
                correlationId,
                threshold
        ).predictedAt();
    }

    @Transactional
    public PredictionPersistenceResult persistWithResult(
            TransactionPredictionRequest request,
            MlPredictionResponse mlResponse,
            String transactionReference,
            String correlationId,
            double threshold
    ) {
        return persistInternal(
                request,
                mlResponse,
                transactionReference,
                correlationId,
                threshold
        );
    }

    private PredictionPersistenceResult persistInternal(
            TransactionPredictionRequest request,
            MlPredictionResponse mlResponse,
            String transactionReference,
            String correlationId,
            double threshold
    ) {

        Transaction transaction = new Transaction();

        transaction.setTransactionReference(transactionReference);
        transaction.setExternalReference(request.externalReference());
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency());

        LocalDateTime transactionTimeUtc =
                request.transactionTime()
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime();

        transaction.setTransactionTime(transactionTimeUtc);
        transaction.setMerchantName(request.merchantName());
        transaction.setMerchantId(request.merchant());
        transaction.setStep(request.step());
        transaction.setAge(request.age());
        transaction.setGender(request.gender());
        transaction.setCategory(request.category());
        transaction.setCountry(request.country());
        transaction.setMaskedCardLast4(request.maskedCardLast4());
        transaction.setStatus(request.status());

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        if (mlResponse.prediction() == null
                || (mlResponse.prediction() != 0
                && mlResponse.prediction() != 1)) {
            throw new IllegalArgumentException(
                    "ML service returned an invalid prediction."
            );
        }

        if (mlResponse.risk_score() == null
                || !Double.isFinite(mlResponse.risk_score())
                || mlResponse.risk_score() < 0
                || mlResponse.risk_score() > 1) {
            throw new IllegalArgumentException(
                    "ML service returned an invalid risk score."
            );
        }

        LocalDateTime predictedAtUtc =
                OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();

        Prediction prediction = new Prediction();

        prediction.setTransaction(savedTransaction);
        prediction.setPrediction(mlResponse.prediction().byteValue());
        prediction.setRiskScore(
                BigDecimal.valueOf(mlResponse.risk_score())
                        .setScale(6, java.math.RoundingMode.HALF_UP)
        );
        prediction.setRiskLevel(
                RiskLevel.valueOf(mlResponse.risk_level())
        );
        prediction.setModelVersion(mlResponse.model_version());
        prediction.setThresholdUsed(
                BigDecimal.valueOf(threshold)
                        .setScale(6, java.math.RoundingMode.HALF_UP)
        );
        prediction.setCorrelationId(correlationId);
        prediction.setPredictedAt(predictedAtUtc);

        Prediction savedPrediction =
                predictionRepository.save(prediction);
        
        investigationService.createIfRequired(savedPrediction);

        if (mlResponse.explanation() != null
                && mlResponse.explanation().top_features() != null) {

            List<PredictionExplanation> explanations =
                    new ArrayList<>();

            int rank = 1;

            for (MlPredictionResponse.TopFeature feature
                    : mlResponse.explanation().top_features()) {

                PredictionExplanation explanation =
                        new PredictionExplanation();

                explanation.setPrediction(savedPrediction);
                explanation.setFeatureName(feature.feature());
                explanation.setContribution(feature.contribution());
                explanation.setImpact(feature.impact());
                explanation.setFeatureRank((short) rank++);

                explanations.add(explanation);
            }

            if (!explanations.isEmpty()) {
                explanationRepository.saveAll(explanations);
            }
        }

        return new PredictionPersistenceResult(
                savedTransaction,
                savedPrediction,
                predictedAtUtc
        );
    }
}
