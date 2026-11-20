
from pathlib import Path
import pandas as pd

# Dataset path
BASE_DIR = Path(__file__).resolve().parents[1]
DATASET_PATH = BASE_DIR / "data" / "raw" / "banksim.csv"

# Load dataset
df = pd.read_csv(DATASET_PATH)

# Clean unnecessary quotes from text columns
for col in df.select_dtypes(include="object").columns:
    df[col] = df[col].str.strip().str.strip("'\"")

print("\n===== DATASET OVERVIEW =====")
print("Shape:", df.shape)
print("\nColumns:")
print(df.columns.tolist())

print("\n===== DATA TYPES =====")
print(df.dtypes)

print("\n===== MISSING VALUES =====")
print(df.isnull().sum())

print("\n===== DUPLICATE ROWS =====")
print(df.duplicated().sum())

print("\n===== FRAUD DISTRIBUTION =====")
print(df["fraud"].value_counts())
print("\nFraud percentage:")
print((df["fraud"].value_counts(normalize=True) * 100).round(2))

print("\n===== TRANSACTION AMOUNT =====")
print(df.groupby("fraud")["amount"].describe())

print("\n===== TRANSACTION CATEGORIES =====")
print(df["category"].nunique(), "unique categories")
print(df["category"].value_counts().head(10))
