
package com.cfd.backend.repository;

import com.cfd.backend.entity.Prediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public interface PredictionRepository extends JpaRepository<Prediction, Long> ,JpaSpecificationExecutor<Prediction>{

    List<Prediction> findByTransactionIdOrderByPredictedAtDesc(Long transactionId);

    List<Prediction> findByCorrelationId(String correlationId);

    @EntityGraph(attributePaths = {"transaction"})
    Page<Prediction> findAllByOrderByPredictedAtDesc(Pageable pageable);
    @Override
    @EntityGraph(attributePaths = {"transaction"})
    Page<Prediction> findAll(
        Specification<Prediction> specification,
        Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Prediction p
    WHERE p.transaction.id IN :transactionIds
      AND p.id = (
          SELECT MAX(p2.id)
          FROM Prediction p2
          WHERE p2.transaction.id = p.transaction.id
            AND p2.predictedAt = (
                SELECT MAX(p3.predictedAt)
                FROM Prediction p3
                WHERE p3.transaction.id = p.transaction.id
            )
      )
    """)
List<Prediction> findLatestPredictionsByTransactionIds(
        @Param("transactionIds") List<Long> transactionIds
    );
}
