
package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class DashboardResponse {

    @JsonProperty("total_transactions")
    private long totalTransactions;

    @JsonProperty("total_transaction_amount")
    private Double totalTransactionAmount;

    @JsonProperty("average_transaction_amount")
    private Double averageTransactionAmount;

    @JsonProperty("currency_summary")
    private Map<String, CurrencySummary> currencySummary;

    @JsonProperty("predicted_fraud_transactions")
    private long predictedFraudTransactions;

    @JsonProperty("risk_distribution")
    private RiskDistribution riskDistribution;

    @JsonProperty("daily_trend")
    private List<DailyTrend> dailyTrend;

    @JsonProperty("recent_suspicious_transactions")
    private List<SuspiciousTransaction> recentSuspiciousTransactions;

    public DashboardResponse() {
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Double getTotalTransactionAmount() {
        return totalTransactionAmount;
    }

    public void setTotalTransactionAmount(Double totalTransactionAmount) {
        this.totalTransactionAmount = totalTransactionAmount;
    }

    public Double getAverageTransactionAmount() {
        return averageTransactionAmount;
    }

    public void setAverageTransactionAmount(Double averageTransactionAmount) {
        this.averageTransactionAmount = averageTransactionAmount;
    }

    public Map<String, CurrencySummary> getCurrencySummary() {
        return currencySummary;
    }

    public void setCurrencySummary(
            Map<String, CurrencySummary> currencySummary
    ) {
        this.currencySummary = currencySummary;
    }

    public long getPredictedFraudTransactions() {
        return predictedFraudTransactions;
    }

    public void setPredictedFraudTransactions(
            long predictedFraudTransactions
    ) {
        this.predictedFraudTransactions = predictedFraudTransactions;
    }

    public RiskDistribution getRiskDistribution() {
        return riskDistribution;
    }

    public void setRiskDistribution(RiskDistribution riskDistribution) {
        this.riskDistribution = riskDistribution;
    }

    public List<DailyTrend> getDailyTrend() {
        return dailyTrend;
    }

    public void setDailyTrend(List<DailyTrend> dailyTrend) {
        this.dailyTrend = dailyTrend;
    }

    public List<SuspiciousTransaction> getRecentSuspiciousTransactions() {
        return recentSuspiciousTransactions;
    }

    public void setRecentSuspiciousTransactions(
            List<SuspiciousTransaction> recentSuspiciousTransactions
    ) {
        this.recentSuspiciousTransactions = recentSuspiciousTransactions;
    }
}
