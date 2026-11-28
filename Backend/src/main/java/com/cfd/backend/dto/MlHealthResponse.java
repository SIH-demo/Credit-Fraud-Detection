package com.cfd.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MlHealthResponse(
        String status,
        String service,

        @JsonProperty("model_loaded")
        Boolean modelLoaded,

        @JsonProperty("model_version")
        String modelVersion,

        @JsonProperty("prediction_threshold")
        Double predictionThreshold
) {
}