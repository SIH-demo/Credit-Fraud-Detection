
package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public class DashboardTransaction {

    @JsonProperty("transaction_id")
    private String transactionId;

    private double amount;

    private String currency = "INR";

    @JsonProperty("transaction_time")
    private OffsetDateTime transactionTime;

    @JsonProperty("risk_score")
    private Double riskScore;

    @JsonProperty("risk_level")
    private String riskLevel;

    @JsonProperty("predicted_fraud")
    private Integer predictedFraud;

    @JsonProperty("actual_fraud")
    private Integer actualFraud;

    @JsonProperty("model_version")
    private String modelVersion;

    public DashboardTransaction() {
    }

    // Getters and setters

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public OffsetDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(OffsetDateTime transactionTime) {
        this.transactionTime = transactionTime;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getPredictedFraud() {
        return predictedFraud;
    }

    public void setPredictedFraud(Integer predictedFraud) {
        this.predictedFraud = predictedFraud;
    }

    public Integer getActualFraud() {
        return actualFraud;
    }

    public void setActualFraud(Integer actualFraud) {
        this.actualFraud = actualFraud;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }
}
