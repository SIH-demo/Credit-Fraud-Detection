
from fastapi import APIRouter
from fastapi.responses import StreamingResponse

from Analytics.app.schemas.analytics import BatchAnalyticsRequest
from Analytics.app.services.batch_service import analyze_batch
from Analytics.app.services.report_service import generate_batch_csv


router = APIRouter(
    prefix="/internal/v1/batches",
    tags=["Batch Analytics"],
)


@router.post("/analyze")
def batch_analysis(request: BatchAnalyticsRequest):
    return {
        "status": "success",
        "data": analyze_batch(request),
    }


@router.post("/report")
def batch_report(request: BatchAnalyticsRequest):
    report = generate_batch_csv(request)

    filename = f"{request.batch_id}_report.csv"

    return StreamingResponse(
        iter([report.getvalue()]),
        media_type="text/csv",
        headers={
            "Content-Disposition": f'attachment; filename="{filename}"'
        },
    )
