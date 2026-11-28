package com.cfd.backend.specification;

import com.cfd.backend.entity.Prediction;
import com.cfd.backend.entity.Transaction;
import com.cfd.backend.enums.RiskLevel;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<Transaction> filter(
            String search,
            String status,
            String currency,
            String riskLevel
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("transactionReference")), pattern),
                        cb.like(cb.lower(root.get("externalReference")), pattern),
                        cb.like(cb.lower(root.get("merchantName")), pattern),
                        cb.like(cb.lower(root.get("merchantId")), pattern)
                ));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(
                        cb.equal(
                                cb.upper(root.get("status")),
                                status.trim().toUpperCase()
                        )
                );
            }

            if (currency != null && !currency.isBlank()) {
                predicates.add(
                        cb.equal(
                                cb.upper(root.get("currency")),
                                currency.trim().toUpperCase()
                        )
                );
            }

            if (riskLevel != null && !riskLevel.isBlank()) {
                RiskLevel requestedRisk;

                try {
                    requestedRisk = RiskLevel.valueOf(
                            riskLevel.trim().toUpperCase()
                    );
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException(
                            "Invalid risk level. Allowed values: LOW, MEDIUM, HIGH"
                    );
                }

                Subquery<LocalDateTime> latestPredictionTime =
                        query.subquery(LocalDateTime.class);

                Root<Prediction> latestPrediction =
                        latestPredictionTime.from(Prediction.class);

                latestPredictionTime.select(
        cb.greatest(
                latestPrediction.<LocalDateTime>get("predictedAt")
        )
);

                latestPredictionTime.where(
                        cb.equal(
                                latestPrediction.get("transaction").get("id"),
                                root.get("id")
                        )
                );

                Subquery<Integer> matchingRisk = query.subquery(Integer.class);

                Root<Prediction> prediction =
                        matchingRisk.from(Prediction.class);

                matchingRisk.select(cb.literal(1));

                matchingRisk.where(
                        cb.equal(
                                prediction.get("transaction").get("id"),
                                root.get("id")
                        ),
                        cb.equal(
                                prediction.get("predictedAt"),
                                latestPredictionTime
                        ),
                        cb.equal(prediction.get("riskLevel"), requestedRisk)
                );

                predicates.add(cb.exists(matchingRisk));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}