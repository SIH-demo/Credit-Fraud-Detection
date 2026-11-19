
from typing import Dict, Any, Optional
from uuid import uuid4

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field


app = FastAPI(
    title="CreditGuard ML Service",
    version="1.0.0",
    description="Internal machine learning service for fraud detection",
)


class PredictionRequest(BaseModel):
    transaction_id: str
    features: Dict[str, Any]
    correlation_id: Optional[str] = None


@app.get("/")
def root():
    return {
        "message": "CreditGuard ML Service is running"
    }


@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "fraud-detection-ml"
    }


@app.post("/internal/v1/predict")
def predict(request: PredictionRequest):

    raise HTTPException(
        status_code=503,
        detail={
            "error": "MODEL_NOT_AVAILABLE",
            "message": "No trained fraud detection model is configured yet.",
            "transaction_id": request.transaction_id,
            "correlation_id": request.correlation_id or str(uuid4()),
        },
    )
