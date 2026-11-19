from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from typing import List
import pandas as pd
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
)

app = FastAPI(
    title="CreditGuard Analytics Service",
    version="1.0.0",
    description="Transaction analytics and model evaluation service",
)


class TransactionRecord(BaseModel):
    transaction_id: str
    amount: float = Field(ge=0)
    actual_fraud: int = Field(ge=0, le=1)
    predicted_fraud: int = Field(ge=0, le=1)


class AnalyticsRequest(BaseModel):
    transactions: List[TransactionRecord]


@app.get("/")
def root():
    return {
        "message": "CreditGuard Analytics Service is running"
    }


@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "creditguard-analytics",
    }


@app.post("/internal/v1/analyze")
def analyze(request: AnalyticsRequest):
    if not request.transactions:
        raise HTTPException(
            status_code=400,
            detail="At least one transaction is required",
        )

    df = pd.DataFrame(
        [item.model_dump() for item in request.transactions]
    )

    y_true = df["actual_fraud"]
    y_pred = df["predicted_fraud"]

    total = len(df)
    fraud_count = int(y_true.sum())
    legitimate_count = total - fraud_count

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

    return {
        "status": "success",
        "summary": {
            "total_transactions": total,
            "legitimate_transactions": legitimate_count,
            "fraudulent_transactions": fraud_count,
            "fraud_rate_percent": round(
                (fraud_count / total) * 100, 2
            ),
            "average_transaction_amount": round(
                float(df["amount"].mean()), 2
            ),
        },
        "model_metrics": metrics,
    }
