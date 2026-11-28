"use strict";

const API_URL = "/api/v1/investigations";

const params = new URLSearchParams(window.location.search);
const investigationId = params.get("id");

const elements = {
    caseId: document.getElementById("caseId"),
    caseStatus: document.getElementById("caseStatus"),
    casePriority: document.getElementById("casePriority"),

    transactionId: document.getElementById("transactionId"),
    transactionAmount: document.getElementById("transactionAmount"),
    currency: document.getElementById("currency"),
    merchant: document.getElementById("merchant"),
    transactionDate: document.getElementById("transactionDate"),
    maskedCard: document.getElementById("maskedCard"),

    riskScore: document.getElementById("riskScore"),
    riskLevel: document.getElementById("riskLevel"),
    modelVersion: document.getElementById("modelVersion"),
    predictionDate: document.getElementById("predictionDate"),

    form: document.getElementById("investigationForm"),
    assignedTo: document.getElementById("assignedTo"),
    status: document.getElementById("investigationStatus"),
    outcome: document.getElementById("investigationOutcome"),
    remarks: document.getElementById("remarks"),
    formMessage: document.getElementById("formMessage"),

    timeline: document.getElementById("investigationTimeline"),
    notes: document.getElementById("investigationNotes")
};

let currentInvestigation = null;

function formatDate(value) {
    if (!value) return "—";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString();
}

function formatAmount(amount, currency) {
    if (amount === null || amount === undefined) return "—";

    const value = Number(amount);

    if (Number.isNaN(value)) return amount;

    return `${currency || ""} ${value.toLocaleString("en-IN", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    })}`.trim();
}

function formatRiskScore(score) {
    if (score === null || score === undefined) return "—";

    const value = Number(score);

    if (Number.isNaN(value)) return "—";

    return `${(value <= 1 ? value * 100 : value).toFixed(2)}%`;
}

function formatLabel(value) {
    if (!value) return "—";

    return value
        .toLowerCase()
        .split("_")
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(" ");
}

function setMessage(message, isError = false) {
    elements.formMessage.textContent = message;
    elements.formMessage.style.color = isError ? "#dc2626" : "#15803d";
}

function createTextElement(tag, className, text) {
    const element = document.createElement(tag);

    if (className) {
        element.className = className;
    }

    element.textContent = text ?? "—";

    return element;
}

function populateForm(investigation) {
    elements.assignedTo.value = investigation.assignedTo || "";
    elements.status.value = investigation.status || "OPEN";
    elements.outcome.value = investigation.outcome || "";
    elements.remarks.value = "";
}

function renderHistory(history) {
    elements.timeline.replaceChildren();

    if (!Array.isArray(history) || history.length === 0) {
        elements.timeline.appendChild(
            createTextElement("p", "empty-state", "No investigation history available.")
        );
        return;
    }

    history.forEach(event => {
        const item = document.createElement("div");
        item.className = "timeline-item";

        item.appendChild(
            createTextElement("h4", "", formatLabel(event.eventType))
        );

        item.appendChild(
            createTextElement("p", "", event.description)
        );

        const metadata = [
            formatDate(event.createdAt),
            event.changedBy ? `By ${event.changedBy}` : ""
        ].filter(Boolean).join(" · ");

        item.appendChild(
            createTextElement("p", "", metadata)
        );

        elements.timeline.appendChild(item);
    });
}

function renderNotes(notes) {
    elements.notes.replaceChildren();

    if (!Array.isArray(notes) || notes.length === 0) {
        elements.notes.appendChild(
            createTextElement("p", "empty-state", "No investigation notes available.")
        );
        return;
    }

    notes.forEach(note => {
        const card = document.createElement("div");
        card.className = "timeline-item";

        card.appendChild(
            createTextElement("h4", "", note.author || "Analyst")
        );

        card.appendChild(
            createTextElement("p", "", note.note)
        );

        card.appendChild(
            createTextElement("p", "", formatDate(note.createdAt))
        );

        elements.notes.appendChild(card);
    });
}

function renderInvestigation(investigation) {
    const transaction = investigation.transaction || {};
    const prediction = investigation.prediction || {};

    elements.caseId.textContent = investigation.caseId || "—";
    elements.caseStatus.textContent = formatLabel(investigation.status);
    elements.casePriority.textContent = formatLabel(investigation.priority);

    elements.transactionId.textContent =
        transaction.transactionReference ||
        transaction.externalReference ||
        transaction.id ||
        "—";

    elements.transactionAmount.textContent =
        formatAmount(transaction.amount, transaction.currency);

    elements.currency.textContent = transaction.currency || "—";
    elements.merchant.textContent =
        transaction.merchantName || transaction.merchantId || "—";

    elements.transactionDate.textContent =
        formatDate(transaction.transactionTime);

    elements.maskedCard.textContent =
        transaction.maskedCardLast4
            ? `**** **** **** ${transaction.maskedCardLast4}`
            : "—";

    elements.riskScore.textContent = formatRiskScore(prediction.riskScore);
    elements.riskLevel.textContent =
        formatLabel(prediction.riskLevel || investigation.priority);

    elements.modelVersion.textContent = prediction.modelVersion || "—";
    elements.predictionDate.textContent = formatDate(prediction.predictedAt);

    populateForm(investigation);
    renderHistory(investigation.history);
    renderNotes(investigation.notes);
}

async function loadInvestigation() {
    if (!investigationId || !/^\d+$/.test(investigationId)) {
        setMessage("Invalid investigation ID.", true);
        return;
    }

    try {
        const result = await CreditGuardAPI.get(
    `investigations/${encodeURIComponent(investigationId)}`
);

        currentInvestigation = result.data ?? result;

        renderInvestigation(currentInvestigation);
    } catch (error) {
        console.error("Investigation loading failed:", error);
        setMessage(error.message || "Failed to load investigation.", true);
    }
}

elements.form.addEventListener("submit", async event => {
    event.preventDefault();

    if (!currentInvestigation) {
        setMessage("Investigation details have not loaded yet.", true);
        return;
    }

    const requestBody = {
        assignedTo: elements.assignedTo.value.trim() || null,
        status: elements.status.value,
        outcome: elements.outcome.value || null,
        remarks: elements.remarks.value.trim() || null,
        changedBy: null
    };

    const submitButton = elements.form.querySelector(
        'button[type="submit"]'
    );

    submitButton.disabled = true;
    setMessage("Saving investigation...");

    try {
        const result = await CreditGuardAPI.put(
    `investigations/${encodeURIComponent(investigationId)}`,
    requestBody
);
        currentInvestigation = result.data ?? result;
        renderInvestigation(currentInvestigation);
        setMessage("Investigation updated successfully.");
    } catch (error) {
        console.error("Investigation update failed:", error);
        setMessage(error.message || "Failed to update investigation.", true);
    } finally {
        submitButton.disabled = false;
    }
});

elements.form.addEventListener("reset", () => {
    if (!currentInvestigation) return;

    setTimeout(() => {
        populateForm(currentInvestigation);
        setMessage("");
    }, 0);
});

loadInvestigation();
