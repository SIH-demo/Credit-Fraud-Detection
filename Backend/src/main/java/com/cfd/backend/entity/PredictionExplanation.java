
package com.cfd.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "prediction_explanations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_explanation_feature_rank",
            columnNames = {"prediction_id", "feature_rank"}
        )
    }
)
public class PredictionExplanation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prediction_id", nullable = false)
    private Prediction prediction;

    @Column(name = "feature_name", nullable = false, length = 100)
    private String featureName;

    @Column(name = "contribution", nullable = false)
    private Double contribution;

    @Column(name = "impact", nullable = false, length = 30)
    private String impact;

    @Column(name = "feature_rank", nullable = false)
    private Short featureRank;

    public PredictionExplanation() {
    }

    public Long getId() {
        return id;
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public Double getContribution() {
        return contribution;
    }

    public void setContribution(Double contribution) {
        this.contribution = contribution;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public Short getFeatureRank() {
        return featureRank;
    }

    public void setFeatureRank(Short featureRank) {
        this.featureRank = featureRank;
    }
}
