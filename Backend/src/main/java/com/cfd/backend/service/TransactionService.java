package com.cfd.backend.service;

import com.cfd.backend.dto.TransactionListResponse;
import com.cfd.backend.entity.Prediction;
import com.cfd.backend.entity.Transaction;
import com.cfd.backend.enums.RiskLevel;
import com.cfd.backend.repository.PredictionRepository;
import com.cfd.backend.repository.TransactionRepository;
import com.cfd.backend.specification.TransactionSpecification;
import com.cfd.backend.dto.TransactionDetailsResponse;
import com.cfd.backend.dto.PredictionHistoryResponse.ExplanationItem;
import com.cfd.backend.entity.PredictionExplanation;
import com.cfd.backend.repository.PredictionExplanationRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.ZoneOffset;

@Service
public class TransactionService {

    private static final int SUMMARY_PAGE_SIZE = 500;

    private final TransactionRepository transactionRepository;
    private final PredictionRepository predictionRepository;
    private final PredictionExplanationRepository explanationRepository;

    public TransactionService(
        TransactionRepository transactionRepository,
        PredictionRepository predictionRepository,
        PredictionExplanationRepository explanationRepository
) {
    this.transactionRepository = transactionRepository;
    this.predictionRepository = predictionRepository;
    this.explanationRepository = explanationRepository;
}

    @Transactional(readOnly = true)
public TransactionListResponse.TransactionItem getTransactionById(Long id) {

    Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Transaction not found with ID: " + id
            ));

    Map<Long, Prediction> latestPredictions =
            getLatestPredictions(List.of(id));

    return mapTransaction(
            transaction,
            latestPredictions.get(id)
    );
}

    @Transactional(readOnly = true)
public TransactionDetailsResponse getTransactionDetails(Long id) {

    Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Transaction not found with ID: " + id
            ));

    List<Prediction> predictions =
            predictionRepository.findByTransactionIdOrderByPredictedAtDesc(id);

    Prediction latestPrediction = predictions.isEmpty()
            ? null
            : predictions.get(0);

    TransactionDetailsResponse.PredictionDetails predictionDetails = null;

    if (latestPrediction != null) {

        List<PredictionExplanation> explanations =
                explanationRepository
                        .findByPredictionIdInOrderByPredictionIdAscFeatureRankAsc(
                                List.of(latestPrediction.getId())
                        );

        List<ExplanationItem> explanationItems = explanations.stream()
                .map(explanation -> new ExplanationItem(
                        explanation.getFeatureName(),
                        explanation.getContribution(),
                        explanation.getImpact(),
                        explanation.getFeatureRank()
                ))
                .toList();

        predictionDetails = new TransactionDetailsResponse.PredictionDetails(
                latestPrediction.getId(),
                latestPrediction.getPrediction().intValue(),
                latestPrediction.getRiskScore(),
                latestPrediction.getRiskLevel().name(),
                latestPrediction.getModelVersion(),
                latestPrediction.getThresholdUsed(),
               latestPrediction.getPredictedAt() != null
        ? latestPrediction.getPredictedAt()
                .atOffset(ZoneOffset.UTC).toString()
        : null,
                explanationItems
        );
    }

    return new TransactionDetailsResponse(
            transaction.getId(),
            transaction.getTransactionReference(),
            transaction.getExternalReference(),
            transaction.getAmount(),
            transaction.getCurrency(),
            transaction.getMerchantName(),
            transaction.getMerchantId(),
            transaction.getCategory(),
            transaction.getCountry(),
            transaction.getTransactionTime() != null
        ? transaction.getTransactionTime()
                .atOffset(ZoneOffset.UTC).toString()
        : null,
            transaction.getMaskedCardLast4(),
            transaction.getStatus(),
            predictionDetails
    );
}

    @Transactional(readOnly = true)
    public TransactionListResponse getTransactions(
            int page,
            int size,
            String search,
            String status,
            String currency,
            String riskLevel
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("transactionTime"),
                        Sort.Order.desc("id")
                )
        );

        Page<Transaction> transactionPage =
                transactionRepository.findAll(
                        TransactionSpecification.filter(
                                search,
                                status,
                                currency,
                                riskLevel
                        ),
                        pageable
                );

        List<Long> transactionIds = transactionPage.getContent()
                .stream()
                .map(Transaction::getId)
                .toList();

        Map<Long, Prediction> latestPredictions =
                getLatestPredictions(transactionIds);

        List<TransactionListResponse.TransactionItem> items =
                transactionPage.getContent()
                        .stream()
                        .map(transaction -> mapTransaction(
                                transaction,
                                latestPredictions.get(transaction.getId())
                        ))
                        .toList();

        TransactionListResponse.TransactionSummary summary =
                calculateSummary();

        return new TransactionListResponse(
                items,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                summary
        );
    }

    private Map<Long, Prediction> getLatestPredictions(
            List<Long> transactionIds
    ) {

        if (transactionIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Prediction> result = new HashMap<>();

        predictionRepository.findLatestPredictionsByTransactionIds(
                transactionIds
        ).forEach(prediction ->
                result.put(
                        prediction.getTransaction().getId(),
                        prediction
                )
        );

        return result;
    }

    private TransactionListResponse.TransactionItem mapTransaction(
            Transaction transaction,
            Prediction prediction
    ) {

        return new TransactionListResponse.TransactionItem(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getExternalReference(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getMerchantName(),
                transaction.getMerchantId(),
                transaction.getCategory(),
                transaction.getCountry(),
                transaction.getTransactionTime() != null
                        ? transaction.getTransactionTime().toString()
                        : null,
                transaction.getStatus(),
                prediction != null ? prediction.getId() : null,
                prediction != null
                        ? prediction.getPrediction().intValue()
                        : null,
                prediction != null ? prediction.getRiskScore() : null,
                prediction != null
                        ? prediction.getRiskLevel().name()
                        : null,
                prediction != null ? prediction.getModelVersion() : null,
                prediction != null && prediction.getPredictedAt() != null
                        ? prediction.getPredictedAt().toString()
                        : null
        );
    }

    private TransactionListResponse.TransactionSummary calculateSummary() {

        long totalTransactions = 0;
        long fraudTransactions = 0;
        long lowRiskTransactions = 0;
        long mediumRiskTransactions = 0;
        long highRiskTransactions = 0;
        long unpredictedTransactions = 0;

        int currentPage = 0;
        Page<Transaction> transactionPage;

        do {
            Pageable pageable = PageRequest.of(
                    currentPage,
                    SUMMARY_PAGE_SIZE,
                    Sort.by(Sort.Order.asc("id"))
            );

            transactionPage = transactionRepository.findAll(pageable);

            List<Long> transactionIds = transactionPage.getContent()
                    .stream()
                    .map(Transaction::getId)
                    .toList();

            Map<Long, Prediction> latestPredictions =
                    getLatestPredictions(transactionIds);

            for (Transaction transaction : transactionPage.getContent()) {

                totalTransactions++;

                Prediction prediction =
                        latestPredictions.get(transaction.getId());

                if (prediction == null) {
                    unpredictedTransactions++;
                    continue;
                }

                if (prediction.getPrediction() != null
                        && prediction.getPrediction() == 1) {
                    fraudTransactions++;
                }

                RiskLevel risk = prediction.getRiskLevel();

                if (risk == RiskLevel.LOW) {
                    lowRiskTransactions++;
                } else if (risk == RiskLevel.MEDIUM) {
                    mediumRiskTransactions++;
                } else if (risk == RiskLevel.HIGH) {
                    highRiskTransactions++;
                }
            }

            currentPage++;

        } while (transactionPage.hasNext());

        return new TransactionListResponse.TransactionSummary(
                totalTransactions,
                fraudTransactions,
                lowRiskTransactions,
                mediumRiskTransactions,
                highRiskTransactions,
                unpredictedTransactions
        );
    }
}