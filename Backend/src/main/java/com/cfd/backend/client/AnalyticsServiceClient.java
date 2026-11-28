
package com.cfd.backend.client;

import com.cfd.backend.dto.analytics.AnalyticsApiResponse;
import com.cfd.backend.dto.analytics.DashboardRequest;
import com.cfd.backend.dto.analytics.DashboardResponse;
import com.cfd.backend.dto.analytics.ModelEvaluationRequest;
import com.cfd.backend.dto.analytics.ModelEvaluationResponse;
import com.cfd.backend.dto.analytics.BatchAnalyticsRequest;
import com.cfd.backend.dto.analytics.BatchAnalyticsResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;

import org.springframework.http.ResponseEntity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

@Component
public class AnalyticsServiceClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AnalyticsServiceClient(
            RestClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${analytics.service.url}") String analyticsServiceUrl
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        this.restClient = builder
                .baseUrl(analyticsServiceUrl)
                .requestFactory(requestFactory)
                .build();

        this.objectMapper = objectMapper;
    }

    public AnalyticsApiResponse<DashboardResponse> getDashboardSummary(
            DashboardRequest request
    ) {
        try {
            String json = objectMapper.writeValueAsString(request);

            return restClient.post()
                    .uri("/internal/v1/dashboard/summary")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(new ParameterizedTypeReference<
                        AnalyticsApiResponse<DashboardResponse>
                    >() {});

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize Analytics dashboard request",
                    exception
            );
        }
    }
    public AnalyticsApiResponse<ModelEvaluationResponse> evaluateModel(
        ModelEvaluationRequest request
) {
    try {
        String json = objectMapper.writeValueAsString(request);

        return restClient.post()
                .uri("/internal/v1/model/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .body(new ParameterizedTypeReference<
                    AnalyticsApiResponse<ModelEvaluationResponse>
                >() {});

    } catch (JsonProcessingException exception) {
        throw new IllegalStateException(
                "Failed to serialize Analytics model evaluation request",
                exception
        );
    }
}

    public AnalyticsApiResponse<BatchAnalyticsResponse> analyzeBatch(
        BatchAnalyticsRequest request
) {
    try {
        String json = objectMapper.writeValueAsString(request);

        return restClient.post()
                .uri("/internal/v1/batches/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .body(new ParameterizedTypeReference<
                    AnalyticsApiResponse<BatchAnalyticsResponse>
                >() {});

    } catch (JsonProcessingException exception) {
        throw new IllegalStateException(
                "Failed to serialize Analytics batch request",
                exception
        );
    }
}

    public ResponseEntity<byte[]> generateBatchReport(
        BatchAnalyticsRequest request
) {
    try {
        String json = objectMapper.writeValueAsString(request);

        return restClient.post()
                .uri("/internal/v1/batches/report")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.parseMediaType("text/csv"))
                .body(json)
                .retrieve()
                .toEntity(byte[].class);

    } catch (JsonProcessingException exception) {
        throw new IllegalStateException(
                "Failed to serialize Analytics batch report request",
                exception
        );
    }
}
}
