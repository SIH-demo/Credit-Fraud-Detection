
package com.cfd.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "model_evaluations",
    indexes = {
        @Index(name = "idx_model_evaluations_version", columnList = "model_version"),
        @Index(name = "idx_model_evaluations_evaluated_at", columnList = "evaluated_at")
    }
)
public class ModelEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "threshold", nullable = false, precision = 8, scale = 6)
    private BigDecimal threshold;

    @Column(name = "evaluation_count", nullable = false)
    private Long evaluationCount;

    @Column(name = "accuracy", precision = 10, scale = 8)
    private BigDecimal accuracy;

    @Column(name = "precision_score", precision = 10, scale = 8)
    private BigDecimal precisionScore;

    @Column(name = "recall_score", precision = 10, scale = 8)
    private BigDecimal recallScore;

    @Column(name = "f1_score", precision = 10, scale = 8)
    private BigDecimal f1Score;

    @Column(name = "roc_auc", precision = 10, scale = 8)
    private BigDecimal rocAuc;

    @Column(name = "average_precision", precision = 10, scale = 8)
    private BigDecimal averagePrecision;

    @Column(name = "score_status", length = 50)
    private String scoreStatus;

    @Column(name = "true_positive")
    private Long truePositive;

    @Column(name = "true_negative")
    private Long trueNegative;

    @Column(name = "false_positive")
    private Long falsePositive;

    @Column(name = "false_negative")
    private Long falseNegative;

    @Column(
        name = "evaluated_at",
        nullable = false,
        columnDefinition = "DATETIME(6)"
    )
    private LocalDateTime evaluatedAt;

    @Column(
        name = "created_at",
        nullable = false,
        insertable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)"
    )
    private LocalDateTime createdAt;

    public ModelEvaluation() {
    }

    public Long getId() {
        return id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public Long getEvaluationCount() {
        return evaluationCount;
    }

    public void setEvaluationCount(Long evaluationCount) {
        this.evaluationCount = evaluationCount;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(BigDecimal accuracy) {
        this.accuracy = accuracy;
    }

    public BigDecimal getPrecisionScore() {
        return precisionScore;
    }

    public void setPrecisionScore(BigDecimal precisionScore) {
        this.precisionScore = precisionScore;
    }

    public BigDecimal getRecallScore() {
        return recallScore;
    }

    public void setRecallScore(BigDecimal recallScore) {
        this.recallScore = recallScore;
    }

    public BigDecimal getF1Score() {
        return f1Score;
    }

    public void setF1Score(BigDecimal f1Score) {
        this.f1Score = f1Score;
    }

    public BigDecimal getRocAuc() {
        return rocAuc;
    }

    public void setRocAuc(BigDecimal rocAuc) {
        this.rocAuc = rocAuc;
    }

    public BigDecimal getAveragePrecision() {
        return averagePrecision;
    }

    public void setAveragePrecision(BigDecimal averagePrecision) {
        this.averagePrecision = averagePrecision;
    }

    public String getScoreStatus() {
        return scoreStatus;
    }

    public void setScoreStatus(String scoreStatus) {
        this.scoreStatus = scoreStatus;
    }

    public Long getTruePositive() {
        return truePositive;
    }

    public void setTruePositive(Long truePositive) {
        this.truePositive = truePositive;
    }

    public Long getTrueNegative() {
        return trueNegative;
    }

    public void setTrueNegative(Long trueNegative) {
        this.trueNegative = trueNegative;
    }

    public Long getFalsePositive() {
        return falsePositive;
    }

    public void setFalsePositive(Long falsePositive) {
        this.falsePositive = falsePositive;
    }

    public Long getFalseNegative() {
        return falseNegative;
    }

    public void setFalseNegative(Long falseNegative) {
        this.falseNegative = falseNegative;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
