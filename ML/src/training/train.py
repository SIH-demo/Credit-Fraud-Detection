
from pathlib import Path
import joblib
import pandas as pd

from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.pipeline import Pipeline
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import (
    train_test_split,
    RandomizedSearchCV,
    StratifiedKFold
)

BASE_DIR = Path(__file__).resolve().parents[2]
DATA_DIR = BASE_DIR / "data" / "processed"
MODEL_DIR = BASE_DIR / "models"
MODEL_DIR.mkdir(parents=True, exist_ok=True)

# 1. Load the original training dataset
df = pd.read_csv(DATA_DIR / "train.csv")

X = df.drop(columns=["fraud"])
y = df["fraud"]

# 2. Split before tuning
X_train, X_val, y_train, y_val = train_test_split(
    X,
    y,
    test_size=0.20,
    random_state=42,
    stratify=y
)

# Save the untouched validation portion
validation_df = X_val.copy()
validation_df["fraud"] = y_val
validation_df.to_csv(DATA_DIR / "validation.csv", index=False)

print("Training samples:", len(X_train))
print("Validation samples:", len(X_val))
print("Training fraud cases:", int(y_train.sum()))
print("Validation fraud cases:", int(y_val.sum()))

# 3. Identify feature types using training data only
categorical = X_train.select_dtypes(include="str").columns.tolist()
numerical = X_train.select_dtypes(include="number").columns.tolist()

preprocessor = ColumnTransformer([
    ("categorical", OneHotEncoder(handle_unknown="ignore"), categorical),
    ("numerical", StandardScaler(), numerical)
])

# 4. Build pipeline
pipeline = Pipeline([
    ("preprocessor", preprocessor),
    ("classifier", RandomForestClassifier(
        random_state=42,
        n_jobs=1
    ))
])

# 5. Hyperparameter search
param_distributions = {
    "classifier__n_estimators": [100, 150, 200],
    "classifier__max_depth": [20, 30, None],
    "classifier__min_samples_leaf": [1, 2, 4],
    "classifier__class_weight": [
        "balanced",
        "balanced_subsample"
    ]
}

cv = StratifiedKFold(
    n_splits=2,
    shuffle=True,
    random_state=42
)

search = RandomizedSearchCV(
    estimator=pipeline,
    param_distributions=param_distributions,
    n_iter=6,
    scoring="average_precision",
    cv=cv,
    random_state=42,
    n_jobs=2,
    verbose=2,
    refit=True
)

print("\nStarting leakage-free Random Forest tuning...")

# Important: only X_train is passed to the search
search.fit(X_train, y_train)

# 6. Save the best model
best_model = search.best_estimator_

MODEL_PATH = MODEL_DIR / "fraud_detection_tuned_model.pkl"
joblib.dump(best_model, MODEL_PATH)

print("\n===== RANDOM FOREST TUNING RESULTS =====")
print("Best CV PR-AUC:", round(search.best_score_, 4))
print("Best parameters:")
print(search.best_params_)

print("\nTuned model saved successfully.")
print("Path:", MODEL_PATH)
