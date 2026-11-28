
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
    let currentPage = 0;
    let totalPages = 0;
    let totalElements = 0;
    let isLoading = false;
    let searchTimer = null;

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
                currency,
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
            getValue(transaction, "riskScore", "risk_score")
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

        return suppliedLevel
            ? String(suppliedLevel).toUpperCase()
            : "UNKNOWN";
    }

    function getTransactionId(transaction) {
        return getValue(transaction, "id", "transactionId", "transaction_id");
    }

    function getStatus(transaction) {
        return String(
            getValue(transaction, "status", "transactionStatus") || "UNKNOWN"
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

    // ---------- Query Parameters ----------

    function buildQueryString() {
        const params = new URLSearchParams();

        params.set("page", String(currentPage));
        params.set("size", String(PAGE_SIZE));

        const search = elements.search?.value.trim();
        const risk = elements.riskFilter?.value;
        const status = elements.statusFilter?.value;
        const currency = elements.currencyFilter?.value;

        if (search) params.set("search", search);
        if (risk) params.set("riskLevel", risk);
        if (status) params.set("status", status);
        if (currency) params.set("currency", currency);

        return params.toString();
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

        updatePaginationButtons();

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
            const query = buildQueryString();

            const response = await CreditGuardAPI.get(
                `${API_ENDPOINT}?${query}`
            );

            if (!response || !Array.isArray(response.data)) {
                throw new Error("Unexpected transaction API response format.");
            }

            transactions = response.data;
            currentPage = Number(response.page ?? currentPage);
            totalPages = Number(response.totalPages ?? 0);
            totalElements = Number(response.totalElements ?? 0);

            updateStatistics(response.summary);
            renderTransactions();

        } catch (error) {
            console.error("Transaction loading error:", error);

            transactions = [];
            totalPages = 0;
            totalElements = 0;

            updateStatistics(null);

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

            updatePaginationButtons();
        }
    }

    // ---------- Statistics ----------

    function updateStatistics(summary) {
        const values = summary || {};

        if (elements.total) {
            elements.total.textContent =
                Number(values.totalTransactions || 0).toLocaleString("en-IN");
        }

        if (elements.fraud) {
            elements.fraud.textContent =
                Number(values.fraudTransactions || 0).toLocaleString("en-IN");
        }

        if (elements.lowRisk) {
            elements.lowRisk.textContent =
                Number(values.lowRiskTransactions || 0).toLocaleString("en-IN");
        }

        if (elements.highRisk) {
            elements.highRisk.textContent =
                Number(values.highRiskTransactions || 0).toLocaleString("en-IN");
        }
    }

    // ---------- Risk Badge ----------

    function getRiskBadge(level) {
        const classes = {
            LOW: "badge-success",
            MEDIUM: "badge-warning",
            HIGH: "badge-danger",
            UNKNOWN: "badge-neutral"
        };

        return `
            <span class="badge ${classes[level] || "badge-neutral"}">
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

        return `
            <span class="badge ${classes[status] || "badge-neutral"}">
                ${escapeHTML(status)}
            </span>
        `;
    }

    // ---------- Render Table ----------

    function renderTransactions() {
        if (!elements.tableBody) return;

        if (transactions.length === 0) {
            elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="8" class="empty-state">
                        No transactions found.
                    </td>
                </tr>
            `;

            if (elements.paginationInfo) {
                elements.paginationInfo.textContent =
                    "Showing 0 transactions";
            }

            updatePaginationButtons();
            return;
        }

        elements.tableBody.innerHTML = transactions.map(transaction => {
            const id = getTransactionId(transaction);

            const reference = getValue(
                transaction,
                "transactionReference",
                "externalReference",
                "external_reference"
            );

            const merchant = getValue(
                transaction,
                "merchantName",
                "merchant",
                "merchant_name"
            ) || "--";

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
                    <td>${escapeHTML(reference || id || "--")}</td>
                    <td>${escapeHTML(merchant)}</td>
                    <td>${escapeHTML(formatAmount(amount, currency))}</td>
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

        const start = totalElements === 0
            ? 0
            : currentPage * PAGE_SIZE + 1;

        const end = Math.min(
            (currentPage + 1) * PAGE_SIZE,
            totalElements
        );

        elements.paginationInfo.textContent =
            `Showing ${start}-${end} of ${totalElements} transactions`;
    }

    function updatePaginationButtons() {
        if (elements.previous) {
            elements.previous.disabled = currentPage <= 0 || isLoading;
        }

        if (elements.next) {
            elements.next.disabled =
                currentPage >= totalPages - 1 || totalPages === 0 || isLoading;
        }
    }

    function previousPage() {
        if (currentPage > 0 && !isLoading) {
            currentPage--;
            loadTransactions();
        }
    }

    function nextPage() {
        if (currentPage < totalPages - 1 && !isLoading) {
            currentPage++;
            loadTransactions();
        }
    }

    // ---------- Search and Filters ----------

    function applyFilters() {
        if (isLoading) return;

        currentPage = 0;
        loadTransactions();
    }

    // ---------- Clear Filters ----------

    function clearFilters() {
        if (elements.search) elements.search.value = "";
        if (elements.riskFilter) elements.riskFilter.value = "";
        if (elements.statusFilter) elements.statusFilter.value = "";
        if (elements.currencyFilter) elements.currencyFilter.value = "";

        currentPage = 0;
        loadTransactions();
    }

    // ---------- Event Listeners ----------

    function initializeEvents() {
        elements.search?.addEventListener("input", () => {
            clearTimeout(searchTimer);

            searchTimer = setTimeout(() => {
                applyFilters();
            }, 300);
        });

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
