
/* =========================================
   CREDITGUARD - TRANSACTION DETAILS
========================================= */

(function () {
    "use strict";

    const elements = {
        message: document.getElementById("transactionMessage"),
        reference: document.getElementById("transactionReference"),
        date: document.getElementById("transactionDate"),
        status: document.getElementById("transactionStatus"),

        amountStat: document.getElementById("transactionAmount"),
        riskScoreStat: document.getElementById("transactionRiskScore"),
        riskLevelStat: document.getElementById("transactionRiskLevel"),
        modelVersionStat: document.getElementById("transactionModelVersion"),

        id: document.getElementById("transactionId"),
        externalReference: document.getElementById("externalReference"),
        amount: document.getElementById("amount"),
        currency: document.getElementById("currency"),
        merchant: document.getElementById("merchant"),
        country: document.getElementById("country"),
        transactionTime: document.getElementById("transactionTime"),
        maskedCard: document.getElementById("maskedCard"),
        statusDetail: document.getElementById("transactionStatusDetail"),

        predictionRiskLevel: document.getElementById("predictionRiskLevel"),
        predictionRiskScore: document.getElementById("predictionRiskScore"),
        predictionThreshold: document.getElementById("predictionThreshold"),
        riskPercentage: document.getElementById("riskPercentage"),
        riskProgressBar: document.getElementById("riskProgressBar"),
        predictionDate: document.getElementById("predictionDate"),
        predictionExplanation: document.getElementById("predictionExplanation"),

        refreshButton: document.getElementById("refreshTransactionButton")
    };

    const params = new URLSearchParams(window.location.search);
    const transactionId = params.get("id");

    function setText(element, value) {
        if (element) {
            element.textContent =
                value !== null && value !== undefined && value !== ""
                    ? value
                    : "--";
        }
    }

    function showMessage(message, type = "error") {
        if (!elements.message) return;

        elements.message.textContent = message;
        elements.message.hidden = false;
        elements.message.className = `alert alert-${type}`;
    }

    function hideMessage() {
        if (!elements.message) return;

        elements.message.hidden = true;
        elements.message.textContent = "";
    }

    function formatAmount(amount, currency) {
        if (amount === null || amount === undefined) {
            return "--";
        }

        return `${currency || ""} ${Number(amount).toLocaleString(
            "en-IN",
            {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            }
        )}`.trim();
    }

    function formatDate(value) {
        if (!value) return "--";

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return value;
        }

        return date.toLocaleString("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    function formatPercentage(value) {
        if (value === null || value === undefined) {
            return "--";
        }

        return `${(Number(value) * 100).toFixed(2)}%`;
    }

    function renderExplanations(explanations) {
        const container = elements.predictionExplanation;

        if (!container) return;

        container.replaceChildren();

        if (!explanations || explanations.length === 0) {
            container.textContent = "No prediction explanations available.";
            return;
        }

        const list = document.createElement("ul");

        explanations.forEach(item => {
            const li = document.createElement("li");

            const feature = document.createElement("strong");
            feature.textContent = item.feature || "Unknown feature";

            const impact = document.createElement("span");
            impact.textContent = ` — ${String(item.impact || "unknown")
                .replaceAll("_", " ")}`;

            const contribution = document.createElement("span");
            contribution.textContent =
                ` (Contribution: ${Number(item.contribution).toFixed(6)})`;

            li.append(feature, impact, contribution);
            list.appendChild(li);
        });

        container.appendChild(list);
    }

    function renderPrediction(prediction) {
        if (!prediction) {
            setText(elements.predictionRiskLevel, "UNKNOWN");
            setText(elements.predictionRiskScore, "--");
            setText(elements.predictionThreshold, "--");
            setText(elements.riskPercentage, "--");
            setText(elements.predictionDate, "--");

            if (elements.riskProgressBar) {
                elements.riskProgressBar.style.width = "0%";
                elements.riskProgressBar.setAttribute("aria-valuenow", "0");
            }

            renderExplanations([]);
            return;
        }

        const score = Number(prediction.riskScore) * 100;
        const percentage = Math.min(100, Math.max(0, score));

        setText(elements.predictionRiskLevel, prediction.riskLevel);
        setText(elements.predictionRiskScore, `${score.toFixed(2)}%`);
        setText(
            elements.predictionThreshold,
            formatPercentage(prediction.thresholdUsed)
        );
        setText(elements.riskPercentage, `${score.toFixed(2)}%`);
        setText(elements.predictionDate, formatDate(prediction.predictedAt));

        if (elements.riskProgressBar) {
            elements.riskProgressBar.style.width = `${percentage}%`;
            elements.riskProgressBar.setAttribute(
                "aria-valuenow",
                percentage.toFixed(2)
            );
        }

        renderExplanations(prediction.explanations);
    }

    function renderTransaction(transaction) {
        const prediction = transaction.prediction;

        const amount = formatAmount(
            transaction.amount,
            transaction.currency
        );

        setText(elements.reference, transaction.transactionReference);
        setText(elements.date, formatDate(transaction.transactionTime));
        setText(elements.status, transaction.status);

        setText(elements.amountStat, amount);
        setText(
            elements.riskScoreStat,
            prediction ? formatPercentage(prediction.riskScore) : "--"
        );
        setText(
            elements.riskLevelStat,
            prediction ? prediction.riskLevel : "UNKNOWN"
        );
        setText(
            elements.modelVersionStat,
            prediction ? prediction.modelVersion : "--"
        );

        setText(elements.id, transaction.id);
        setText(elements.externalReference, transaction.externalReference);
        setText(elements.amount, amount);
        setText(elements.currency, transaction.currency);
        setText(elements.merchant, transaction.merchantName);
        setText(elements.country, transaction.country);
        setText(elements.transactionTime, formatDate(transaction.transactionTime));
        setText(
            elements.maskedCard,
            transaction.maskedCardLast4
                ? `****${transaction.maskedCardLast4}`
                : "--"
        );
        setText(elements.statusDetail, transaction.status);

        renderPrediction(prediction);
    }

    async function loadTransaction() {
        hideMessage();

        if (!transactionId || !/^[1-9]\d*$/.test(transactionId)) {
            showMessage("A valid transaction ID is required.");
            return;
        }

        try {
            if (elements.refreshButton) {
                elements.refreshButton.disabled = true;
            }

            const transaction = await CreditGuardAPI.get(
                `/transactions/${encodeURIComponent(transactionId)}`
            );

            renderTransaction(transaction);

        } catch (error) {
            console.error("Transaction details error:", error);

            showMessage(
                error.status === 404
                    ? "Transaction not found."
                    : error.message || "Unable to load transaction details."
            );

        } finally {
            if (elements.refreshButton) {
                elements.refreshButton.disabled = false;
            }
        }
    }

    if (elements.refreshButton) {
        elements.refreshButton.addEventListener("click", loadTransaction);
    }

    loadTransaction();

})();
