
/* =========================================
   CREDITGUARD - TRANSACTIONS MANAGEMENT
   File: Frontend/transactions/transactions.js
========================================= */

(function () {
    "use strict";

    // ---------- Configuration ----------

    const API_ENDPOINT = "/transactions";
    const PAGE_SIZE = 10;

    let transactions = [];
    let filteredTransactions = [];
    let currentPage = 1;
    let isLoading = false;

    // ---------- DOM Elements ----------

    const elements = {
        message: document.getElementById("transactionMessage"),

        total: document.getElementById("totalTransactions"),
        fraud: document.getElementById("fraudTransactions"),
        lowRisk: document.getElementById("lowRiskTransactions"),
        highRisk: document.getElementById("highRiskTransactions"),

        search: document.getElementById("transactionSearch"),
        riskFilter: document.getElementById("riskFilter"),
        statusFilter: document.getElementById("statusFilter"),
        currencyFilter: document.getElementById("currencyFilter"),

        tableBody: document.getElementById("transactionsTableBody"),

        refresh: document.getElementById("refreshTransactions"),
        clearFilters: document.getElementById("clearFilters"),

        paginationInfo: document.getElementById("paginationInfo"),
        previous: document.getElementById("previousPage"),
        next: document.getElementById("nextPage")
    };

    // ---------- Utility Functions ----------

    function escapeHTML(value) {
        return String(value ?? "").replace(/[&<>"']/g, char => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#039;"
        })[char]);
    }

    function getValue(object, ...keys) {
        for (const key of keys) {
            const value = object?.[key];

            if (value !== undefined && value !== null) {
                return value;
            }
        }

        return null;
    }

    function formatAmount(amount, currency = "INR") {
        const value = Number(amount);

        if (!Number.isFinite(value)) return "--";

        try {
            return new Intl.NumberFormat("en-IN", {
                style: "currency",
                currency: currency,
                maximumFractionDigits: 2
            }).format(value);
        } catch {
            return `${currency} ${value.toFixed(2)}`;
        }
    }

    function formatDate(date) {
        if (!date) return "--";

        const parsedDate = new Date(date);

        if (Number.isNaN(parsedDate.getTime())) {
            return escapeHTML(date);
        }

        return parsedDate.toLocaleString("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    function getScore(transaction) {
        const score = Number(
            getValue(
                transaction,
                "riskScore",
                "risk_score",
                "predictionScore",
                "score"
            )
        );

        return Number.isFinite(score) && score >= 0 && score <= 1
            ? score
            : null;
    }

    function getRiskLevel(transaction) {
        const suppliedLevel = getValue(
            transaction,
            "riskLevel",
            "risk_level"
        );

        if (suppliedLevel) {
            return String(suppliedLevel).toUpperCase();
        }

        const score = getScore(transaction);

        if (score === null) return "UNKNOWN";

        // Illustrative display bands only.
        if (score < 0.30) return "LOW";
        if (score < 0.70) return "MEDIUM";

        return "HIGH";
    }

    function getTransactionId(transaction) {
        return getValue(
            transaction,
            "id",
            "transactionId",
            "transaction_id"
        );
    }

    function getStatus(transaction) {
        return String(
            getValue(
                transaction,
                "status",
                "transactionStatus",
                "transaction_status"
            ) || "UNKNOWN"
        ).toUpperCase();
    }

    // ---------- Message Handling ----------

    function showMessage(message, type = "info") {
        if (!elements.message) return;

        elements.message.textContent = message;
        elements.message.className = `alert alert-${type}`;
        elements.message.hidden = false;
    }

    function hideMessage() {
        if (!elements.message) return;

        elements.message.hidden = true;
        elements.message.textContent = "";
    }

    // ---------- Response Normalization ----------

    function normalizeResponse(response) {
        if (Array.isArray(response)) {
            return response;
        }

        if (Array.isArray(response?.content)) {
            return response.content;
        }

        if (Array.isArray(response?.transactions)) {
            return response.transactions;
        }

        if (Array.isArray(response?.data)) {
            return response.data;
        }

        if (Array.isArray(response?.data?.transactions)) {
            return response.data.transactions;
        }

        return null;
    }

    // ---------- Load Transactions ----------

    async function loadTransactions() {
        if (isLoading) return;

        isLoading = true;
        hideMessage();

        if (elements.refresh) {
            elements.refresh.disabled = true;
            elements.refresh.textContent = "Loading...";
        }

        if (elements.tableBody) {
            elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="8" class="loading-state">
                        Loading transactions...
                    </td>
                </tr>
            `;
        }

        try {
            const response = await CreditGuardAPI.get(API_ENDPOINT);

            const data = normalizeResponse(response);

            if (!data) {
                throw new Error(
                    "Unexpected transaction API response format."
                );
            }

            transactions = data;
            currentPage = 1;

            updateStatistics();
            applyFilters();

        } catch (error) {
            console.error("Transaction loading error:", error);

            transactions = [];
            filteredTransactions = [];

            updateStatistics();

            if (elements.tableBody) {
                elements.tableBody.innerHTML = `
                    <tr>
                        <td colspan="8" class="empty-state">
                            Unable to load transactions.
                        </td>
                    </tr>
                `;
            }

            if (elements.paginationInfo) {
                elements.paginationInfo.textContent =
                    "Transactions unavailable";
            }

            updatePaginationButtons();

            showMessage(
                error.message || "Failed to load transactions.",
                "error"
            );

        } finally {
            isLoading = false;

            if (elements.refresh) {
                elements.refresh.disabled = false;
                elements.refresh.textContent = "Refresh";
            }
        }
    }

    // ---------- Statistics ----------

    function updateStatistics() {
        const total = transactions.length;

        const fraud = transactions.filter(transaction => {
            const level = getRiskLevel(transaction);

            return level === "HIGH" ||
                String(getValue(transaction, "predictionLabel", "prediction_label") || "")
                    .toUpperCase() === "FRAUD";
        }).length;

        const low = transactions.filter(
            transaction => getRiskLevel(transaction) === "LOW"
        ).length;

        const high = transactions.filter(
            transaction => getRiskLevel(transaction) === "HIGH"
        ).length;

        if (elements.total) {
            elements.total.textContent = total.toLocaleString("en-IN");
        }

        if (elements.fraud) {
            elements.fraud.textContent = fraud.toLocaleString("en-IN");
        }

        if (elements.lowRisk) {
            elements.lowRisk.textContent = low.toLocaleString("en-IN");
        }

        if (elements.highRisk) {
            elements.highRisk.textContent = high.toLocaleString("en-IN");
        }
    }

    // ---------- Search and Filters ----------

    function applyFilters() {
        const searchTerm = (
            elements.search?.value || ""
        ).trim().toLowerCase();

        const selectedRisk = elements.riskFilter?.value || "";
        const selectedStatus = elements.statusFilter?.value || "";
        const selectedCurrency = elements.currencyFilter?.value || "";

        filteredTransactions = transactions.filter(transaction => {

            const id = String(
                getTransactionId(transaction) || ""
            ).toLowerCase();

            const reference = String(
                getValue(
                    transaction,
                    "externalReference",
                    "external_reference"
                ) || ""
            ).toLowerCase();

            const merchant = String(
                getValue(transaction, "merchant") || ""
            ).toLowerCase();

            const matchesSearch =
                !searchTerm ||
                id.includes(searchTerm) ||
                reference.includes(searchTerm) ||
                merchant.includes(searchTerm);

            const matchesRisk =
                !selectedRisk ||
                getRiskLevel(transaction) === selectedRisk;

            const matchesStatus =
                !selectedStatus ||
                getStatus(transaction) === selectedStatus;

            const currency = String(
                getValue(transaction, "currency") || ""
            ).toUpperCase();

            const matchesCurrency =
                !selectedCurrency ||
                currency === selectedCurrency;

            return matchesSearch &&
                matchesRisk &&
                matchesStatus &&
                matchesCurrency;
        });

        currentPage = 1;
        renderTransactions();
    }

    // ---------- Risk Badge ----------

    function getRiskBadge(level) {
        const classes = {
            LOW: "badge-success",
            MEDIUM: "badge-warning",
            HIGH: "badge-danger",
            UNKNOWN: "badge-neutral"
        };

        const badgeClass = classes[level] || "badge-neutral";

        return `
            <span class="badge ${badgeClass}">
                ${escapeHTML(level)}
            </span>
        `;
    }

    // ---------- Status Badge ----------

    function getStatusBadge(status) {
        const classes = {
            COMPLETED: "badge-success",
            PENDING: "badge-warning",
            FAILED: "badge-danger"
        };

        const badgeClass = classes[status] || "badge-neutral";

        return `
            <span class="badge ${badgeClass}">
                ${escapeHTML(status)}
            </span>
        `;
    }

    // ---------- Render Table ----------

    function renderTransactions() {
        if (!elements.tableBody) return;

        if (filteredTransactions.length === 0) {
            elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="8" class="empty-state">
                        No transactions found.
                    </td>
                </tr>
            `;

            updatePaginationButtons();

            if (elements.paginationInfo) {
                elements.paginationInfo.textContent =
                    "Showing 0 transactions";
            }

            return;
        }

        const startIndex = (currentPage - 1) * PAGE_SIZE;

        const pageTransactions = filteredTransactions.slice(
            startIndex,
            startIndex + PAGE_SIZE
        );

        elements.tableBody.innerHTML = pageTransactions.map(transaction => {

            const id = getTransactionId(transaction);

            const reference = getValue(
                transaction,
                "externalReference",
                "external_reference"
            );

            const merchant = getValue(transaction, "merchant") || "--";

            const amount = getValue(transaction, "amount");

            const currency = getValue(transaction, "currency") || "INR";

            const date = getValue(
                transaction,
                "transactionTime",
                "transaction_time",
                "createdAt",
                "created_at"
            );

            const score = getScore(transaction);

            const riskLevel = getRiskLevel(transaction);

            const status = getStatus(transaction);

            const riskScore = score === null
                ? "--"
                : `${(score * 100).toFixed(1)}%`;

            const detailsLink = id !== null
                ? `transactions-details.html?id=${encodeURIComponent(id)}`
                : "#";

            return `
                <tr>
                    <td>
                        ${escapeHTML(reference || id || "--")}
                    </td>

                    <td>${escapeHTML(merchant)}</td>

                    <td>
                        ${escapeHTML(formatAmount(amount, currency))}
                    </td>

                    <td>${formatDate(date)}</td>

                    <td>${escapeHTML(riskScore)}</td>

                    <td>${getRiskBadge(riskLevel)}</td>

                    <td>${getStatusBadge(status)}</td>

                    <td>
                        ${
                            id !== null
                                ? `<a href="${escapeHTML(detailsLink)}"
                                      class="btn btn-secondary">
                                      View Details
                                   </a>`
                                : "--"
                        }
                    </td>
                </tr>
            `;

        }).join("");

        updatePaginationInfo();
        updatePaginationButtons();
    }

    // ---------- Pagination ----------

    function updatePaginationInfo() {
        if (!elements.paginationInfo) return;

        const total = filteredTransactions.length;

        const start = total === 0
            ? 0
            : (currentPage - 1) * PAGE_SIZE + 1;

        const end = Math.min(
            currentPage * PAGE_SIZE,
            total
        );

        elements.paginationInfo.textContent =
            `Showing ${start}-${end} of ${total} transactions`;
    }

    function updatePaginationButtons() {
        const totalPages = Math.ceil(
            filteredTransactions.length / PAGE_SIZE
        );

        if (elements.previous) {
            elements.previous.disabled =
                currentPage <= 1 || isLoading;
        }

        if (elements.next) {
            elements.next.disabled =
                currentPage >= totalPages || isLoading;
        }
    }

    function previousPage() {
        if (currentPage > 1) {
            currentPage--;
            renderTransactions();
        }
    }

    function nextPage() {
        const totalPages = Math.ceil(
            filteredTransactions.length / PAGE_SIZE
        );

        if (currentPage < totalPages) {
            currentPage++;
            renderTransactions();
        }
    }

    // ---------- Clear Filters ----------

    function clearFilters() {
        if (elements.search) elements.search.value = "";
        if (elements.riskFilter) elements.riskFilter.value = "";
        if (elements.statusFilter) elements.statusFilter.value = "";
        if (elements.currencyFilter) elements.currencyFilter.value = "";

        applyFilters();
    }

    // ---------- Event Listeners ----------

    function initializeEvents() {

        elements.search?.addEventListener("input", applyFilters);

        elements.riskFilter?.addEventListener("change", applyFilters);

        elements.statusFilter?.addEventListener("change", applyFilters);

        elements.currencyFilter?.addEventListener("change", applyFilters);

        elements.refresh?.addEventListener("click", loadTransactions);

        elements.clearFilters?.addEventListener("click", clearFilters);

        elements.previous?.addEventListener("click", previousPage);

        elements.next?.addEventListener("click", nextPage);
    }

    // ---------- Initialize ----------

    function init() {
        initializeEvents();

        if (!window.CreditGuardAPI) {
            showMessage(
                "API service is not loaded. Check api.js script path.",
                "error"
            );
            return;
        }

        loadTransactions();
    }

    document.addEventListener("DOMContentLoaded", init);

})();

