
package com.cfd.backend.entity;

import com.cfd.backend.enums.BatchJobStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "batch_jobs",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_batch_jobs_batch_id",
            columnNames = "batch_id"
        )
    }
)
public class BatchJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false, unique = true, length = 100)
    private String batchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BatchJobStatus status;

    @Column(name = "total_rows", nullable = false)
    private Integer totalRows = 0;

    @Column(name = "processed_rows", nullable = false)
    private Integer processedRows = 0;

    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)"
    )
    private LocalDateTime createdAt;

    @Column(name = "completed_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime completedAt;

    public BatchJob() {
    }

    public Long getId() {
        return id;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public BatchJobStatus getStatus() {
        return status;
    }

    public void setStatus(BatchJobStatus status) {
        this.status = status;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getProcessedRows() {
        return processedRows;
    }

    public void setProcessedRows(Integer processedRows) {
        this.processedRows = processedRows;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
