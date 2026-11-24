
"use strict";

/* =========================================
   CREDITGUARD - MODEL ANALYTICS
   File: model-analytics.js
   API: GET /api/v1/models
========================================= */

const API_URL = "/api/v1/models";

const elements = {
    modelName: document.getElementById("modelName"),
    modelVersion: document.getElementById("modelVersion"),
    modelStatus: document.getElementById("modelStatus"),
    lastUpdated: document.getElementById("lastUpdated"),

    accuracy: document.getElementById("accuracy"),
    precision: document.getElementById("precision"),
    recall: document.getElementById("recall"),
    f1Score: document.getElementById("f1Score"),

    algorithm: document.getElementById("algorithm"),
    trainingDataset: document.getElementById("trainingDataset"),
    testingDataset: document.getElementById("testingDataset"),
    threshold: document.getElementById("threshold"),
    totalFeatures: document.getElementById("totalFeatures"),
    trainingDate: document.getElementById("trainingDate"),

    modelsTableBody: document.getElementById("modelsTableBody"),
    pageMessage: document.getElementById("pageMessage"),
    refreshButton: document.getElementById("refreshButton")
};


/* =========================================
   HELPER FUNCTIONS
========================================= */

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


function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => {
        const entities = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#039;"
        };

        return entities[character];
    });
}


function formatDate(value) {
    if (!value) return "--";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    return date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });
}


function formatMetric(value) {
    if (value === null || value === undefined || value === "") {
        return "--";
    }

    const number = Number(value);

    if (!Number.isFinite(number)) {
        return String(value);
    }

    // Supports metrics represented either as 0-1 or 0-100.
    const percentage = number <= 1 ? number * 100 : number;

    return `${percentage.toFixed(2)}%`;
}


function formatThreshold(value) {
    if (value === null || value === undefined || value === "") {
        return "--";
    }

    return Number.isFinite(Number(value))
        ? Number(value).toFixed(3)
        : String(value);
}


function showMessage(message, type = "info") {
    if (!elements.pageMessage) return;

    elements.pageMessage.textContent = message;
    elements.pageMessage.className = `page-message ${type}`;
}


function setText(element, value) {
    if (!element) return;

    element.textContent =
        value === null || value === undefined || value === ""
            ? "--"
            : String(value);
}


/* =========================================
   NORMALIZE API RESPONSE
========================================= */

function normalizeModels(response) {
    let models = response;

    if (Array.isArray(response)) {
        models = response;
    } else {
        models =
            response?.models ??
            response?.content ??
            response?.data ??
            response?.records ??
            response?.items ??
            (response && typeof response === "object" ? [response] : []);
    }

    return Array.isArray(models) ? models : [];
}


/* =========================================
   DISPLAY MODEL OVERVIEW
========================================= */

function displayModelOverview(model) {
    const metrics =
        model.metrics ??
        model.evaluationMetrics ??
        model.performance ??
        {};

    const dataset =
        model.dataset ??
        model.datasetInfo ??
        {};

    setText(
        elements.modelName,
        getValue(model, ["modelName", "name", "algorithm"], "--")
    );

    setText(
        elements.modelVersion,
        getValue(model, ["version", "modelVersion"], "--")
    );

    setText(
        elements.modelStatus,
        getValue(model, ["status", "deploymentStatus"], "--")
    );

    setText(
        elements.lastUpdated,
        formatDate(
            getValue(model, ["updatedAt", "lastUpdated", "createdAt"])
        )
    );

    // Performance metrics
    setText(
        elements.accuracy,
        formatMetric(getValue(metrics, ["accuracy"]) ??
                     getValue(model, ["accuracy"]))
    );

    setText(
        elements.precision,
        formatMetric(getValue(metrics, ["precision"]) ??
                     getValue(model, ["precision"]))
    );

    setText(
        elements.recall,
        formatMetric(getValue(metrics, ["recall"]) ??
                     getValue(model, ["recall"]))
    );

    setText(
        elements.f1Score,
        formatMetric(getValue(metrics, ["f1Score", "f1_score", "f1"]) ??
                     getValue(model, ["f1Score", "f1_score", "f1"]))
    );

    // Technical details
    setText(
        elements.algorithm,
        getValue(model, ["algorithm", "algorithmName", "modelType"], "--")
    );

    setText(
        elements.trainingDataset,
        getValue(dataset, ["trainingSize", "trainingDatasetSize"]) ??
        getValue(model, ["trainingDataset", "trainingSize"], "--")
    );

    setText(
        elements.testingDataset,
        getValue(dataset, ["testingSize", "testingDatasetSize"]) ??
        getValue(model, ["testingDataset", "testingSize"], "--")
    );

    setText(
        elements.threshold,
        formatThreshold(
            getValue(model, ["threshold", "thresholdUsed"])
        )
    );

    setText(
        elements.totalFeatures,
        getValue(model, ["totalFeatures", "featureCount"], "--")
    );

    setText(
        elements.trainingDate,
        formatDate(getValue(model, ["trainingDate", "trainedAt"]))
    );
}


/* =========================================
   DISPLAY MODEL VERSION TABLE
========================================= */

function displayModelsTable(models) {
    if (!elements.modelsTableBody) return;

    if (!models.length) {
        elements.modelsTableBody.innerHTML = `
            <tr>
                <td colspan="6" class="empty-state">
                    No model information available.
                </td>
            </tr>
        `;
        return;
    }

    elements.modelsTableBody.innerHTML = models.map(model => {
        const id = getValue(model, ["id", "modelId"], "--");
        const name = getValue(
            model,
            ["modelName", "name", "algorithm"],
            "--"
        );
        const version = getValue(
            model,
            ["version", "modelVersion"],
            "--"
        );
        const accuracy = formatMetric(
            getValue(model, ["metrics.accuracy", "accuracy"])
        );
        const status = getValue(
            model,
            ["status", "deploymentStatus"],
            "UNKNOWN"
        );
        const createdAt = formatDate(
            getValue(model, ["createdAt", "trainingDate", "trainedAt"])
        );

        return `
            <tr>
                <td>${escapeHTML(id)}</td>
                <td>${escapeHTML(name)}</td>
                <td>${escapeHTML(version)}</td>
                <td>${escapeHTML(accuracy)}</td>
                <td>
                    <span class="status-badge">
                        ${escapeHTML(status)}
                    </span>
                </td>
                <td>${escapeHTML(createdAt)}</td>
            </tr>
        `;
    }).join("");
}


/* =========================================
   FETCH MODELS FROM BACKEND
========================================= */

async function loadModelAnalytics() {
    try {
        showMessage("Loading model analytics...", "info");

        if (elements.refreshButton) {
            elements.refreshButton.disabled = true;
        }

        const response = await fetch(API_URL, {
            method: "GET",
            credentials: "include",
            headers: {
                "Accept": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(
                `Server error: ${response.status} ${response.statusText}`
            );
        }

        const result = await response.json();
        const models = normalizeModels(result);

        if (!models.length) {
            displayModelsTable([]);
            showMessage("No model data returned by the server.", "info");
            return;
        }

        // Select the deployed/current model if available.
        const activeModel =
            models.find(model =>
                ["DEPLOYED", "ACTIVE", "PRODUCTION"].includes(
                    String(
                        getValue(model, ["status", "deploymentStatus"], "")
                    ).toUpperCase()
                )
            ) ?? models[0];

        displayModelOverview(activeModel);
        displayModelsTable(models);

        showMessage("Model analytics loaded successfully.", "success");

    } catch (error) {
        console.error("Model Analytics Error:", error);

        showMessage(
            "Unable to load model analytics. Please check the backend connection.",
            "error"
        );

        if (elements.modelsTableBody) {
            elements.modelsTableBody.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-state">
                        Failed to load model information.
                    </td>
                </tr>
            `;
        }

    } finally {
        if (elements.refreshButton) {
            elements.refreshButton.disabled = false;
        }
    }
}


/* =========================================
   EVENT LISTENERS
========================================= */

if (elements.refreshButton) {
    elements.refreshButton.addEventListener(
        "click",
        loadModelAnalytics
    );
}


/* =========================================
   INITIALIZE PAGE
========================================= */

document.addEventListener("DOMContentLoaded", () => {
    loadModelAnalytics();
});
