from Analytics.app.schemas.analytics import (
    BatchAnalyticsRequest,
    DashboardRequest,
)
from Analytics.app.services.dashboard_service import (
    build_dashboard_summary,
)


def analyze_batch(request: BatchAnalyticsRequest) -> dict:
    """Calculate batch-level and row-level analytics."""

    dashboard_request = DashboardRequest(
        transactions=request.transactions
    )

    summary = build_dashboard_summary(dashboard_request)

    rows = []

    for txn in request.transactions:
        rows.append({
            "transaction_id": txn.transaction_id,
            "amount": txn.amount,
            "currency": txn.currency,
            "transaction_time": txn.transaction_time.isoformat(),
            "risk_score": txn.risk_score,
            "risk_level": txn.risk_level or "UNKNOWN",
            "predicted_fraud": txn.predicted_fraud,
            "actual_fraud": txn.actual_fraud,
            "model_version": txn.model_version,
        })

    return {
        "batch_id": request.batch_id,
        "total_rows": len(rows),
        "processed_rows": len(rows),
        "error_count": 0,
        "summary": summary,
        "transactions": rows,
    }
