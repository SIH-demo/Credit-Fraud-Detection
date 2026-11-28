package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class ModelEvaluationRequest {

    private List<EvaluationTransaction> transactions =
            new ArrayList<>();

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_version")
    private String modelVersion;

    private Double threshold;

    @JsonProperty("evaluated_at")
    private OffsetDateTime evaluatedAt = OffsetDateTime.now(ZoneOffset.UTC);

    public ModelEvaluationRequest() {}

    public List<EvaluationTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(
            List<EvaluationTransaction> transactions
    ) {
        this.transactions = transactions;
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

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public OffsetDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(OffsetDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
