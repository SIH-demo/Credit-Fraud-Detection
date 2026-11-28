
package com.cfd.backend.repository;

import com.cfd.backend.entity.ModelEvaluationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelEvaluationRecordRepository
        extends JpaRepository<ModelEvaluationRecord, Long> {

    List<ModelEvaluationRecord> findByEvaluationId(Long evaluationId);
}
