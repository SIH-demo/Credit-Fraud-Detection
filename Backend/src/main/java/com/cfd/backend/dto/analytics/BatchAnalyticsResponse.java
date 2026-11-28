package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class BatchAnalyticsResponse {

    @JsonProperty("batch_id")
    private String batchId;

    @JsonProperty("total_rows")
    private long totalRows;

    @JsonProperty("processed_rows")
    private long processedRows;

    @JsonProperty("error_count")
    private long errorCount;

    private DashboardResponse summary;

    private List<SuspiciousTransaction> transactions;

    public BatchAnalyticsResponse() {
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(long totalRows) {
        this.totalRows = totalRows;
    }

    public long getProcessedRows() {
        return processedRows;
    }

    public void setProcessedRows(long processedRows) {
        this.processedRows = processedRows;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public DashboardResponse getSummary() {
        return summary;
    }

    public void setSummary(DashboardResponse summary) {
        this.summary = summary;
    }

    public List<SuspiciousTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(
            List<SuspiciousTransaction> transactions
    ) {
        this.transactions = transactions;
    }
}