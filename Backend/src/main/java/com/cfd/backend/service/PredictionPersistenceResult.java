package com.cfd.backend.service;

import java.time.LocalDateTime;

import com.cfd.backend.entity.Prediction;
import com.cfd.backend.entity.Transaction;

public record PredictionPersistenceResult(
        Transaction transaction,
        Prediction prediction,
        LocalDateTime predictedAt
) {}