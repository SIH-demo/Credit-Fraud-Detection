from fastapi import APIRouter

from Analytics.app.schemas.analytics import ModelEvaluationRequest
from Analytics.app.services.model_service import evaluate_model


router = APIRouter(
    prefix="/internal/v1/model",
    tags=["Model Evaluation"],
)


@router.post("/evaluate")
def model_evaluation(request: ModelEvaluationRequest):
    return {
        "status": "success",
        "data": evaluate_model(request),
    }
