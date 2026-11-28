
package com.cfd.backend.service;

import com.cfd.backend.entity.BatchJob;
import com.cfd.backend.entity.BatchJobItem;
import com.cfd.backend.entity.Prediction;
import com.cfd.backend.entity.Transaction;
import com.cfd.backend.enums.BatchItemStatus;
import com.cfd.backend.enums.BatchJobStatus;
import com.cfd.backend.repository.BatchJobItemRepository;
import com.cfd.backend.repository.BatchJobRepository;
import com.cfd.backend.repository.PredictionRepository;
import com.cfd.backend.repository.TransactionRepository;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

@Service
public class BatchProcessingService {

    private final BatchJobRepository batchJobRepository;
    private final BatchJobItemRepository batchJobItemRepository;
    private final PredictionService predictionService;
    private final TransactionRepository transactionRepository;
    private final PredictionRepository predictionRepository;

    public BatchProcessingService(
            BatchJobRepository batchJobRepository,
            BatchJobItemRepository batchJobItemRepository,
            PredictionService predictionService,
            TransactionRepository transactionRepository,
            PredictionRepository predictionRepository
    ) {
        this.batchJobRepository = batchJobRepository;
        this.batchJobItemRepository = batchJobItemRepository;
        this.predictionService = predictionService;
        this.transactionRepository = transactionRepository;
        this.predictionRepository = predictionRepository;
    }

    @Async("batchTaskExecutor")
    public void processBatch(
            String batchId,
            BatchCsvParser.ParseResult parseResult
    ) {
        BatchJob batchJob = batchJobRepository.findByBatchId(batchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Batch not found: " + batchId
                ));

        batchJob.setStatus(BatchJobStatus.PROCESSING);
        batchJobRepository.save(batchJob);

        var items = batchJobItemRepository
                .findByBatchJobIdOrderBySourceRowNumberAsc(batchJob.getId());

        Map<Long, BatchJobItem> itemMap = new HashMap<>();

        for (BatchJobItem item : items) {
            itemMap.put(item.getSourceRowNumber().longValue(), item);
        }

        // Track external references within this CSV upload.
        Set<String> seenExternalReferences = new HashSet<>();

        for (BatchCsvParser.ParsedRow row : parseResult.rows()) {

            if (!row.isValid()) {
                continue;
            }

            BatchJobItem item = itemMap.get(row.sourceRowNumber());

            if (item == null) {
                batchJob.setErrorCount(batchJob.getErrorCount() + 1);
                batchJobRepository.save(batchJob);
                continue;
            }

            try {
                item.setProcessingStatus(BatchItemStatus.PROCESSING);
                batchJobItemRepository.save(item);

                String externalReference =
                        row.request().externalReference();

                if (StringUtils.hasText(externalReference)) {

                    externalReference = externalReference.trim();

                    boolean duplicateInCurrentBatch =
                            !seenExternalReferences.add(externalReference);

                    List<Transaction> existingTransactions =
                            transactionRepository.findByExternalReference(
                                    externalReference
                            );

                    if (duplicateInCurrentBatch
                            || !existingTransactions.isEmpty()) {

                        if (!existingTransactions.isEmpty()) {

                            Transaction existingTransaction =
                                    existingTransactions.get(0);

                            item.setTransaction(existingTransaction);

                            List<Prediction> existingPredictions =
                                    predictionRepository
                                            .findByTransactionIdOrderByPredictedAtDesc(
                                                    existingTransaction.getId()
                                            );

                            if (!existingPredictions.isEmpty()) {
                                item.setPrediction(
                                        existingPredictions.get(0)
                                );
                            }
                        }

                        item.setProcessingStatus(BatchItemStatus.SKIPPED);
                        item.setErrorMessage(null);

                        batchJobItemRepository.save(item);
                        batchJobRepository.save(batchJob);

                        continue;
                    }
                }

                PredictionPersistenceResult result =
                        predictionService.predictForBatch(row.request());

                item.setTransaction(result.transaction());
                item.setPrediction(result.prediction());
                item.setProcessingStatus(BatchItemStatus.COMPLETED);
                item.setErrorMessage(null);

                batchJobItemRepository.save(item);

                batchJob.setProcessedRows(
                        batchJob.getProcessedRows() + 1
                );

            } catch (Exception exception) {

                item.setProcessingStatus(BatchItemStatus.FAILED);
                item.setErrorMessage(
                        exception.getMessage() != null
                                ? exception.getMessage()
                                : "Transaction processing failed."
                );

                batchJobItemRepository.save(item);

                batchJob.setErrorCount(batchJob.getErrorCount() + 1);
            }

            batchJobRepository.save(batchJob);
        }

        if (batchJob.getErrorCount() > 0) {

            if (batchJob.getProcessedRows() > 0) {
                batchJob.setStatus(BatchJobStatus.PARTIAL);
            } else {
                batchJob.setStatus(BatchJobStatus.FAILED);
            }

        } else {
            // A batch containing only skipped duplicates is still completed.
            batchJob.setStatus(BatchJobStatus.COMPLETED);
        }

        batchJob.setCompletedAt(LocalDateTime.now());
        batchJobRepository.save(batchJob);
    }
}
