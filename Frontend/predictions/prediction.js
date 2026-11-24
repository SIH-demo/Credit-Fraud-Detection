
"use strict";

/* =========================================
   CREDITGUARD - FRAUD PREDICTION
   File: prediction.js
   API: POST /api/v1/transactions/predict
========================================= */

const API_URL = "/api/v1/transactions/predict";

const elements = {
    form: document.getElementById("predictionForm"),
    message: document.getElementById("pageMessage"),
    predictButton: document.getElementById("predictButton"),
    resetButton: document.getElementById("resetButton"),

    resultSection: document.getElementById("predictionResultSection"),

    riskLevel: document.getElementById("riskLevel"),
    riskScore: document.getElementById("riskScore"),
    riskProgressBar: document.getElementById("riskProgressBar"),
    predictionLabel: document.getElementById("predictionLabel"),

    modelVersion: document.getElementById("resultModelVersion"),
    threshold: document.getElementById("resultThreshold"),
    predictionDate: document.getElementById("predictionDate"),
    transactionId: document.getElementById("resultTransactionId"),
    explanation: document.getElementById("predictionExplanation")
};


/* =========================================
   HELPER FUNCTIONS
========================================= */

function showMessage(message, type = "info") {
    if (!elements.message) return;

    elements.message.textContent = message;
    elements.message.className = `page-message ${type}`;
}

function setText(element, value, fallback = "--") {
    if (!element) return;

    element.textContent =
        value === null || value === undefined || value === ""
            ? fallback
            : String(value);
}

function getValue(object, paths, fallback = null) {
    for (const path of paths) {
        const value = path.split(".").reduce((current, key) => {
            return current == null ? undefined : current[key];
        }, object);

        if (value !== undefined && value !== null && value !== "") {
            return value;
        }
    }

    return fallback;
}

function formatDate(value) {
    if (!value) return new Date().toLocaleString("en-IN");

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    return date.toLocaleString("en-IN");
}

function formatThreshold(value) {
    if (value === null || value === undefined || value === "") {
        return "--";
    }

    const number = Number(value);

    return Number.isFinite(number)
        ? number.toFixed(3)
        : String(value);
}


/* =========================================
   FORM DATA
========================================= */

function getTransactionPayload() {
    const formData = new FormData(elements.form);

    return {
        externalReference:
            formData.get("externalReference")?.trim() || null,

        amount: Number(formData.get("amount")),

        currency: formData.get("currency"),

        transactionTime: formData.get("transactionTime"),

        merchant: formData.get("merchant")?.trim(),

        country: formData.get("country")?.trim(),

        maskedCardLast4:
            formData.get("maskedCardLast4")?.trim() || null,

        status: formData.get("transactionStatus")
    };
}


/* =========================================
   CLIENT-SIDE VALIDATION
========================================= */

function validateTransaction(payload) {
    if (!Number.isFinite(payload.amount) || payload.amount <= 0) {
        return "Please enter a valid transaction amount.";
    }

    if (!payload.currency) {
        return "Please select a currency.";
    }

    if (!payload.transactionTime) {
        return "Please enter transaction date and time.";
    }

    if (!payload.merchant) {
        return "Please enter merchant name.";
    }

    if (!payload.country) {
        return "Please enter country.";
    }

    if (
        payload.maskedCardLast4 &&
        !/^[0-9]{4}$/.test(payload.maskedCardLast4)
    ) {
        return "Card last four digits must contain exactly 4 numbers.";
    }

    return null;
}


/* =========================================
   RISK SCORE HANDLING
========================================= */

function normalizeRiskScore(value) {
    if (value === null || value === undefined || value === "") {
        return null;
    }

    const score = Number(value);

    if (!Number.isFinite(score)) {
        return null;
    }

    // SRS risk score range is 0 to 1.
    // Values above 1 are treated as percentage values
    // only for display compatibility.
    const normalized = score > 1 ? score / 100 : score;

    return Math.min(1, Math.max(0, normalized));
}

function displayRiskScore(scoreValue) {
    const score = normalizeRiskScore(scoreValue);

    if (score === null) {
        setText(elements.riskScore, "--");
        if (elements.riskProgressBar) {
            elements.riskProgressBar.style.width = "0%";
        }
        return;
    }

    const percentage = score * 100;

    setText(elements.riskScore, `${percentage.toFixed(2)}%`);

    if (elements.riskProgressBar) {
        elements.riskProgressBar.style.width = `${percentage}%`;
    }
}


/* =========================================
   DISPLAY PREDICTION RESULT
========================================= */

function displayPredictionResult(response) {
    const prediction =
        response.prediction ??
        response.result ??
        response.data ??
        response;

    const riskScore = getValue(prediction, [
        "riskScore",
        "risk_score",
        "score"
    ]);

    const riskLevel = getValue(prediction, [
        "riskLevel",
        "risk_level",
        "risk"
    ], "Not available");

    const predictionValue = getValue(prediction, [
        "prediction",
        "predictionLabel",
        "predictedClass",
        "classification",
        "isFraud",
        "is_fraud"
    ], "Not available");

    const modelVersion = getValue(prediction, [
        "modelVersion",
        "model_version",
        "model.version"
    ]);

    const threshold = getValue(prediction, [
        "thresholdUsed",
        "threshold_used",
        "threshold"
    ]);

    const predictionTime = getValue(prediction, [
        "predictedAt",
        "predicted_at",
        "predictionDate",
        "timestamp"
    ]);

    const transactionId = getValue(prediction, [
        "transactionId",
        "transaction_id",
        "transaction.id"
    ]);

    const explanation = getValue(prediction, [
        "explanation",
        "reason",
        "message",
        "explanationText"
    ]);

    setText(elements.riskLevel, riskLevel);
    displayRiskScore(riskScore);

    setText(elements.predictionLabel, predictionValue);
    setText(elements.modelVersion, modelVersion);
    setText(elements.threshold, formatThreshold(threshold));
    setText(elements.predictionDate, formatDate(predictionTime));
    setText(elements.transactionId, transactionId);
    setText(
        elements.explanation,
        explanation,
        "No prediction explanation was provided by the server."
    );

    if (elements.resultSection) {
        elements.resultSection.hidden = false;
    }
}


/* =========================================
   SUBMIT PREDICTION
========================================= */

async function submitPrediction(event) {
    event.preventDefault();

    if (!elements.form) return;

    const payload = getTransactionPayload();

    const validationError = validateTransaction(payload);

    if (validationError) {
        showMessage(validationError, "error");
        return;
    }

    try {
        elements.predictButton.disabled = true;
        elements.predictButton.textContent = "Analyzing...";

        showMessage(
            "Analyzing transaction. Please wait...",
            "info"
        );

        if (elements.resultSection) {
            elements.resultSection.hidden = true;
        }

        const response = await fetch(API_URL, {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(payload)
        });

        const responseText = await response.text();

        let result = {};

        if (responseText) {
            try {
                result = JSON.parse(responseText);
            } catch {
                throw new Error("Server returned an invalid response.");
            }
        }

        if (!response.ok) {
            throw new Error(
                result.message ||
                result.error ||
                `Request failed with status ${response.status}`
            );
        }

        displayPredictionResult(result);

        showMessage(
            "Transaction analysis completed successfully.",
            "success"
        );

        elements.resultSection?.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });

    } catch (error) {
        console.error("Prediction Error:", error);

        showMessage(
            error.message || "Unable to analyze transaction.",
            "error"
        );

    } finally {
        elements.predictButton.disabled = false;
        elements.predictButton.textContent = "Analyze Transaction";
    }
}


/* =========================================
   RESET FORM
========================================= */

function resetPredictionForm() {
    if (elements.resultSection) {
        elements.resultSection.hidden = true;
    }

    showMessage("", "info");

    if (elements.riskProgressBar) {
        elements.riskProgressBar.style.width = "0%";
    }
}


/* =========================================
   EVENT LISTENERS
========================================= */

if (elements.form) {
    elements.form.addEventListener(
        "submit",
        submitPrediction
    );

    elements.form.addEventListener("reset", () => {
        setTimeout(resetPredictionForm, 0);
    });
}

if (elements.resetButton) {
    elements.resetButton.addEventListener(
        "click",
        resetPredictionForm
    );
}


/* =========================================
   INITIALIZE
========================================= */

document.addEventListener("DOMContentLoaded", () => {
    if (!elements.form) {
        console.error("Prediction form not found.");
        return;
    }

    if (elements.resultSection) {
        elements.resultSection.hidden = true;
    }
});
