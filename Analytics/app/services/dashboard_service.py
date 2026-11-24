
from collections import defaultdict

from Analytics.app.schemas.analytics import DashboardRequest


def build_dashboard_summary(request: DashboardRequest) -> dict:
    """Calculate currency-aware dashboard KPIs and trends."""

    transactions = request.transactions

    if request.start_time is not None:
        transactions = [
            txn for txn in transactions
            if txn.transaction_time >= request.start_time
        ]

    if request.end_time is not None:
        transactions = [
            txn for txn in transactions
            if txn.transaction_time <= request.end_time
        ]

    total = len(transactions)

    predicted_fraud_count = sum(
        txn.predicted_fraud == 1
        for txn in transactions
        if txn.predicted_fraud is not None
    )

    currency_totals = defaultdict(float)
    currency_counts = defaultdict(int)
    daily_data = defaultdict(
        lambda: {
            "transaction_count": 0,
            "total_amount": 0.0,
            "predicted_fraud_count": 0,
        }
    )

    risk_distribution = {
        "LOW": 0,
        "MEDIUM": 0,
        "HIGH": 0,
        "UNKNOWN": 0,
    }

    for txn in transactions:
        currency = txn.currency

        currency_totals[currency] += txn.amount
        currency_counts[currency] += 1

        risk = txn.risk_level or "UNKNOWN"
        risk_distribution[risk] += 1

        day = txn.transaction_time.date().isoformat()

        daily_data[(day, currency)]["transaction_count"] += 1
        daily_data[(day, currency)]["total_amount"] += txn.amount

        if txn.predicted_fraud == 1:
            daily_data[(day, currency)]["predicted_fraud_count"] += 1

    currency_summary = {
        currency: {
            "transaction_count": currency_counts[currency],
            "total_amount": round(currency_totals[currency], 2),
            "average_transaction_amount": round(
                currency_totals[currency] / currency_counts[currency], 2
            ),
        }
        for currency in sorted(currency_totals)
    }

    daily_trend = [
        {
            "date": day,
            "currency": currency,
            "transaction_count": values["transaction_count"],
            "total_amount": round(values["total_amount"], 2),
            "predicted_fraud_count": values["predicted_fraud_count"],
        }
        for (day, currency), values in sorted(daily_data.items())
    ]

    suspicious_transactions = [
        txn for txn in transactions
        if txn.risk_level == "HIGH" or txn.predicted_fraud == 1
    ]

    suspicious_transactions.sort(
        key=lambda txn: (
            txn.risk_score if txn.risk_score is not None else -1,
            txn.transaction_time,
        ),
        reverse=True,
    )

    recent_suspicious = [
        {
            "transaction_id": txn.transaction_id,
            "amount": round(txn.amount, 2),
            "currency": txn.currency,
            "transaction_time": txn.transaction_time.isoformat(),
            "risk_score": txn.risk_score,
            "risk_level": txn.risk_level or "UNKNOWN",
            "predicted_fraud": txn.predicted_fraud,
            "model_version": txn.model_version,
        }
        for txn in suspicious_transactions[:10]
    ]

    # Preserve the old scalar fields when only one currency is present.
    single_currency = len(currency_summary) <= 1
    only_currency = next(iter(currency_summary), None)

    return {
        "total_transactions": total,
        "total_transaction_amount": (
            currency_summary[only_currency]["total_amount"]
            if single_currency and only_currency
            else None
        ),
        "average_transaction_amount": (
            currency_summary[only_currency]["average_transaction_amount"]
            if single_currency and only_currency
            else None
        ),
        "currency_summary": currency_summary,
        "predicted_fraud_transactions": predicted_fraud_count,
        "risk_distribution": risk_distribution,
        "daily_trend": daily_trend,
        "recent_suspicious_transactions": recent_suspicious,
    }
