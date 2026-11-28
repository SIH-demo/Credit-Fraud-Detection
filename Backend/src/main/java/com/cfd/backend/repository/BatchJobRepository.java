package com.cfd.backend.repository;

import com.cfd.backend.entity.BatchJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;


public interface BatchJobRepository extends JpaRepository<BatchJob, Long> {

    Optional<BatchJob> findByBatchId(String batchId);
    Page<BatchJob> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
