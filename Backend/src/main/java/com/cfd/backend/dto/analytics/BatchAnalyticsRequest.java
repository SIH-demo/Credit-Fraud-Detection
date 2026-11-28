package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class BatchAnalyticsRequest {

    @JsonProperty("batch_id")
    private String batchId;

    private List<DashboardTransaction> transactions =
            new ArrayList<>();

    public BatchAnalyticsRequest() {
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public List<DashboardTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(
            List<DashboardTransaction> transactions
    ) {
        this.transactions = transactions;
    }
}