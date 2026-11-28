package com.cfd.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Max;

import java.time.OffsetDateTime;

public record TransactionPredictionRequest(

        String externalReference,

        @NotNull(message = "Transaction amount is required")
        @DecimalMin(value = "0.0", inclusive = false,
                message = "Transaction amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Transaction time is required")
        OffsetDateTime transactionTime,

        @NotBlank(message = "Merchant name is required")
        String merchantName,

        @NotBlank(message = "Merchant ID is required")
        String merchant,

        @NotNull(message = "Step is required")
        @PositiveOrZero(message = "Step cannot be negative")
        @Max(value = 179, message = "Step cannot exceed 179")
        Integer step,

        @NotBlank(message = "Age is required")
        @Pattern(regexp = "0|1|2|3|4|5|6|U",
                message = "Invalid age category")
        String age,

        @NotBlank(message = "Gender is required")
        @Pattern(regexp = "E|F|M|U",
                message = "Invalid gender")
        String gender,

        @NotBlank(message = "Category is required")
        @Pattern(
        regexp = "es_barsandrestaurants|es_contents|es_fashion|es_food|es_health|es_home|es_hotelservices|es_hyper|es_leisure|es_otherservices|es_sportsandtoys|es_tech|es_transportation|es_travel|es_wellnessandbeauty",
        message = "Invalid transaction category"
        )
        String category,

        @NotBlank(message = "Country is required")
        String country,

        @Pattern(regexp = "^$|^[0-9]{4}$",
                message = "Masked card must contain exactly four digits")
        String maskedCardLast4,

        @NotBlank(message = "Transaction status is required")
        String status

        
) {}