package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassificationMetrics {

    private Double accuracy;
    private Double precision;
    private Double recall;

    @JsonProperty("f1_score")
    private Double f1Score;

    public ClassificationMetrics() {}

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getPrecision() {
        return precision;
    }

    public void setPrecision(Double precision) {
        this.precision = precision;
    }

    public Double getRecall() {
        return recall;
    }

    public void setRecall(Double recall) {
        this.recall = recall;
    }

    public Double getF1Score() {
        return f1Score;
    }

    public void setF1Score(Double f1Score) {
        this.f1Score = f1Score;
    }
}
