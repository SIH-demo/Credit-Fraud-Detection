package com.cfd.backend.dto;

import java.util.Map;

public record MlPredictionRequest(

        String transaction_id,

        Map<String, Object> features,

        String correlation_id

) {}