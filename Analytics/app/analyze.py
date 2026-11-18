
from pathlib import Path

import pandas as pd
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
)

BASE_DIR = Path(__file__).resolve().parents[1]
REPORTS_DIR = BASE_DIR / "reports"
REPORTS_DIR.mkdir(exist_ok=True)

# Small synthetic dataset for testing the analytics pipeline.
# Class 1 = fraud; Class 0 = legitimate.
data = {
    "transaction_id": [1001, 1002, 1003, 1004, 1005, 1006],
    "amount": [250.0, 1200.0, 80.0, 9500.0, 400.0, 7200.0],
    "actual_fraud": [0, 0, 0, 1, 0, 1],
    "predicted_fraud": [0, 0, 1, 1, 0, 1],
}

df = pd.DataFrame(data)

total_transactions = len(df)
fraud_count = int(df["actual_fraud"].sum())
legitimate_count = total_transactions - fraud_count
fraud_rate = fraud_count / total_transactions * 100

print("=== Transaction Analytics ===")
print(f"Total transactions: {total_transactions}")
print(f"Legitimate transactions: {legitimate_count}")
print(f"Fraudulent transactions: {fraud_count}")
print(f"Fraud rate: {fraud_rate:.2f}%")
print(f"Average transaction amount: {df['amount'].mean():.2f}")

y_true = df["actual_fraud"]
y_pred = df["predicted_fraud"]

metrics = {
    "accuracy": accuracy_score(y_true, y_pred),
    "precision": precision_score(y_true, y_pred, zero_division=0),
    "recall": recall_score(y_true, y_pred, zero_division=0),
    "f1_score": f1_score(y_true, y_pred, zero_division=0),
}

print("\n=== Example Model Evaluation ===")
for metric, value in metrics.items():
    print(f"{metric}: {value:.4f}")

# Save a transaction summary for later integration.
summary = pd.DataFrame(
    [
        {"metric": "total_transactions", "value": total_transactions},
        {"metric": "legitimate_transactions", "value": legitimate_count},
        {"metric": "fraudulent_transactions", "value": fraud_count},
        {"metric": "fraud_rate_percent", "value": round(fraud_rate, 2)},
        {"metric": "average_amount", "value": round(df["amount"].mean(), 2)},
    ]
)

output_file = REPORTS_DIR / "transaction_summary.csv"
summary.to_csv(output_file, index=False)

print(f"\nSummary saved to: {output_file}")
