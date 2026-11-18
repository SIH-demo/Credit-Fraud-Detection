
from fastapi import FastAPI

app = FastAPI(
    title="Credit Card Fraud Detection ML Service",
    version="0.1.0",
    description="ML service for fraud detection"
)


@app.get("/health")
def health():
    return {
        "status": "UP",
        "service": "fraud-detection-ml"
    }


@app.get("/")
def root():
    return {
        "message": "Credit Fraud Detection ML Service is running"
    }
