
from pathlib import Path
import joblib
import pandas as pd

from sklearn.metrics import (
    classification_report,
    confusion_matrix,
    precision_score,
    recall_score,
    f1_score,
    average_precision_score,
    roc_auc_score
)

BASE_DIR = Path(__file__).resolve().parents[2]

MODEL_PATH = BASE_DIR / "models" / "fraud_detection_tuned_model.pkl"
TEST_PATH = BASE_DIR / "data" / "processed" / "test.csv"

THRESHOLD = 0.70

# Load the selected model and final test dataset
model = joblib.load(MODEL_PATH)
df = pd.read_csv(TEST_PATH)

X_test = df.drop(columns=["fraud"])
y_test = df["fraud"]

# Generate fraud probabilities
y_prob = model.predict_proba(X_test)[:, 1]

# Apply the threshold selected using validation data
y_pred = (y_prob >= THRESHOLD).astype(int)

# Calculate metrics
print("\n===== FINAL TEST EVALUATION =====")
print("Model: Tuned Random Forest")
print("Threshold:", THRESHOLD)
print("Test samples:", len(X_test))
print("Actual fraud cases:", int(y_test.sum()))

print("\nPR-AUC:", round(
    average_precision_score(y_test, y_prob), 4
))

print("ROC-AUC:", round(
    roc_auc_score(y_test, y_prob), 4
))

print("\nClassification Report:")
print(classification_report(
    y_test,
    y_pred,
    target_names=["Legitimate", "Fraud"],
    digits=4,
    zero_division=0
))

tn, fp, fn, tp = confusion_matrix(
    y_test, y_pred, labels=[0, 1]
).ravel()

print("Confusion Matrix:")
print([[tn, fp], [fn, tp]])

print("\nFraud Detection Metrics:")
print("Precision:", round(
    precision_score(y_test, y_pred, zero_division=0), 4
))
print("Recall:", round(
    recall_score(y_test, y_pred, zero_division=0), 4
))
print("F1-score:", round(
    f1_score(y_test, y_pred, zero_division=0), 4
))

print("\nDetailed Counts:")
print("True Positives:", tp)
print("False Positives:", fp)
print("False Negatives:", fn)
print("True Negatives:", tn)
