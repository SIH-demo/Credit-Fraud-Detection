from pathlib import Path
import pandas as pd
from sklearn.model_selection import train_test_split

BASE_DIR = Path(__file__).resolve().parents[2]
RAW_PATH = BASE_DIR / "data" / "raw" / "banksim.csv"
OUTPUT_DIR = BASE_DIR / "data" / "processed"

OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

df = pd.read_csv(RAW_PATH)

for col in df.select_dtypes(include="str").columns:
    df[col] = df[col].str.strip().str.strip("'\"")

df.drop(columns=["customer", "zipcodeOri", "zipMerchant"], inplace=True)

X = df.drop(columns=["fraud"])
y = df["fraud"]

X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.20,
    random_state=42,
    stratify=y
)

X_train.assign(fraud=y_train).to_csv(OUTPUT_DIR / "train.csv", index=False)
X_test.assign(fraud=y_test).to_csv(OUTPUT_DIR / "test.csv", index=False)

print("Training rows:", len(X_train))
print("Testing rows:", len(X_test))
print("Training fraud cases:", int(y_train.sum()))
print("Testing fraud cases:", int(y_test.sum()))
