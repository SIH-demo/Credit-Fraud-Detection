
package com.cfd.backend.service;

import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cfd.backend.client.AnalyticsServiceClient;
import com.cfd.backend.dto.analytics.AnalyticsApiResponse;
import com.cfd.backend.dto.analytics.BatchAnalyticsRequest;
import com.cfd.backend.dto.analytics.BatchAnalyticsResponse;
import com.cfd.backend.dto.analytics.DashboardRequest;
import com.cfd.backend.dto.analytics.DashboardResponse;
import com.cfd.backend.dto.analytics.DashboardTransaction;
import com.cfd.backend.dto.analytics.ModelEvaluationRequest;
import com.cfd.backend.dto.analytics.ModelEvaluationResponse;
import com.cfd.backend.entity.Prediction;
import com.cfd.backend.entity.Transaction;
import com.cfd.backend.repository.PredictionRepository;

@Service
public class AnalyticsService {

    private static final int PAGE_SIZE = 500;

    private final AnalyticsServiceClient analyticsServiceClient;
    private final PredictionRepository predictionRepository;

    public AnalyticsService(
            AnalyticsServiceClient analyticsServiceClient,
            PredictionRepository predictionRepository
    ) {
        this.analyticsServiceClient = analyticsServiceClient;
        this.predictionRepository = predictionRepository;
    }

    public AnalyticsApiResponse<DashboardResponse> getDashboardSummary(
            DashboardRequest filters
    ) {
        // Keep the latest prediction for each transaction.
        Map<Long, Prediction> latestPredictions = new LinkedHashMap<>();

        int pageNumber = 0;
        Page<Prediction> predictionPage;

        do {
            predictionPage =
                    predictionRepository.findAllByOrderByPredictedAtDesc(
                            PageRequest.of(pageNumber, PAGE_SIZE)
                    );

            for (Prediction prediction : predictionPage.getContent()) {
                Transaction transaction = prediction.getTransaction();

                latestPredictions.putIfAbsent(
                        transaction.getId(),
                        prediction
                );
            }

            pageNumber++;

        } while (predictionPage.hasNext());

        // Convert persisted records into Analytics DTOs.
        DashboardRequest analyticsRequest = new DashboardRequest();

        for (Prediction prediction : latestPredictions.values()) {
            Transaction transaction = prediction.getTransaction();

            DashboardTransaction dashboardTransaction =
                    new DashboardTransaction();

            dashboardTransaction.setTransactionId(
                    transaction.getTransactionReference()
            );

            dashboardTransaction.setAmount(
                    transaction.getAmount().doubleValue()
            );

            dashboardTransaction.setCurrency(
                    transaction.getCurrency()
            );

            dashboardTransaction.setTransactionTime(
                    transaction.getTransactionTime()
                            .atOffset(ZoneOffset.UTC)
            );

            dashboardTransaction.setRiskScore(
                    prediction.getRiskScore().doubleValue()
            );

            dashboardTransaction.setRiskLevel(
                    prediction.getRiskLevel().name()
            );

            dashboardTransaction.setPredictedFraud(
                    prediction.getPrediction().intValue()
            );

            dashboardTransaction.setModelVersion(
                    prediction.getModelVersion()
            );

            dashboardRequestTransaction(
                    analyticsRequest,
                    dashboardTransaction
            );
        }

        // Preserve optional date filters supplied by the caller.
        if (filters != null) {
            analyticsRequest.setStartTime(filters.getStartTime());
            analyticsRequest.setEndTime(filters.getEndTime());
        }

        return analyticsServiceClient.getDashboardSummary(
                analyticsRequest
        );
    }

    private void dashboardRequestTransaction(
            DashboardRequest request,
            DashboardTransaction transaction
    ) {
        request.getTransactions().add(transaction);
    }

    public AnalyticsApiResponse<ModelEvaluationResponse> evaluateModel(
            ModelEvaluationRequest request
    ) {
        return analyticsServiceClient.evaluateModel(request);
    }

    public AnalyticsApiResponse<BatchAnalyticsResponse> analyzeBatch(
            BatchAnalyticsRequest request
    ) {
        return analyticsServiceClient.analyzeBatch(request);
    }

    public ResponseEntity<byte[]> generateBatchReport(
            BatchAnalyticsRequest request
    ) {
        return analyticsServiceClient.generateBatchReport(request);
    }
}
