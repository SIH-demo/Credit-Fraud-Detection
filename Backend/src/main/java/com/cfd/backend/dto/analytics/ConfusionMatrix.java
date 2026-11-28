package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfusionMatrix {

    @JsonProperty("true_positive")
    private long truePositive;

    @JsonProperty("true_negative")
    private long trueNegative;

    @JsonProperty("false_positive")
    private long falsePositive;

    @JsonProperty("false_negative")
    private long falseNegative;

    public ConfusionMatrix() {}

    public long getTruePositive() {
        return truePositive;
    }

    public void setTruePositive(long truePositive) {
        this.truePositive = truePositive;
    }

    public long getTrueNegative() {
        return trueNegative;
    }

    public void setTrueNegative(long trueNegative) {
        this.trueNegative = trueNegative;
    }

    public long getFalsePositive() {
        return falsePositive;
    }

    public void setFalsePositive(long falsePositive) {
        this.falsePositive = falsePositive;
    }

    public long getFalseNegative() {
        return falseNegative;
    }

    public void setFalseNegative(long falseNegative) {
        this.falseNegative = falseNegative;
    }
}
