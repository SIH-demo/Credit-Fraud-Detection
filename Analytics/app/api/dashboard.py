
from fastapi import APIRouter

from Analytics.app.schemas.analytics import DashboardRequest
from Analytics.app.services.dashboard_service import (
    build_dashboard_summary,
)


router = APIRouter(
    prefix="/internal/v1/dashboard",
    tags=["Dashboard Analytics"],
)


@router.post("/summary")
def dashboard_summary(request: DashboardRequest):
    return {
        "status": "success",
        "data": build_dashboard_summary(request),
    }
