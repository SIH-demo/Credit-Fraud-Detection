package com.cfd.backend.repository;

import com.cfd.backend.entity.PredictionExplanation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;

import java.util.List;

public interface PredictionExplanationRepository
        extends JpaRepository<PredictionExplanation, Long> {

    List<PredictionExplanation> findByPredictionIdOrderByFeatureRankAsc(Long predictionId);
    List<PredictionExplanation> findByPredictionIdInOrderByPredictionIdAscFeatureRankAsc(Collection<Long> predictionIds);
}
