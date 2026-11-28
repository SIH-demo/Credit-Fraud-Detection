package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EvaluationTransaction {

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("actual_fraud")
    private Integer actualFraud;

    @JsonProperty("predicted_fraud")
    private Integer predictedFraud;

    @JsonProperty("risk_score")
    private Double riskScore;

    public EvaluationTransaction() {}

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }
}
