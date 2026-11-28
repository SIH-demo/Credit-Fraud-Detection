
package com.cfd.backend.service;

import com.cfd.backend.dto.TransactionPredictionRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BatchCsvParser {

    private static final int MAX_ROWS = 10000;

    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "amount",
            "currency",
            "transactionTime",
            "merchantName",
            "merchant",
            "step",
            "age",
            "gender",
            "category",
            "country",
            "status"
    );

    private static final Set<String> ALLOWED_HEADERS = Set.of(
            "externalReference",
            "amount",
            "currency",
            "transactionTime",
            "merchantName",
            "merchant",
            "step",
            "age",
            "gender",
            "category",
            "country",
            "maskedCardLast4",
            "status"
    );

    private final Validator validator;

    public BatchCsvParser(Validator validator) {
        this.validator = validator;
    }

    public ParseResult parse(InputStream inputStream) throws IOException {

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .setAllowMissingColumnNames(false)
                .setDuplicateHeaderMode(DuplicateHeaderMode.DISALLOW)
                .get();

        List<ParsedRow> rows = new ArrayList<>();

        try (Reader reader = new InputStreamReader(
                inputStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVParser.parse(reader, format)) {

            List<String> headers = parser.getHeaderNames();

            if (headers.isEmpty()) {
                throw new IllegalArgumentException(
                        "CSV file is empty or missing its header row."
                );
            }

            validateHeaders(headers);

            for (CSVRecord record : parser) {

                if (rows.size() >= MAX_ROWS) {
                    throw new IllegalArgumentException(
                            "CSV file cannot contain more than "
                                    + MAX_ROWS + " data rows."
                    );
                }

                long sourceRowNumber = record.getRecordNumber() + 1;

                Map<String, String> rawPayload = record.toMap();

                if (!record.isConsistent()) {
                    rows.add(new ParsedRow(
                            sourceRowNumber,
                            null,
                            rawPayload,
                            "Column count does not match the CSV header."
                    ));
                    continue;
                }

                try {
                    TransactionPredictionRequest request =
                            toRequest(record);

                    Set<ConstraintViolation<TransactionPredictionRequest>>
                            violations = validator.validate(request);

                    if (!violations.isEmpty()) {

                        String errorMessage = violations.stream()
                                .map(v -> v.getPropertyPath()
                                        + ": " + v.getMessage())
                                .sorted()
                                .reduce((a, b) -> a + "; " + b)
                                .orElse("Validation failed.");

                        rows.add(new ParsedRow(
                                sourceRowNumber,
                                null,
                                rawPayload,
                                errorMessage
                        ));

                        continue;
                    }

                    rows.add(new ParsedRow(
                            sourceRowNumber,
                            request,
                            rawPayload,
                            null
                    ));

                } catch (RuntimeException exception) {

                    rows.add(new ParsedRow(
                            sourceRowNumber,
                            null,
                            rawPayload,
                            "Invalid row: " + exception.getMessage()
                    ));
                }
            }
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "CSV file does not contain any transaction rows."
            );
        }

        return new ParseResult(List.copyOf(rows));
    }

    private void validateHeaders(List<String> headers) {

        Set<String> suppliedHeaders = Set.copyOf(headers);

        Set<String> missingHeaders = new java.util.HashSet<>(
                REQUIRED_HEADERS
        );

        missingHeaders.removeAll(suppliedHeaders);

        if (!missingHeaders.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required CSV columns: " + missingHeaders
            );
        }

        Set<String> unknownHeaders = new java.util.HashSet<>(
                suppliedHeaders
        );

        unknownHeaders.removeAll(ALLOWED_HEADERS);

        if (!unknownHeaders.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unknown CSV columns: " + unknownHeaders
            );
        }
    }

    private TransactionPredictionRequest toRequest(CSVRecord record) {

        return new TransactionPredictionRequest(
                optionalValue(record, "externalReference"),
                new BigDecimal(requiredValue(record, "amount")),
                requiredValue(record, "currency"),
                OffsetDateTime.parse(
                        requiredValue(record, "transactionTime")
                ),
                requiredValue(record, "merchantName"),
                requiredValue(record, "merchant"),
                Integer.valueOf(requiredValue(record, "step")),
                requiredValue(record, "age"),
                requiredValue(record, "gender"),
                requiredValue(record, "category"),
                requiredValue(record, "country"),
                optionalValue(record, "maskedCardLast4"),
                requiredValue(record, "status")
        );
    }

    private String requiredValue(CSVRecord record, String column) {

        String value = record.get(column).trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(
                    column + " cannot be empty."
            );
        }

        return value;
    }

    private String optionalValue(CSVRecord record, String column) {

        if (!record.isMapped(column)) {
            return null;
        }

        String value = record.get(column).trim();

        return value.isEmpty() ? null : value;
    }

    public record ParsedRow(
            long sourceRowNumber,
            TransactionPredictionRequest request,
            Map<String, String> rawPayload,
            String errorMessage
    ) {
        public boolean isValid() {
            return request != null && errorMessage == null;
        }
    }

    public record ParseResult(List<ParsedRow> rows) {

        public int totalRows() {
            return rows.size();
        }

        public long validRows() {
            return rows.stream()
                    .filter(ParsedRow::isValid)
                    .count();
        }

        public long invalidRows() {
            return rows.stream()
                    .filter(row -> !row.isValid())
                    .count();
        }
    }
}
