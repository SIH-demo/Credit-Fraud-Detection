package com.cfd.backend.controller;

import com.cfd.backend.dto.TransactionListResponse;
import com.cfd.backend.service.TransactionService;
import com.cfd.backend.dto.TransactionDetailsResponse;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Validated
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<TransactionListResponse> getTransactions(

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page number cannot be negative.")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Page size must be at least 1.")
            @Max(value = 100, message = "Page size cannot exceed 100.")
            int size,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            String status,

            @RequestParam(required = false)
            String currency,

            @RequestParam(required = false)
            String riskLevel
    ) {

        TransactionListResponse response =
                transactionService.getTransactions(
                        page,
                        size,
                        search,
                        status,
                        currency,
                        riskLevel
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
public ResponseEntity<TransactionDetailsResponse> getTransactionDetails(
        @PathVariable
        @Min(value = 1, message = "Transaction ID must be positive.")
        Long id
) {

    TransactionDetailsResponse response =
            transactionService.getTransactionDetails(id);

    return ResponseEntity.ok(response);
}
}