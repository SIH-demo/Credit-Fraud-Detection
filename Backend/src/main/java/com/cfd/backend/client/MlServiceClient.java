package com.cfd.backend.client;

import com.cfd.backend.dto.MlPredictionRequest;
import com.cfd.backend.dto.MlPredictionResponse;
import com.cfd.backend.dto.MlHealthResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MlServiceClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MlServiceClient(
            RestClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${ml.service.url}") String mlServiceUrl
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        this.restClient = builder
                .baseUrl(mlServiceUrl)
                .requestFactory(requestFactory)
                .build();

        this.objectMapper = objectMapper;
    }

    public MlPredictionResponse predict(
            MlPredictionRequest request
    ) {
        try {
            String json = objectMapper.writeValueAsString(request);

            System.out.println("ML REQUEST JSON: " + json);

            return restClient.post()
                    .uri("/internal/v1/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .body(MlPredictionResponse.class);

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize ML prediction request",
                    exception
            );
        }
    }

    public MlHealthResponse getHealth() {
    return restClient.get()
            .uri("/health")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(MlHealthResponse.class);
}
}