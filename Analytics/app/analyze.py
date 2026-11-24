
import pandas as pd

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from Analytics.app.api.dashboard import router as dashboard_router
from Analytics.app.api.model import router as model_router
from Analytics.app.api.batches import router as batches_router

from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    FiniteFloat,
)

from sklearn.metrics import (
    accuracy_score,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score,
)


app = FastAPI(
    title="CreditGuard Analytics Service",
    version="1.1.0",
    description="Transaction analytics and model evaluation service",
)

app.include_router(dashboard_router)
app.include_router(model_router)
app.include_router(batches_router)


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(
    request: Request,
    exc: RequestValidationError,
):
    errors = [
        {
            "type": error["type"],
            "loc": error["loc"],
            "msg": error["msg"],
        }
        for error in exc.errors()
    ]

    return JSONResponse(
        status_code=422,
        content={"detail": errors},
    )


class TransactionRecord(BaseModel):
    model_config = ConfigDict(str_strip_whitespace=True)

    transaction_id: str = Field(min_length=1)
    amount: FiniteFloat = Field(ge=0)
    actual_fraud: int = Field(ge=0, le=1)
    predicted_fraud: int = Field(ge=0, le=1)


class AnalyticsRequest(BaseModel):
    transactions: list[TransactionRecord] = Field(min_length=1)


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

    df = pd.DataFrame(
        [item.model_dump() for item in request.transactions]
    )

    y_true = df["actual_fraud"]
    y_pred = df["predicted_fraud"]
    amounts = df["amount"]

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

    tn, fp, fn, tp = confusion_matrix(
        y_true,
        y_pred,
        labels=[0, 1],
    ).ravel()

    return {
        "status": "success",
        "summary": {
            "total_transactions": total,
            "legitimate_transactions": legitimate_count,
            "fraudulent_transactions": fraud_count,
            "fraud_rate_percent": round(
                (fraud_count / total) * 100, 2
            ),
            "total_transaction_amount": round(
                float(amounts.sum()), 2
            ),
            "average_transaction_amount": round(
                float(amounts.mean()), 2
            ),
            "minimum_transaction_amount": round(
                float(amounts.min()), 2
            ),
            "maximum_transaction_amount": round(
                float(amounts.max()), 2
            ),
        },
        "model_metrics": metrics,
        "confusion_matrix": {
            "true_positive": int(tp),
            "true_negative": int(tn),
            "false_positive": int(fp),
            "false_negative": int(fn),
        },
    }
