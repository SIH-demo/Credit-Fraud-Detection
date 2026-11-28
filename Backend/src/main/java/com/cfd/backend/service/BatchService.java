
package com.cfd.backend.service;

import com.cfd.backend.entity.BatchJob;
import com.cfd.backend.entity.BatchJobItem;
import com.cfd.backend.enums.BatchItemStatus;
import com.cfd.backend.enums.BatchJobStatus;
import com.cfd.backend.repository.BatchJobItemRepository;
import com.cfd.backend.repository.BatchJobRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BatchService {

    private final BatchJobRepository batchJobRepository;
    private final BatchJobItemRepository batchJobItemRepository;
    private final ObjectMapper objectMapper;

    public BatchService(
            BatchJobRepository batchJobRepository,
            BatchJobItemRepository batchJobItemRepository,
            ObjectMapper objectMapper
    ) {
        this.batchJobRepository = batchJobRepository;
        this.batchJobItemRepository = batchJobItemRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BatchJob createBatch(
            BatchCsvParser.ParseResult parseResult
    ) {

        // 1. Create batch metadata.

        BatchJob batchJob = new BatchJob();

        batchJob.setBatchId(
                "BATCH-" + UUID.randomUUID()
        );

        batchJob.setStatus(BatchJobStatus.RECEIVED);
        batchJob.setTotalRows(parseResult.totalRows());
        batchJob.setProcessedRows(0);
        batchJob.setErrorCount(
                Math.toIntExact(parseResult.invalidRows())
        );

        BatchJob savedBatch =
                batchJobRepository.save(batchJob);


        // 2. Create one batch item for every CSV row.

        List<BatchJobItem> items = new ArrayList<>();

        for (BatchCsvParser.ParsedRow row : parseResult.rows()) {

            BatchJobItem item = new BatchJobItem();

            item.setBatchJob(savedBatch);

            item.setSourceRowNumber(
                    Math.toIntExact(row.sourceRowNumber())
            );

            if (row.isValid()) {

                item.setProcessingStatus(BatchItemStatus.PENDING);

            } else {

                item.setProcessingStatus(BatchItemStatus.FAILED);
                item.setErrorMessage(row.errorMessage());
            }

            try {
                item.setRawPayload(
                        objectMapper.writeValueAsString(
                                row.rawPayload()
                        )
                );
            } catch (JsonProcessingException exception) {
                throw new IllegalStateException(
                        "Could not serialize CSV row "
                                + row.sourceRowNumber(),
                        exception
                );
            }

            items.add(item);
        }

        

        batchJobItemRepository.saveAll(items);

        return savedBatch;
    }

    @Transactional
    public void saveBatch(BatchJob batchJob) {
    batchJobRepository.save(batchJob);
   }
}
