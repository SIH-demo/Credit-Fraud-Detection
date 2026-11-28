
package com.cfd.backend.specification;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.cfd.backend.entity.Prediction;
import com.cfd.backend.entity.Transaction;
import com.cfd.backend.enums.RiskLevel;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public final class PredictionSpecification {

    private PredictionSpecification() {
    }

    public static Specification<Prediction> hasRiskLevel(
            RiskLevel riskLevel
    ) {
        return (root, query, criteriaBuilder) -> {

            if (riskLevel == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("riskLevel"),
                    riskLevel
            );
        };
    }

    public static Specification<Prediction> hasTransactionReference(
            String transactionReference
    ) {
        return (root, query, criteriaBuilder) -> {

            if (transactionReference == null ||
                    transactionReference.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Join<Prediction, Transaction> transaction =
                    root.join("transaction", JoinType.INNER);

            return criteriaBuilder.like(
                    criteriaBuilder.lower(
                            transaction.get("transactionReference")
                    ),
                    "%" + transactionReference.trim()
                            .toLowerCase(Locale.ROOT) + "%"
            );
        };
    }

    public static Specification<Prediction> hasPrediction(
            Byte prediction
    ) {
        return (root, query, criteriaBuilder) -> {

            if (prediction == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("prediction"),
                    prediction
            );
        };
    }

    public static Specification<Prediction> predictedAtFrom(
            LocalDateTime startDate
    ) {
        return (root, query, criteriaBuilder) -> {

            if (startDate == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("predictedAt"),
                    startDate
            );
        };
    }

    public static Specification<Prediction> predictedAtTo(
            LocalDateTime endDate
    ) {
        return (root, query, criteriaBuilder) -> {

            if (endDate == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("predictedAt"),
                    endDate
            );
        };
    }
}
