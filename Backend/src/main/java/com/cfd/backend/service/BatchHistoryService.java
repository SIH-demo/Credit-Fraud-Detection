
package com.cfd.backend.service;

import com.cfd.backend.dto.BatchApiResponse;
import com.cfd.backend.entity.BatchJob;
import com.cfd.backend.repository.BatchJobRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BatchHistoryService {

    private final BatchJobRepository batchJobRepository;

    public BatchHistoryService(BatchJobRepository batchJobRepository) {
        this.batchJobRepository = batchJobRepository;
    }

    @Transactional(readOnly = true)
    public BatchApiResponse<BatchApiResponse.HistoryData> getBatchHistory(
            int page,
            int size
    ) {

        Page<BatchJob> batchPage =
                batchJobRepository.findAllByOrderByCreatedAtDesc(
                        PageRequest.of(page, size)
                );

        List<BatchApiResponse.BatchSummary> batches =
                batchPage.getContent().stream()
                        .map(batch -> new BatchApiResponse.BatchSummary(
                                batch.getBatchId(),
                                batch.getStatus().name(),
                                batch.getTotalRows(),
                                batch.getProcessedRows(),
                                batch.getErrorCount(),
                                batch.getCreatedAt() != null
                                        ? batch.getCreatedAt().toString()
                                        : null,
                                batch.getCompletedAt() != null
                                        ? batch.getCompletedAt().toString()
                                        : null
                        ))
                        .toList();

        return new BatchApiResponse<>(
                new BatchApiResponse.HistoryData(
                        batches,
                        batchPage.getNumber(),
                        batchPage.getSize(),
                        batchPage.getTotalElements(),
                        batchPage.getTotalPages()
                )
        );
    }
}
