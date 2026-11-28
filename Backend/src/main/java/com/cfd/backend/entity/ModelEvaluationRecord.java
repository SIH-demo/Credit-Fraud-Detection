package com.cfd.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "model_evaluation_records")
public class ModelEvaluationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private ModelEvaluation evaluation;

    @Column(name = "record_reference", nullable = false, length = 100)
    private String recordReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "actual_fraud", nullable = false, columnDefinition = "TINYINT")
    private Integer actualFraud;

    @Column(name = "predicted_fraud", nullable = false, columnDefinition = "TINYINT")
    private Integer predictedFraud;

    @Column(name = "risk_score", precision = 8, scale = 6)
    private BigDecimal riskScore;

    public ModelEvaluationRecord() {
    }

    public Long getId() {
        return id;
    }

    public ModelEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(ModelEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    public String getRecordReference() {
        return recordReference;
    }

    public void setRecordReference(String recordReference) {
        this.recordReference = recordReference;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Integer getActualFraud() {
        return actualFraud;
    }

    public void setActualFraud(Integer actualFraud) {
        this.actualFraud = actualFraud;
    }

    public Integer getPredictedFraud() {
        return predictedFraud;
    }

    public void setPredictedFraud(Integer predictedFraud) {
        this.predictedFraud = predictedFraud;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }
}
