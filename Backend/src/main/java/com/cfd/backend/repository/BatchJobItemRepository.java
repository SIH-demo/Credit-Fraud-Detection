package com.cfd.backend.repository;

import com.cfd.backend.entity.BatchJobItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchJobItemRepository
        extends JpaRepository<BatchJobItem, Long> {

    List<BatchJobItem> findByBatchJobIdOrderBySourceRowNumberAsc(Long batchJobId);
}
