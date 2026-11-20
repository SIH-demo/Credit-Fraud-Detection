
from pathlib import Path
import pandas as pd

BASE_DIR = Path(__file__).resolve().parents[1]
DATASET_PATH = BASE_DIR / "data" / "raw" / "banksim.csv"

df = pd.read_csv(DATASET_PATH)

for col in df.select_dtypes(include="object").columns:
    df[col] = df[col].str.strip().str.strip("'\"")

print("Shape:", df.shape)
print(df.columns.tolist())

print(df.dtypes)

print(df.isnull().sum())

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
