
import csv
import io

from Analytics.app.schemas.analytics import BatchAnalyticsRequest
from Analytics.app.services.batch_service import analyze_batch


def generate_batch_csv(request: BatchAnalyticsRequest) -> io.StringIO:
    """Generate a CSV report for the supplied batch."""

    result = analyze_batch(request)

    output = io.StringIO()

    columns = [
        "transaction_id",
        "amount",
        "currency",
        "transaction_time",
        "risk_score",
        "risk_level",
        "predicted_fraud",
        "actual_fraud",
        "model_version",
    ]

    writer = csv.DictWriter(
        output,
        fieldnames=columns,
    )

    writer.writeheader()

    for transaction in result["transactions"]:
        safe_transaction = {}

        for column in columns:
            value = transaction.get(column)

            # Prevent spreadsheet formula injection in text fields.
            if isinstance(value, str) and value.startswith(
                ("=", "+", "-", "@", "\t", "\r")
            ):
                value = "'" + value

            safe_transaction[column] = value

        writer.writerow(safe_transaction)

    output.seek(0)
    return output
