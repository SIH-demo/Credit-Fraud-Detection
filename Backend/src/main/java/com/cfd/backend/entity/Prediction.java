
package com.cfd.backend.entity;

import com.cfd.backend.enums.RiskLevel;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "predictions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_predictions_correlation_id",
            columnNames = "correlation_id"
        )
    }
)
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "prediction", nullable = false)
    private Byte prediction;

    @Column(name = "risk_score", nullable = false, precision = 8, scale = 6)
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 10)
    private RiskLevel riskLevel;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "threshold_used", nullable = false, precision = 8, scale = 6)
    private BigDecimal thresholdUsed;

    @Column(name = "correlation_id", nullable = false, unique = true, length = 36)
    private String correlationId;

    @Column(name = "predicted_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime predictedAt;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)"
    )
    private LocalDateTime createdAt;

    public Prediction() {
    }

    public Long getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Byte getPrediction() {
        return prediction;
    }

    public void setPrediction(Byte prediction) {
        this.prediction = prediction;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public BigDecimal getThresholdUsed() {
        return thresholdUsed;
    }

    public void setThresholdUsed(BigDecimal thresholdUsed) {
        this.thresholdUsed = thresholdUsed;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public LocalDateTime getPredictedAt() {
        return predictedAt;
    }

    public void setPredictedAt(LocalDateTime predictedAt) {
        this.predictedAt = predictedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
