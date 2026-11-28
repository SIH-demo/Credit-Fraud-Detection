
package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class DailyTrend {

    private LocalDate date;

    private String currency;

    @JsonProperty("transaction_count")
    private long transactionCount;

    @JsonProperty("total_amount")
    private double totalAmount;

    @JsonProperty("predicted_fraud_count")
    private long predictedFraudCount;

    public DailyTrend() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getPredictedFraudCount() {
        return predictedFraudCount;
    }

    public void setPredictedFraudCount(long predictedFraudCount) {
        this.predictedFraudCount = predictedFraudCount;
    }
}
