
from pathlib import Path
import joblib
import pandas as pd

BASE_DIR = Path(__file__).resolve().parents[2]
MODEL_PATH = BASE_DIR / "models" / "fraud_detection_model.pkl"

model = joblib.load(MODEL_PATH)

# Sample transaction
transaction = pd.DataFrame([{
    "step": 100,
    "age": "2",
    "gender": "M",
    "merchant": "M348934600",
    "category": "es_transportation",
    "amount": 500.0
}])

prediction = model.predict(transaction)[0]
probability = model.predict_proba(transaction)[0][1]

print("\n===== FRAUD PREDICTION =====")
print("Prediction:", "Fraud" if prediction == 1 else "Legitimate")
print("Fraud probability:", round(probability * 100, 2), "%")
