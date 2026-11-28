package com.cfd.backend.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelEvaluationResponse {

    @JsonProperty("evaluation_count")
    private long evaluationCount;

    @JsonProperty("model_metadata")
    private ModelMetadata modelMetadata;

    @JsonProperty("classification_metrics")
    private ClassificationMetrics classificationMetrics;

    @JsonProperty("score_metrics")
    private ScoreMetrics scoreMetrics;

    @JsonProperty("score_status")
    private String scoreStatus;

    @JsonProperty("confusion_matrix")
    private ConfusionMatrix confusionMatrix;

    public ModelEvaluationResponse() {}

    public long getEvaluationCount() {
        return evaluationCount;
    }

    public void setEvaluationCount(long evaluationCount) {
        this.evaluationCount = evaluationCount;
    }

    public ModelMetadata getModelMetadata() {
        return modelMetadata;
    }

    public void setModelMetadata(ModelMetadata modelMetadata) {
        this.modelMetadata = modelMetadata;
    }

    public ClassificationMetrics getClassificationMetrics() {
        return classificationMetrics;
    }

    public void setClassificationMetrics(
            ClassificationMetrics classificationMetrics
    ) {
        this.classificationMetrics = classificationMetrics;
    }

    public ScoreMetrics getScoreMetrics() {
        return scoreMetrics;
    }

    public void setScoreMetrics(ScoreMetrics scoreMetrics) {
        this.scoreMetrics = scoreMetrics;
    }

    public String getScoreStatus() {
        return scoreStatus;
    }

    public void setScoreStatus(String scoreStatus) {
        this.scoreStatus = scoreStatus;
    }

    public ConfusionMatrix getConfusionMatrix() {
        return confusionMatrix;
    }

    public void setConfusionMatrix(ConfusionMatrix confusionMatrix) {
        this.confusionMatrix = confusionMatrix;
    }
}
