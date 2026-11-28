package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScoreMetrics {

    @JsonProperty("roc_auc")
    private Double rocAuc;

    @JsonProperty("average_precision")
    private Double averagePrecision;

    public ScoreMetrics() {}

    public Double getRocAuc() {
        return rocAuc;
    }

    public void setRocAuc(Double rocAuc) {
        this.rocAuc = rocAuc;
    }

    public Double getAveragePrecision() {
        return averagePrecision;
    }

    public void setAveragePrecision(Double averagePrecision) {
        this.averagePrecision = averagePrecision;
    }
}
