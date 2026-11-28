
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
    const localTransactionTime = formData.get("transactionTime");
    const parsedTransactionTime = localTransactionTime
        ? new Date(localTransactionTime)
        : null;

    const transactionTimeUtc =
        parsedTransactionTime &&
        !Number.isNaN(parsedTransactionTime.getTime())
            ? parsedTransactionTime.toISOString()
            : localTransactionTime;
    return {
        externalReference:
            formData.get("externalReference")?.trim() || null,

        amount: Number(formData.get("amount")),

        currency: formData.get("currency"),

        transactionTime: transactionTimeUtc,

        merchantName:
            formData.get("merchantName")?.trim(),

        // BankSim merchant ID required by the ML model.
        merchant: formData.get("merchant")?.trim(),

        // ML model features.
        step: Number(formData.get("step")),
        age: formData.get("age"),
        gender: formData.get("gender"),
        category: formData.get("category"),

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

    const transactionDate = new Date(payload.transactionTime);

    if (Number.isNaN(transactionDate.getTime())) {
        return "Please enter a valid transaction date and time.";
    }

    if (!payload.merchantName) {
        return "Please enter merchant name.";
    }

    if (!payload.merchant) {
        return "Please select an ML merchant ID.";
    }

    if (
        !Number.isInteger(payload.step) ||
        payload.step < 0 ||
        payload.step > 179
    ) {
        return "BankSim step must be an integer between 0 and 179.";
    }

    if (!["0", "1", "2", "3", "4", "5", "6", "U"].includes(payload.age)) {
        return "Please select a valid age code.";
    }

    if (!["E", "F", "M", "U"].includes(payload.gender)) {
        return "Please select a valid gender code.";
    }

    const validCategories = [
        "es_barsandrestaurants",
        "es_contents",
        "es_fashion",
        "es_food",
        "es_health",
        "es_home",
        "es_hotelservices",
        "es_hyper",
        "es_leisure",
        "es_otherservices",
        "es_sportsandtoys",
        "es_tech",
        "es_transportation",
        "es_travel",
        "es_wellnessandbeauty"
    ];

    if (!validCategories.includes(payload.category)) {
        return "Please select a valid transaction category.";
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

        const result = await CreditGuardAPI.post(
    "transactions/predict",
    payload
);
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

/* =========================================
   PREDICTION HISTORY MODULE
========================================= */

(function () {
    "use strict";

    const API_ENDPOINT = "/transactions/predictions";
    const PAGE_SIZE = 10;

    let currentPage = 0;
    let totalPages = 0;
    let currentRecords = [];

    const elements = {
        message: document.getElementById("historyMessage"),

        refreshButton: document.getElementById("refreshHistoryButton"),
        applyFiltersButton: document.getElementById("applyHistoryFilters"),
        resetFiltersButton: document.getElementById("resetHistoryFilters"),

        riskFilter: document.getElementById("historyRiskFilter"),
        predictionFilter: document.getElementById("historyPredictionFilter"),
        referenceFilter: document.getElementById("historyReferenceFilter"),
        startDate: document.getElementById("historyStartDate"),
        endDate: document.getElementById("historyEndDate"),

        tableBody: document.getElementById("predictionHistoryTableBody"),
        recordCount: document.getElementById("historyRecordCount"),

        paginationInfo: document.getElementById("historyPaginationInfo"),
        previousButton: document.getElementById("historyPreviousPage"),
        nextButton: document.getElementById("historyNextPage"),

        detailsSection: document.getElementById("historyDetailsSection"),
        closeDetailsButton: document.getElementById("closeHistoryDetails"),

        explanationTableBody: document.getElementById("historyExplanationTableBody")
    };

    const detailFields = {
        transactionReference: document.getElementById("detailTransactionReference"),
        predictionId: document.getElementById("detailPredictionId"),
        merchantName: document.getElementById("detailMerchantName"),
        merchantId: document.getElementById("detailMerchantId"),
        category: document.getElementById("detailCategory"),
        country: document.getElementById("detailCountry"),
        transactionTime: document.getElementById("detailTransactionTime"),
        predictedAt: document.getElementById("detailPredictedAt"),
        modelVersion: document.getElementById("detailModelVersion"),
        threshold: document.getElementById("detailThreshold"),
        correlationId: document.getElementById("detailCorrelationId")
    };

    // Stop initialization if the history section is not present.
    if (!elements.tableBody) {
        return;
    }

    /**
     * Safely escape dynamic values before inserting HTML.
     */
    function escapeHTML(value) {
        return String(value ?? "").replace(/[&<>"']/g, character => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        })[character]);
    }

    /**
     * Display a history-specific message.
     */
    function showHistoryMessage(message, type = "info") {
    if (!elements.message) {
        return;
    }

    elements.message.textContent = message;
    elements.message.className = message
        ? `page-message ${type}`
        : "page-message";

    elements.message.hidden = !message;
}

    /**
     * Format a date for display.
     */
    function formatHistoryDate(value) {
        if (!value) {
            return "--";
        }

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return "--";
        }

        return date.toLocaleString();
    }

    /**
     * Format a monetary amount.
     */
    function formatHistoryAmount(amount, currency) {
        const numericAmount = Number(amount);

        if (!Number.isFinite(numericAmount)) {
            return "--";
        }

        return `${escapeHTML(currency || "")} ${numericAmount.toLocaleString(
            undefined,
            {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }
        )}`.trim();
    }

    /**
     * Format risk score as percentage.
     */
    function formatHistoryRiskScore(score) {
        const numericScore = Number(score);

        if (!Number.isFinite(numericScore)) {
            return "--";
        }

        return `${(numericScore * 100).toFixed(2)}%`;
    }

    /**
     * Format prediction class.
     */
    function getPredictionLabel(prediction) {
        if (Number(prediction) === 1) {
            return "Fraud";
        }

        if (Number(prediction) === 0) {
            return "Legitimate";
        }

        return "Unknown";
    }

    /**
     * Convert risk level into a CSS class.
     */
    function getRiskClass(riskLevel) {
        const value = String(riskLevel || "").toLowerCase();

        if (["low", "medium", "high"].includes(value)) {
            return value;
        }

        return "";
    }

    /**
     * Read active filters.
     */
    function getFilters() {
        return {
            riskLevel: elements.riskFilter.value,
            prediction: elements.predictionFilter.value,
            transactionReference: elements.referenceFilter.value.trim(),
            startDate: elements.startDate.value,
            endDate: elements.endDate.value
        };
    }


    /**
     * Build the backend query string.
     */
    function buildHistoryQuery() {
        const filters = getFilters();
        const params = new URLSearchParams();

        params.set("page", currentPage);
        params.set("size", PAGE_SIZE);

        if (filters.riskLevel) {
            params.set("riskLevel", filters.riskLevel);
        }

        if (filters.prediction !== "") {
            params.set("prediction", filters.prediction);
        }

        if (filters.transactionReference) {
            params.set(
                "transactionReference",
                filters.transactionReference
            );
        }

        // Backend expects LocalDateTime, not UTC timestamps.
        if (filters.startDate) {
            params.set(
                "startDate",
                `${filters.startDate}:00`
            );
        }

        if (filters.endDate) {
            params.set(
                "endDate",
                `${filters.endDate}:00`
            );
        }

        return `${API_ENDPOINT}?${params.toString()}`;
    }


    /**
     * Render prediction history records.
     */
    function renderHistoryTable(records) {
        if (!records.length) {
            renderTableMessage(
                "No prediction records found for the selected filters."
            );

            return;
        }

        elements.tableBody.innerHTML = records.map(record => {
            const riskClass = getRiskClass(record.riskLevel);
            const predictionLabel = getPredictionLabel(record.prediction);

            const predictionClass =
                Number(record.prediction) === 1
                    ? "fraud"
                    : Number(record.prediction) === 0
                        ? "legitimate"
                        : "";

            return `
                <tr>
                    <td>${escapeHTML(record.transactionId || "--")}</td>

                    <td>
                        ${formatHistoryAmount(record.amount, record.currency)}
                    </td>

                    <td>
                        ${formatHistoryRiskScore(record.riskScore)}
                    </td>

                    <td>
                        <span class="history-risk ${riskClass}">
                            ${escapeHTML(record.riskLevel || "Unknown")}
                        </span>
                    </td>

                    <td>
                        <span class="history-prediction ${predictionClass}">
                            ${escapeHTML(predictionLabel)}
                        </span>
                    </td>

                    <td>
                        ${escapeHTML(formatHistoryDate(record.predictedAt))}
                    </td>

                    <td>
                        <button
                            type="button"
                            class="history-view-button"
                            data-prediction-id="${escapeHTML(record.predictionId)}">
                            View Details
                        </button>
                    </td>
                </tr>
            `;
        }).join("");
    }


    /**
     * Safely populate a prediction detail field.
     */
    function setDetailValue(element, value) {
        if (element) {
            element.textContent =
                value === null || value === undefined || value === ""
                    ? "--"
                    : String(value);
        }
    }

    /**
     * Render SHAP explanations.
     */
    function renderHistoryExplanations(explanations) {
        if (!Array.isArray(explanations) || !explanations.length) {
            elements.explanationTableBody.innerHTML = `
                <tr>
                    <td colspan="4" class="history-empty-cell">
                        No SHAP explanations available.
                    </td>
                </tr>
            `;
            return;
        }

        elements.explanationTableBody.innerHTML =
            explanations.map(item => {
                const contribution = Number(item.contribution);

                const impactClass =
                    item.impact === "increases_fraud_risk"
                        ? "positive"
                        : item.impact === "decreases_fraud_risk"
                            ? "negative"
                            : "";

                const impactLabel =
                    item.impact === "increases_fraud_risk"
                        ? "Increases fraud risk"
                        : item.impact === "decreases_fraud_risk"
                            ? "Decreases fraud risk"
                            : "Unknown";

                return `
                    <tr>
                        <td>${escapeHTML(item.rank ?? "--")}</td>

                        <td>${escapeHTML(item.feature || "--")}</td>

                        <td>
                            ${
                                Number.isFinite(contribution)
                                    ? contribution.toFixed(6)
                                    : "--"
                            }
                        </td>

                        <td>
                            <span class="history-impact ${impactClass}">
                                ${escapeHTML(impactLabel)}
                            </span>
                        </td>
                    </tr>
                `;
            }).join("");
    }

    /**
     * Display details for a selected prediction.
     */
    function showPredictionDetails(record) {
        if (!record || !elements.detailsSection) {
            return;
        }

        setDetailValue(
            detailFields.transactionReference,
            record.transactionId
        );

        setDetailValue(
            detailFields.predictionId,
            record.predictionId
        );

        setDetailValue(
            detailFields.merchantName,
            record.merchantName
        );

        setDetailValue(
            detailFields.merchantId,
            record.merchantId
        );

        setDetailValue(
            detailFields.category,
            record.category
        );

        setDetailValue(
            detailFields.country,
            record.country
        );

        setDetailValue(
            detailFields.transactionTime,
            formatHistoryDate(record.transactionTime)
        );

        setDetailValue(
            detailFields.predictedAt,
            formatHistoryDate(record.predictedAt)
        );

        setDetailValue(
            detailFields.modelVersion,
            record.modelVersion
        );

        setDetailValue(
            detailFields.threshold,
            record.thresholdUsed
        );

        setDetailValue(
            detailFields.correlationId,
            record.correlationId
        );

        renderHistoryExplanations(record.explanations);

        elements.detailsSection.hidden = false;

        elements.detailsSection.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    }

    /**
     * Close the prediction details panel.
     */
    function closePredictionDetails() {
        if (elements.detailsSection) {
            elements.detailsSection.hidden = true;
        }
    }


    /**
     * Update pagination controls.
     */
    function updatePagination(response) {
        currentPage = response.page ?? 0;
        totalPages = response.totalPages ?? 0;

        const displayPage = totalPages === 0 ? 0 : currentPage + 1;

        elements.paginationInfo.textContent =
            `Page ${displayPage} of ${totalPages}`;

        elements.previousButton.disabled =
            currentPage <= 0;

        elements.nextButton.disabled =
            totalPages === 0 || currentPage >= totalPages - 1;

        elements.recordCount.textContent =
            `${response.totalElements ?? 0} records`;
    }

    /**
     * Render empty/loading/error table messages.
     */
    function renderTableMessage(message) {
        elements.tableBody.innerHTML = `
            <tr>
                <td colspan="7" class="history-empty-cell">
                    ${escapeHTML(message)}
                </td>
            </tr>
        `;
    }

    /**
     * Load prediction history from the backend.
     */
    async function loadPredictionHistory() {
        elements.refreshButton.disabled = true;
        elements.applyFiltersButton.disabled = true;
        elements.resetFiltersButton.disabled = true;

        renderTableMessage("Loading prediction history...");

        showHistoryMessage("");

        try {
            const response = await CreditGuardAPI.get(
                buildHistoryQuery()
            );

            if (!response || !Array.isArray(response.data)) {
                throw new Error(
                    "Invalid prediction history response."
                );
            }

            currentRecords = response.data;

            renderHistoryTable(currentRecords);
            updatePagination(response);

        } catch (error) {
            console.error("Prediction History Error:", error);

            currentRecords = [];

            renderTableMessage(
                "Unable to load prediction history."
            );

            elements.recordCount.textContent = "0 records";
            elements.paginationInfo.textContent = "Page 0 of 0";

            elements.previousButton.disabled = true;
            elements.nextButton.disabled = true;

            showHistoryMessage(
                error.message || "Unable to load prediction history.",
                "error"
            );

        } finally {
            elements.refreshButton.disabled = false;
            elements.applyFiltersButton.disabled = false;
            elements.resetFiltersButton.disabled = false;
        }
    }

    /**
     * Validate the selected prediction date range.
     */
    function validateHistoryFilters() {
        const startValue = elements.startDate.value;
        const endValue = elements.endDate.value;

        if (startValue && endValue && startValue > endValue) {
            showHistoryMessage(
                "Start date must be before or equal to end date.",
                "error"
            );

            return false;
        }

        return true;
    }

    /**
     * Apply history filters.
     */
    elements.applyFiltersButton.addEventListener("click", () => {
        if (!validateHistoryFilters()) {
            return;
        }

        currentPage = 0;
        closePredictionDetails();
        loadPredictionHistory();
    });

    /**
     * Reset all history filters.
     */
    elements.resetFiltersButton.addEventListener("click", () => {
        elements.riskFilter.value = "";
        elements.predictionFilter.value = "";
        elements.referenceFilter.value = "";
        elements.startDate.value = "";
        elements.endDate.value = "";

        currentPage = 0;

        closePredictionDetails();
        showHistoryMessage("");

        loadPredictionHistory();
    });

    /**
     * Refresh current history page.
     */
    elements.refreshButton.addEventListener("click", () => {
        loadPredictionHistory();
    });

    /**
     * Navigate to previous page.
     */
    elements.previousButton.addEventListener("click", () => {
        if (currentPage <= 0) {
            return;
        }

        currentPage--;
        closePredictionDetails();
        loadPredictionHistory();
    });

    /**
     * Navigate to next page.
     */
    elements.nextButton.addEventListener("click", () => {
        if (currentPage >= totalPages - 1) {
            return;
        }

        currentPage++;
        closePredictionDetails();
        loadPredictionHistory();
    });

    /**
     * Handle View Details button clicks.
     */
    elements.tableBody.addEventListener("click", event => {
        const button = event.target.closest(
            ".history-view-button"
        );

        if (!button) {
            return;
        }

        const predictionId = button.dataset.predictionId;

        const record = currentRecords.find(
            item => String(item.predictionId) === predictionId
        );

        if (record) {
            showPredictionDetails(record);
        }
    });

    /**
     * Close details panel.
     */
    elements.closeDetailsButton.addEventListener(
        "click",
        closePredictionDetails
    );

    /**
     * Initial history load.
     */
    loadPredictionHistory();

})();