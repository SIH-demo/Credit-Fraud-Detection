
package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CurrencySummary {

    @JsonProperty("transaction_count")
    private long transactionCount;

    @JsonProperty("total_amount")
    private double totalAmount;

    @JsonProperty("average_transaction_amount")
    private double averageTransactionAmount;

    public CurrencySummary() {
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

    public double getAverageTransactionAmount() {
        return averageTransactionAmount;
    }

    public void setAverageTransactionAmount(
            double averageTransactionAmount
    ) {
        this.averageTransactionAmount = averageTransactionAmount;
    }
}
