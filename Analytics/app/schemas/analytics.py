from datetime import datetime,timezone
from typing import Literal

from pydantic import (
    BaseModel,
    ConfigDict,
    Field,
    FiniteFloat,
    StrictInt,
    model_validator,
)


RiskLevel = Literal["LOW", "MEDIUM", "HIGH"]


class DashboardTransaction(BaseModel):
    """Transaction data used for dashboard calculations."""

    model_config = ConfigDict(str_strip_whitespace=True)

    transaction_id: str = Field(min_length=1)
    amount: FiniteFloat = Field(ge=0)
    currency: str = Field(
    default="INR",
    min_length=3,
    max_length=3,
    pattern=r"^[A-Z]{3}$",
    )

    transaction_time: datetime

    risk_score: FiniteFloat | None = Field(
        default=None,
        ge=0,
        le=1,
    )
    risk_level: RiskLevel | None = None
    predicted_fraud: StrictInt | None = Field(
        default=None,
        ge=0,
        le=1,
    )
    actual_fraud: StrictInt | None = Field(
        default=None,
        ge=0,
        le=1,
    )
    model_version: str | None = None



class DashboardRequest(BaseModel):
    """Transaction records and optional dashboard date filters."""

    transactions: list[DashboardTransaction] = Field(
        default_factory=list
    )
    start_time: datetime | None = None
    end_time: datetime | None = None

    @model_validator(mode="after")
    def validate_time_range(self):
        timestamps = [
            txn.transaction_time
            for txn in self.transactions
        ]

        if self.start_time is not None:
            timestamps.append(self.start_time)

        if self.end_time is not None:
            timestamps.append(self.end_time)

        awareness = {
            timestamp.utcoffset() is not None
            for timestamp in timestamps
        }

        if len(awareness) > 1:
            raise ValueError(
                "All transaction timestamps and date filters "
                "must consistently use timezone-aware or "
                "timezone-naive datetimes"
            )

        if (
            self.start_time is not None
            and self.end_time is not None
            and self.start_time > self.end_time
        ):
            raise ValueError(
                "start_time must be before or equal to end_time"
            )

        return self



class EvaluationTransaction(BaseModel):
    """Labeled transaction data used for model evaluation."""

    model_config = ConfigDict(str_strip_whitespace=True)

    transaction_id: str = Field(min_length=1)
    actual_fraud: StrictInt = Field(ge=0, le=1)
    predicted_fraud: StrictInt = Field(ge=0, le=1)
    risk_score: FiniteFloat | None = Field(
        default=None,
        ge=0,
        le=1,
    )


class ModelEvaluationRequest(BaseModel):
    """Model evaluation data and associated model metadata."""

    model_config = ConfigDict(str_strip_whitespace=True)

    transactions: list[EvaluationTransaction] = Field(
        min_length=1,
        max_length=100000,
    )

    model_name: str | None = Field(default=None, min_length=1)
    model_version: str | None = Field(default=None, min_length=1)

    threshold: FiniteFloat | None = Field(
        default=None,
        ge=0,
        le=1,
    )

    evaluated_at: datetime = Field(
        default_factory=lambda: datetime.now(timezone.utc)
    )



class BatchAnalyticsRequest(BaseModel):
    """Parsed transaction records received from Backend."""

    model_config = ConfigDict(str_strip_whitespace=True)

    batch_id: str = Field(
        min_length=1,
        max_length=100,
        pattern=r"^[A-Za-z0-9_-]+$",
    )

    transactions: list[DashboardTransaction] = Field(
        min_length=1,
        max_length=10000,
    )

