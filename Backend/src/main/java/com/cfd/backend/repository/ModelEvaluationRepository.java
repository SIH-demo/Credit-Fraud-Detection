
package com.cfd.backend.repository;

import com.cfd.backend.entity.ModelEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelEvaluationRepository
        extends JpaRepository<ModelEvaluation, Long> {

    List<ModelEvaluation> findByModelVersionOrderByEvaluatedAtDesc(String modelVersion);
}
