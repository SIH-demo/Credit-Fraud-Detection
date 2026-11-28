
package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardRequest {

    private List<DashboardTransaction> transactions =
            new ArrayList<>();

    @JsonProperty("start_time")
    private OffsetDateTime startTime;

    @JsonProperty("end_time")
    private OffsetDateTime endTime;

    public DashboardRequest() {
    }

    public List<DashboardTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(
            List<DashboardTransaction> transactions
    ) {
        this.transactions = transactions;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }
}
