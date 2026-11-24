from sklearn.metrics import (
    accuracy_score,
    average_precision_score,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score,
    roc_auc_score,
)

from Analytics.app.schemas.analytics import ModelEvaluationRequest


def evaluate_model(request: ModelEvaluationRequest) -> dict:
    transactions = request.transactions

    y_true = [txn.actual_fraud for txn in transactions]
    y_pred = [txn.predicted_fraud for txn in transactions]

    metrics = {
        "accuracy": float(accuracy_score(y_true, y_pred)),
        "precision": float(
            precision_score(y_true, y_pred, zero_division=0)
        ),
        "recall": float(
            recall_score(y_true, y_pred, zero_division=0)
        ),
        "f1_score": float(
            f1_score(y_true, y_pred, zero_division=0)
        ),
    }

    tn, fp, fn, tp = confusion_matrix(
        y_true,
        y_pred,
        labels=[0, 1],
    ).ravel()

    scores = [txn.risk_score for txn in transactions]
    all_scores_available = all(
        score is not None for score in scores
    )

    score_metrics = {
        "roc_auc": None,
        "average_precision": None,
    }

    score_status = "not_provided"

    if all_scores_available:
        score_status = "available"
        actual_classes = set(y_true)

        if len(actual_classes) == 2:
            score_metrics["roc_auc"] = float(
                roc_auc_score(y_true, scores)
            )

        if 1 in actual_classes:
            score_metrics["average_precision"] = float(
                average_precision_score(y_true, scores)
            )

    elif any(score is not None for score in scores):
        score_status = "incomplete"

    return {
        "evaluation_count": len(transactions),

        "model_metadata": {
            "model_name": request.model_name,
            "model_version": request.model_version,
            "threshold": request.threshold,
            "evaluated_at": request.evaluated_at.isoformat(),
        },

        "classification_metrics": metrics,

        "score_metrics": score_metrics,
        "score_status": score_status,

        "confusion_matrix": {
            "true_positive": int(tp),
            "true_negative": int(tn),
            "false_positive": int(fp),
            "false_negative": int(fn),
        },
    }
