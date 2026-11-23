
from pathlib import Path
from typing import Any, Optional
from uuid import uuid4

import joblib
import numpy as np
import pandas as pd
import shap

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, ConfigDict, Field

BASE_DIR = Path(__file__).resolve().parents[1]

MODEL_PATH = BASE_DIR / "models" / "fraud_detection_tuned_model.pkl"

MODEL_VERSION = "2.0.0"
PREDICTION_THRESHOLD = 0.70

FEATURE_COLUMNS = [
    "step",
    "age",
    "gender",
    "merchant",
    "category",
    "amount",
]

_model = None
_explainer = None

app = FastAPI(
    title="CreditGuard ML Service",
    version=MODEL_VERSION,
    description="Internal machine learning service for fraud detection",
)


class PredictionRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    transaction_id: str = Field(min_length=1)
    features: dict[str, Any]
    correlation_id: Optional[str] = None


class PredictionResponse(BaseModel):
    transaction_id: str
    prediction: int
    risk_score: float
    risk_level: str
    model_version: str
    correlation_id: str
    explanation: dict[str, Any]


def get_model():
    global _model

    if _model is None:
        if not MODEL_PATH.exists():
            raise HTTPException(
                status_code=503,
                detail={
                    "error": "MODEL_NOT_AVAILABLE",
                    "message": "Trained model file not found.",
                },
            )

        try:
            _model = joblib.load(MODEL_PATH)

        except Exception:
            raise HTTPException(
                status_code=503,
                detail={
                    "error": "MODEL_LOAD_FAILED",
                    "message": "Unable to load trained model.",
                },
            )

    return _model


def get_explainer():
    global _explainer

    if _explainer is None:
        model = get_model()
        classifier = model.named_steps["classifier"]
        _explainer = shap.TreeExplainer(classifier)

    return _explainer


def prepare_features(features: dict):
    missing = set(FEATURE_COLUMNS) - set(features)
    extra = set(features) - set(FEATURE_COLUMNS)

    if missing or extra:
        raise HTTPException(
            status_code=422,
            detail={
                "error": "INVALID_FEATURES",
                "missing_features": sorted(missing),
                "unexpected_features": sorted(extra),
            },
        )

    try:
        row = {}

        for column in FEATURE_COLUMNS:
            value = features[column]

            if column in ["step", "amount"]:
                if isinstance(value, bool):
                    raise ValueError()

                row[column] = float(value)

                if not pd.notna(row[column]):
                    raise ValueError()

            else:
                if not isinstance(value, str):
                    raise ValueError()

                row[column] = value.strip().strip("'\"")

        return pd.DataFrame([row], columns=FEATURE_COLUMNS)

    except (ValueError, TypeError):
        raise HTTPException(
            status_code=422,
            detail={
                "error": "INVALID_FEATURE_VALUE",
                "message": "Transaction features have invalid values or types.",
            },
        )


def explain_prediction(model, transaction):
    preprocessor = model.named_steps["preprocessor"]

    transformed = preprocessor.transform(transaction)

    if hasattr(transformed, "toarray"):
        transformed = transformed.toarray()

    transformed = np.asarray(transformed, dtype=np.float64)

    explainer = get_explainer()
    shap_values = explainer.shap_values(transformed)

    # Binary classification: class 1 represents fraud.
    contributions = shap_values[0, :, 1]

    feature_names = preprocessor.get_feature_names_out()

    original_features = {
        feature: 0.0 for feature in FEATURE_COLUMNS
    }

    for name, contribution in zip(feature_names, contributions):
        original_name = name.split("__", 1)[-1]

        # Aggregate one-hot encoded contributions.
        if original_name.startswith("age_"):
            original_name = "age"

        elif original_name.startswith("gender_"):
            original_name = "gender"

        elif original_name.startswith("merchant_"):
            original_name = "merchant"

        elif original_name.startswith("category_"):
            original_name = "category"

        original_features[original_name] += float(contribution)

    top_features = sorted(
        original_features.items(),
        key=lambda item: abs(item[1]),
        reverse=True,
    )[:5]

    return [
        {
            "feature": feature,
            "contribution": round(contribution, 6),
            "impact": (
                "increases_fraud_risk"
                if contribution > 0
                else (
                    "decreases_fraud_risk"
                    if contribution < 0
                    else "neutral"
                )
            ),
        }
        for feature, contribution in top_features
    ]


@app.get("/")
def root():
    return {
        "message": "CreditGuard ML Service is running",
        "version": MODEL_VERSION,
        "model": "Tuned Random Forest",
        "prediction_threshold": PREDICTION_THRESHOLD,
    }


@app.get("/health")
def health():
    try:
        get_model()

        return {
            "status": "UP",
            "service": "fraud-detection-ml",
            "model_loaded": True,
            "model_version": MODEL_VERSION,
            "prediction_threshold": PREDICTION_THRESHOLD,
        }

    except HTTPException:
        return {
            "status": "DEGRADED",
            "service": "fraud-detection-ml",
            "model_loaded": False,
            "model_version": MODEL_VERSION,
        }


@app.post(
    "/internal/v1/predict",
    response_model=PredictionResponse,
)
def predict(request: PredictionRequest):

    correlation_id = request.correlation_id or str(uuid4())

    model = get_model()
    transaction = prepare_features(request.features)

    try:
        risk_score = float(
            model.predict_proba(transaction)[0][1]
        )

        prediction = int(
            risk_score >= PREDICTION_THRESHOLD
        )

        top_features = explain_prediction(model, transaction)

    except Exception:
        raise HTTPException(
            status_code=500,
            detail={
                "error": "PREDICTION_FAILED",
                "message": "Unable to generate transaction prediction.",
                "transaction_id": request.transaction_id,
                "correlation_id": correlation_id,
            },
        )

    if risk_score < 0.30:
        risk_level = "LOW"

    elif risk_score < PREDICTION_THRESHOLD:
        risk_level = "MEDIUM"

    else:
        risk_level = "HIGH"

    return PredictionResponse(
        transaction_id=request.transaction_id,
        prediction=prediction,
        risk_score=round(risk_score, 4),
        risk_level=risk_level,
        model_version=MODEL_VERSION,
        correlation_id=correlation_id,
        explanation={
            "top_features": top_features
        },
    )
