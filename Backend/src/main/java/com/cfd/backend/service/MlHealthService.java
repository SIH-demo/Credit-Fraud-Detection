package com.cfd.backend.service;

import com.cfd.backend.client.MlServiceClient;
import com.cfd.backend.dto.MlHealthResponse;
import org.springframework.stereotype.Service;

@Service
public class MlHealthService {

    private final MlServiceClient mlServiceClient;

    public MlHealthService(MlServiceClient mlServiceClient) {
        this.mlServiceClient = mlServiceClient;
    }

    public MlHealthResponse getHealth() {
        return mlServiceClient.getHealth();
    }
}