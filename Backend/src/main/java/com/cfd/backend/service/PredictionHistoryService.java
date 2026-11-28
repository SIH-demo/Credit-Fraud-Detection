
package com.cfd.backend.service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.time.LocalDateTime;

import com.cfd.backend.enums.RiskLevel;
import com.cfd.backend.specification.PredictionSpecification;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.cfd.backend.dto.PredictionHistoryResponse;
import com.cfd.backend.dto.PredictionHistoryResponse.ExplanationItem;
import com.cfd.backend.dto.PredictionHistoryResponse.PredictionHistoryItem;
import com.cfd.backend.entity.Prediction;
import com.cfd.backend.entity.PredictionExplanation;
import com.cfd.backend.repository.PredictionExplanationRepository;
import com.cfd.backend.repository.PredictionRepository;

@Service
public class PredictionHistoryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final PredictionRepository predictionRepository;
    private final PredictionExplanationRepository explanationRepository;

    public PredictionHistoryService(
            PredictionRepository predictionRepository,
            PredictionExplanationRepository explanationRepository
    ) {
        this.predictionRepository = predictionRepository;
        this.explanationRepository = explanationRepository;
    }

    @Transactional(readOnly = true)
    public PredictionHistoryResponse getHistory(int page, int size,
        RiskLevel riskLevel,
        String transactionReference,
        Byte prediction,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative."
            );
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100."
            );
        }

        if (startDate != null && endDate != null
        && startDate.isAfter(endDate)) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Start date must be before or equal to end date."
    );
}

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "predictedAt")
        );

        Specification<Prediction> specification =
        PredictionSpecification.hasRiskLevel(riskLevel)
                .and(PredictionSpecification.hasTransactionReference(
                        transactionReference
                ))
                .and(PredictionSpecification.hasPrediction(prediction))
                .and(PredictionSpecification.predictedAtFrom(startDate))
                .and(PredictionSpecification.predictedAtTo(endDate));

        Page<Prediction> predictionPage =
                predictionRepository.findAll(specification,pageable);

        List<Prediction> predictions = predictionPage.getContent();

        if (predictions.isEmpty()) {
            return new PredictionHistoryResponse(
                    List.of(),
                    predictionPage.getNumber(),
                    predictionPage.getSize(),
                    predictionPage.getTotalElements(),
                    predictionPage.getTotalPages()
            );
        }

        List<Long> predictionIds = predictions.stream()
                .map(Prediction::getId)
                .toList();

        List<PredictionExplanation> explanations =
                explanationRepository
                        .findByPredictionIdInOrderByPredictionIdAscFeatureRankAsc(
                                predictionIds
                        );

        Map<Long, List<ExplanationItem>> explanationMap =
                explanations.stream()
                        .collect(Collectors.groupingBy(
                                explanation ->
                                        explanation.getPrediction().getId(),
                                Collectors.mapping(
                                        explanation -> new ExplanationItem(
                                                explanation.getFeatureName(),
                                                explanation.getContribution(),
                                                explanation.getImpact(),
                                                explanation.getFeatureRank()
                                        ),
                                        Collectors.toList()
                                )
                        ));

        List<PredictionHistoryItem> historyItems =
                predictions.stream()
                        .map(predictionRecord -> {

    var transaction = predictionRecord.getTransaction();

    return new PredictionHistoryItem(
            predictionRecord.getId(),
            transaction.getTransactionReference(),
            transaction.getAmount(),
            transaction.getCurrency(),
            transaction.getMerchantName(),
            transaction.getMerchantId(),
            transaction.getCategory(),
            transaction.getCountry(),
            transaction.getTransactionTime()
                    .atOffset(ZoneOffset.UTC)
                    .toString(),
            predictionRecord.getPrediction().intValue(),
            predictionRecord.getRiskScore(),
            predictionRecord.getRiskLevel().name(),
            predictionRecord.getModelVersion(),
            predictionRecord.getThresholdUsed(),
            predictionRecord.getCorrelationId(),
            predictionRecord.getPredictedAt()
                    .atOffset(ZoneOffset.UTC)
                    .toString(),
            explanationMap.getOrDefault(
                    predictionRecord.getId(),
                    List.of()
                )
                );
                })
                        .toList();

        return new PredictionHistoryResponse(
                historyItems,
                predictionPage.getNumber(),
                predictionPage.getSize(),
                predictionPage.getTotalElements(),
                predictionPage.getTotalPages()
        );
    }
}
