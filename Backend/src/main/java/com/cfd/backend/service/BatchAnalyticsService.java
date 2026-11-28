
package com.cfd.backend.service;

import com.cfd.backend.dto.analytics.AnalyticsApiResponse;
import com.cfd.backend.dto.analytics.BatchAnalyticsRequest;
import com.cfd.backend.dto.analytics.BatchAnalyticsResponse;
import com.cfd.backend.dto.analytics.DashboardTransaction;
import com.cfd.backend.entity.BatchJob;
import com.cfd.backend.entity.BatchJobItem;
import com.cfd.backend.entity.Prediction;
import com.cfd.backend.entity.Transaction;
import com.cfd.backend.enums.BatchItemStatus;
import com.cfd.backend.repository.BatchJobItemRepository;
import com.cfd.backend.repository.BatchJobRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class BatchAnalyticsService {

    private final BatchJobRepository batchJobRepository;
    private final BatchJobItemRepository batchJobItemRepository;
    private final AnalyticsService analyticsService;

    public BatchAnalyticsService(
            BatchJobRepository batchJobRepository,
            BatchJobItemRepository batchJobItemRepository,
            AnalyticsService analyticsService
    ) {
        this.batchJobRepository = batchJobRepository;
        this.batchJobItemRepository = batchJobItemRepository;
        this.analyticsService = analyticsService;
    }

    @Transactional(readOnly = true)
    public AnalyticsApiResponse<BatchAnalyticsResponse> analyzeBatch(
            String batchId
    ) {
        BatchAnalyticsRequest request = buildAnalyticsRequest(batchId);

        return analyticsService.analyzeBatch(request);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> generateBatchReport(String batchId) {

        BatchAnalyticsRequest request = buildAnalyticsRequest(batchId);

        return analyticsService.generateBatchReport(request);
    }

    private BatchAnalyticsRequest buildAnalyticsRequest(String batchId) {

        BatchJob batchJob = batchJobRepository.findByBatchId(batchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Batch not found: " + batchId
                ));

        List<BatchJobItem> items =
                batchJobItemRepository
                        .findByBatchJobIdOrderBySourceRowNumberAsc(
                                batchJob.getId()
                        );

        List<DashboardTransaction> transactions = new ArrayList<>();

        for (BatchJobItem item : items) {

            if (item.getProcessingStatus() != BatchItemStatus.COMPLETED) {
                continue;
            }

            Transaction transaction = item.getTransaction();
            Prediction prediction = item.getPrediction();

            if (transaction == null || prediction == null) {
                continue;
            }

            DashboardTransaction analyticsTransaction =
                    new DashboardTransaction();

            analyticsTransaction.setTransactionId(
                    transaction.getTransactionReference()
            );

            analyticsTransaction.setAmount(
                    transaction.getAmount().doubleValue()
            );

            analyticsTransaction.setCurrency(
                    transaction.getCurrency()
            );

            analyticsTransaction.setTransactionTime(
                    transaction.getTransactionTime()
                            .atOffset(ZoneOffset.UTC)
            );

            analyticsTransaction.setRiskScore(
                    prediction.getRiskScore().doubleValue()
            );

            analyticsTransaction.setRiskLevel(
                    prediction.getRiskLevel().name()
            );

            analyticsTransaction.setPredictedFraud(
                    prediction.getPrediction().intValue()
            );

            analyticsTransaction.setActualFraud(null);

            analyticsTransaction.setModelVersion(
                    prediction.getModelVersion()
            );

            transactions.add(analyticsTransaction);
        }

        if (transactions.isEmpty()) {
            throw new IllegalStateException(
                    "No successfully processed transactions are available "
                            + "for batch " + batchId
            );
        }

        BatchAnalyticsRequest request = new BatchAnalyticsRequest();
        request.setBatchId(batchJob.getBatchId());
        request.setTransactions(transactions);

        return request;
    }
}
