
"use strict";

/*
 * CreditGuard - Model Analytics
 *
 * MVP:
 * - Display the deployed ML model's health and configuration.
 * - Use the backend ML health endpoint as the source of truth.
 *
 * Future extension points:
 * - Model registry
 * - Model evaluation and comparison
 * - Active model selection
 */

const elements = {
    modelName: document.getElementById("modelName"),
    modelVersion: document.getElementById("modelVersion"),
    modelStatus: document.getElementById("modelStatus"),
    modelLoaded: document.getElementById("modelLoaded"),

    detailModelName: document.getElementById("detailModelName"),
    detailModelVersion: document.getElementById("detailModelVersion"),
    threshold: document.getElementById("threshold"),
    serviceName: document.getElementById("serviceName"),
    detailStatus: document.getElementById("detailStatus"),
    detailModelLoaded: document.getElementById("detailModelLoaded"),

    pageMessage: document.getElementById("pageMessage"),
    refreshButton: document.getElementById("refreshButton")
};


/*
 * Keep API response normalization separate from rendering.
 * This makes it easier to support a model registry later.
 */

function normalizeModelHealth(response) {
    if (!response || typeof response !== "object") {
        throw new Error("Invalid ML health response.");
    }

    return {
        modelName: "Fraud Detection Model",

        modelVersion:
            response.modelVersion ??
            response.model_version ??
            "--",

        status: response.status ?? "UNKNOWN",

        service: response.service ?? "--",

        modelLoaded:
            response.modelLoaded ??
            response.model_loaded ??
            false,

        threshold:
            response.predictionThreshold ??
            response.prediction_threshold ??
            null
    };
}



function setText(element, value) {
    if (element) {
        element.textContent = value;
    }
}


function showMessage(message, type = "info") {
    if (!elements.pageMessage) {
        return;
    }

    elements.pageMessage.textContent = message;
    elements.pageMessage.className = `page-message ${type}`;
}


function formatThreshold(value) {
    if (typeof value !== "number" || !Number.isFinite(value)) {
        return "--";
    }

    return `${(value * 100).toFixed(1)}%`;
}


function renderModelHealth(model) {
    setText(elements.modelName, model.modelName);
    setText(elements.modelVersion, model.modelVersion);
    setText(elements.modelStatus, model.status);
    setText(elements.modelLoaded, model.modelLoaded ? "Yes" : "No");

    setText(elements.detailModelName, model.modelName);
    setText(elements.detailModelVersion, model.modelVersion);
    setText(elements.threshold, formatThreshold(model.threshold));
    setText(elements.serviceName, model.service);
    setText(elements.detailStatus, model.status);
    setText(
        elements.detailModelLoaded,
        model.modelLoaded ? "Yes" : "No"
    );
}


async function loadModelAnalytics() {
    if (elements.refreshButton) {
        elements.refreshButton.disabled = true;
        elements.refreshButton.textContent = "Refreshing...";
    }

    showMessage("Loading model information...", "info");

    try {
        const response = await CreditGuardAPI.get("health/ml");
        const model = normalizeModelHealth(response);

        renderModelHealth(model);

        if (model.status.toUpperCase() === "UP" && model.modelLoaded) {
            showMessage(
                "ML service is connected and the fraud detection model is loaded.",
                "success"
            );
        } else {
            showMessage(
                "ML service responded, but the model may not be ready for predictions.",
                "error"
            );
        }

    } catch (error) {
        console.error("Failed to load ML model information:", error);

        showMessage(
            error.message || "Unable to retrieve ML model information.",
            "error"
        );

    } finally {
        if (elements.refreshButton) {
            elements.refreshButton.disabled = false;
            elements.refreshButton.textContent = "Refresh";
        }
    }
}


if (elements.refreshButton) {
    elements.refreshButton.addEventListener("click", loadModelAnalytics);
}

loadModelAnalytics();
