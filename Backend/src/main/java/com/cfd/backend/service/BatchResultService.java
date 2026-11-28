
package com.cfd.backend.service;

import com.cfd.backend.dto.BatchApiResponse;
import com.cfd.backend.entity.BatchJob;
import com.cfd.backend.entity.BatchJobItem;
import com.cfd.backend.entity.Prediction;
import com.cfd.backend.entity.Transaction;
import com.cfd.backend.enums.BatchItemStatus;
import com.cfd.backend.repository.BatchJobItemRepository;
import com.cfd.backend.repository.BatchJobRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BatchResultService {

    private final BatchJobRepository batchJobRepository;
    private final BatchJobItemRepository batchJobItemRepository;

    public BatchResultService(
            BatchJobRepository batchJobRepository,
            BatchJobItemRepository batchJobItemRepository
    ) {
        this.batchJobRepository = batchJobRepository;
        this.batchJobItemRepository = batchJobItemRepository;
    }

    @Transactional(readOnly = true)
    public BatchApiResponse<BatchApiResponse.ResultData> getBatchResult(
            String batchId
    ) {

        BatchJob batchJob = batchJobRepository.findByBatchId(batchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Batch not found: " + batchId
                ));

        List<BatchJobItem> items =
                batchJobItemRepository
                        .findByBatchJobIdOrderBySourceRowNumberAsc(
                                batchJob.getId()
                        );

        List<BatchApiResponse.TransactionResult> results =
                new ArrayList<>();

        List<BatchApiResponse.RowError> errors =
                new ArrayList<>();

        List<BatchApiResponse.RowSkipped> skippedRows =
                new ArrayList<>();

        int fraudCount = 0;
        int normalCount = 0;
        int lowRiskCount = 0;
        int mediumRiskCount = 0;
        int highRiskCount = 0;
        int skippedCount = 0;

        for (BatchJobItem item : items) {

            if (item.getProcessingStatus() == BatchItemStatus.FAILED) {
                errors.add(new BatchApiResponse.RowError(
                        item.getSourceRowNumber(),
                        item.getErrorMessage() != null
                                ? item.getErrorMessage()
                                : "Transaction processing failed."
                ));
                continue;
            }

            if (item.getProcessingStatus() == BatchItemStatus.SKIPPED) {
                skippedCount++;

                skippedRows.add(new BatchApiResponse.RowSkipped(
                        item.getSourceRowNumber(),
                        "Duplicate external reference. Existing transaction was not processed again."
                ));
                continue;
            }

            if (item.getProcessingStatus() != BatchItemStatus.COMPLETED) {
                continue;
            }

            Transaction transaction = item.getTransaction();
            Prediction prediction = item.getPrediction();

            if (transaction == null || prediction == null) {
                errors.add(new BatchApiResponse.RowError(
                        item.getSourceRowNumber(),
                        "Saved transaction or prediction is unavailable."
                ));
                continue;
            }

            boolean isFraud = prediction.getPrediction() == 1;

            if (isFraud) {
                fraudCount++;
            } else {
                normalCount++;
            }

            switch (prediction.getRiskLevel()) {
                case LOW -> lowRiskCount++;
                case MEDIUM -> mediumRiskCount++;
                case HIGH -> highRiskCount++;
            }

            results.add(new BatchApiResponse.TransactionResult(
                    transaction.getTransactionReference(),
                    transaction.getAmount().doubleValue(),
                    transaction.getCurrency(),
                    transaction.getTransactionTime().toString(),
                    isFraud,
                    prediction.getRiskLevel().name(),
                    prediction.getRiskScore().doubleValue() * 100,
                    prediction.getModelVersion()
            ));
        }

        int totalTransactions = results.size();

        double fraudPercentage = totalTransactions == 0
                ? 0.0
                : (fraudCount * 100.0) / totalTransactions;

        BatchApiResponse.ResultData data =
                new BatchApiResponse.ResultData(
                        batchJob.getBatchId(),
                        batchJob.getStatus().name(),
                        totalTransactions,
                        batchJob.getTotalRows(),
                        batchJob.getProcessedRows(),
                        batchJob.getErrorCount(),
                        skippedCount,
                        fraudCount,
                        normalCount,
                        fraudPercentage,
                        lowRiskCount,
                        mediumRiskCount,
                        highRiskCount,
                        results,
                        errors,
                        skippedRows
                );

        return new BatchApiResponse<>(data);
    }
}
