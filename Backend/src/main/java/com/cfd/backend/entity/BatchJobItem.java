
package com.cfd.backend.entity;

import com.cfd.backend.enums.BatchItemStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "batch_job_items",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_batch_job_row",
            columnNames = {"batch_job_id", "source_row_number"}
        )
    }
)
public class BatchJobItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_job_id", nullable = false)
    private BatchJob batchJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediction_id")
    private Prediction prediction;

    @Column(name = "source_row_number", nullable = false)
    private Integer sourceRowNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private BatchItemStatus processingStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "JSON")
    private String rawPayload;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)"
    )
    private LocalDateTime createdAt;

    public BatchJobItem() {
    }

    public Long getId() {
        return id;
    }

    public BatchJob getBatchJob() {
        return batchJob;
    }

    public void setBatchJob(BatchJob batchJob) {
        this.batchJob = batchJob;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    public Integer getSourceRowNumber() {
        return sourceRowNumber;
    }

    public void setSourceRowNumber(Integer sourceRowNumber) {
        this.sourceRowNumber = sourceRowNumber;
    }

    public BatchItemStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(BatchItemStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
