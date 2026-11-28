package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RiskDistribution {

    @JsonProperty("LOW")
    private long low;

    @JsonProperty("MEDIUM")
    private long medium;

    @JsonProperty("HIGH")
    private long high;

    @JsonProperty("UNKNOWN")
    private long unknown;

    public RiskDistribution() {
    }

    public long getLow() {
        return low;
    }

    public void setLow(long low) {
        this.low = low;
    }

    public long getMedium() {
        return medium;
    }

    public void setMedium(long medium) {
        this.medium = medium;
    }

    public long getHigh() {
        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }

    public long getUnknown() {
        return unknown;
    }

    public void setUnknown(long unknown) {
        this.unknown = unknown;
    }
}