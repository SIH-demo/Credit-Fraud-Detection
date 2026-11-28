
// ==========================================
// CreditGuard - Dashboard JavaScript
// File: Frontend/dashboard/dashboard.js
// ==========================================

document.addEventListener("DOMContentLoaded", function () {

    // --------------------------------------
    // API Configuration
    // --------------------------------------

    const API_BASE = "/api/v1";
    const SUMMARY_API = `${API_BASE}/analytics/dashboard/summary`;
    const ML_HEALTH_API = `${API_BASE}/health/ml`;

    // --------------------------------------
    // DOM Elements
    // --------------------------------------

    const totalTransactions = document.getElementById("totalTransactions");
    const fraudPercentage = document.getElementById("fraudPercentage");
    const highRiskTransactions = document.getElementById("highRiskTransactions");
    const mediumRiskTransactions = document.getElementById("mediumRiskTransactions");
    const lowRiskTransactions = document.getElementById("lowRiskTransactions");

    const recentTransactionsBody = document.getElementById("recentTransactionsBody");
    const currencySummaryBody = document.getElementById("currencySummaryBody");

    const dashboardMessage = document.getElementById("dashboardMessage");
    const refreshButton = document.getElementById("refreshButton");
    const dateRange = document.getElementById("dateRange");

    const backendStatus = document.getElementById("backendStatus");
    const mlStatus = document.getElementById("mlStatus");
    const modelVersion = document.getElementById("modelVersion");
    const modelStatus = document.getElementById("modelStatus");

    let trendChart = null;
    let riskChart = null;

    // --------------------------------------
    // Utility Functions
    // --------------------------------------

    function formatNumber(value) {
        return Number(value ?? 0).toLocaleString("en-IN");
    }

    function formatAmount(value, currency = "INR") {
        const amount = Number(value ?? 0);

        return `${currency} ${amount.toLocaleString("en-IN", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        })}`;
    }

    function formatDate(value) {
        if (!value) return "N/A";

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return String(value);
        }

        return date.toLocaleString("en-IN", {
            dateStyle: "medium",
            timeStyle: "short"
        });
    }

    function showMessage(message, type = "info") {
        dashboardMessage.hidden = false;
        dashboardMessage.textContent = message;

        const styles = {
            info: ["#eff6ff", "#1e40af"],
            success: ["#dcfce7", "#166534"],
            error: ["#fee2e2", "#991b1b"]
        };

        const selectedStyle = styles[type] || styles.info;

        dashboardMessage.style.backgroundColor = selectedStyle[0];
        dashboardMessage.style.color = selectedStyle[1];
    }

    function hideMessage() {
        dashboardMessage.hidden = true;
        dashboardMessage.textContent = "";
    }

    function setStatus(element, text, className) {
        element.textContent = text;
        element.className = className;
    }

    function getArray(value) {
        return Array.isArray(value) ? value : [];
    }

    // --------------------------------------
    // Date Range
    // --------------------------------------

    function getDateFilters() {
        const selectedRange = dateRange.value;
        const now = new Date();

        if (selectedRange === "all") {
            return {};
        }

        let startDate = new Date(now);

        if (selectedRange === "today") {
            startDate.setHours(0, 0, 0, 0);
        } else {
            const days = {
                "7d": 7,
                "30d": 30,
                "90d": 90
            };

            startDate.setDate(
                startDate.getDate() - (days[selectedRange] || 7)
            );
        }

        return {
            start_time: startDate.toISOString(),
            end_time: now.toISOString()
        };
    }

    // --------------------------------------
    // Current Date
    // --------------------------------------

    function displayCurrentDate() {
        const currentDate = document.getElementById("currentDate");

        if (currentDate) {
            currentDate.textContent = new Date().toLocaleDateString("en-IN", {
                day: "numeric",
                month: "short",
                year: "numeric"
            });
        }
    }

    // --------------------------------------
    // Fetch Dashboard Summary
    // --------------------------------------

   
async function fetchDashboardSummary() {
    const responseData = await CreditGuardAPI.post(
        "analytics/dashboard/summary",
        getDateFilters()
    );

    if (!responseData.data) {
        throw new Error("Invalid dashboard API response.");
    }

    return responseData.data;
}


    // --------------------------------------
    // Update Statistics
    // --------------------------------------

    function updateStatistics(data) {
        const total = Number(data.total_transactions ?? 0);
        const fraudCount = Number(data.predicted_fraud_transactions ?? 0);

        const risk = data.risk_distribution || {};

        totalTransactions.textContent = formatNumber(total);

        const percentage = total > 0
            ? (fraudCount / total) * 100
            : 0;

        fraudPercentage.textContent = `${percentage.toFixed(2)}%`;

        highRiskTransactions.textContent = formatNumber(risk.HIGH ?? 0);
        mediumRiskTransactions.textContent = formatNumber(risk.MEDIUM ?? 0);
        lowRiskTransactions.textContent = formatNumber(risk.LOW ?? 0);
    }

    // --------------------------------------
    // Transaction Trend Chart
    // --------------------------------------

    function renderTransactionTrend(data) {
        const canvas = document.getElementById("transactionTrendChart");
        const emptyMessage = document.getElementById("trendEmpty");

        const trendData = getArray(data.daily_trend);

        if (!trendData.length) {
            if (trendChart) {
                trendChart.destroy();
                trendChart = null;
            }

            canvas.hidden = true;
            emptyMessage.hidden = false;
            return;
        }

        canvas.hidden = false;
        emptyMessage.hidden = true;

        const labels = trendData.map(item => item.date || "");

        const values = trendData.map(item =>
            Number(item.transaction_count ?? 0)
        );

        if (trendChart) {
            trendChart.destroy();
        }

        trendChart = new Chart(canvas, {
            type: "line",

            data: {
                labels,

                datasets: [{
                    label: "Transactions",
                    data: values,
                    borderColor: "#2563eb",
                    backgroundColor: "rgba(37, 99, 235, 0.12)",
                    fill: true,
                    tension: 0.35,
                    borderWidth: 2,
                    pointRadius: 3,
                    pointBackgroundColor: "#2563eb"
                }]
            },

            options: {
                responsive: true,
                maintainAspectRatio: false,

                plugins: {
                    legend: {
                        display: false
                    }
                },

                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    },

                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    // --------------------------------------
    // Risk Distribution Chart
    // --------------------------------------

    function renderRiskDistribution(data) {
        const canvas = document.getElementById("riskDistributionChart");
        const emptyMessage = document.getElementById("riskEmpty");

        const distribution = data.risk_distribution || {};

        const low = Number(distribution.LOW ?? 0);
        const medium = Number(distribution.MEDIUM ?? 0);
        const high = Number(distribution.HIGH ?? 0);

        if (low + medium + high === 0) {
            if (riskChart) {
                riskChart.destroy();
                riskChart = null;
            }

            canvas.hidden = true;
            emptyMessage.hidden = false;
            return;
        }

        canvas.hidden = false;
        emptyMessage.hidden = true;

        if (riskChart) {
            riskChart.destroy();
        }

        riskChart = new Chart(canvas, {
            type: "doughnut",

            data: {
                labels: ["Low Risk", "Medium Risk", "High Risk"],

                datasets: [{
                    data: [low, medium, high],

                    backgroundColor: [
                        "#16a34a",
                        "#f59e0b",
                        "#dc2626"
                    ],

                    borderWidth: 0
                }]
            },

            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: "68%",

                plugins: {
                    legend: {
                        display: false
                    }
                }
            }
        });
    }

    // --------------------------------------
    // Recent Suspicious Transactions
    // --------------------------------------

    function renderRecentTransactions(data) {
        recentTransactionsBody.innerHTML = "";

        const transactions = getArray(
            data.recent_suspicious_transactions
        );

        if (!transactions.length) {
            const row = document.createElement("tr");
            const cell = document.createElement("td");

            cell.colSpan = 6;
            cell.className = "empty-state";
            cell.textContent = "No recent suspicious transactions found.";

            row.appendChild(cell);
            recentTransactionsBody.appendChild(row);
            return;
        }

        transactions.forEach(transaction => {
            const row = document.createElement("tr");

            const riskScore = Number(transaction.risk_score ?? 0) * 100;
            const risk = String(transaction.risk_level ?? "UNKNOWN").toLowerCase();

            const values = [
                transaction.transaction_id ?? "N/A",
                formatDate(transaction.transaction_time),
                formatAmount(transaction.amount, transaction.currency),
                `${riskScore.toFixed(2)}%`,
                risk.toUpperCase(),
                Number(transaction.predicted_fraud) === 1
                    ? "Predicted Fraud"
                    : "High Risk"
            ];

            values.forEach((value, index) => {
                const cell = document.createElement("td");

                if (index === 4) {
                    const badge = document.createElement("span");

                    badge.textContent = value;
                    badge.className = "risk-badge";

                    if (risk === "high") {
                        badge.style.color = "#dc2626";
                    } else if (risk === "medium") {
                        badge.style.color = "#d97706";
                    } else {
                        badge.style.color = "#16a34a";
                    }

                    cell.appendChild(badge);
                } else {
                    cell.textContent = value;
                }

                row.appendChild(cell);
            });

            recentTransactionsBody.appendChild(row);
        });
    }

    // --------------------------------------
    // Currency Summary
    // --------------------------------------

    function renderCurrencySummary(data) {
        currencySummaryBody.innerHTML = "";

        const summary = data.currency_summary || {};
        const currencies = Object.entries(summary);

        if (!currencies.length) {
            const row = document.createElement("tr");
            const cell = document.createElement("td");

            cell.colSpan = 4;
            cell.className = "empty-state";
            cell.textContent = "No currency summary available.";

            row.appendChild(cell);
            currencySummaryBody.appendChild(row);
            return;
        }

        currencies.forEach(([currency, details]) => {
            const row = document.createElement("tr");

            const values = [
                currency,
                formatNumber(details.transaction_count),
                formatAmount(details.total_amount, currency),
                formatAmount(details.average_transaction_amount, currency)
            ];

            values.forEach(value => {
                const cell = document.createElement("td");
                cell.textContent = value;
                row.appendChild(cell);
            });

            currencySummaryBody.appendChild(row);
        });
    }

    // --------------------------------------
    // Load Dashboard
    // --------------------------------------

    async function loadDashboard() {
        refreshButton.disabled = true;
        refreshButton.textContent = "Loading...";

        showMessage("Loading dashboard data...", "info");

        try {
            const data = await fetchDashboardSummary();

            updateStatistics(data);
            renderTransactionTrend(data);
            renderRiskDistribution(data);
            renderRecentTransactions(data);
            renderCurrencySummary(data);

            setStatus(backendStatus, "Connected", "status-online");

            hideMessage();

        } catch (error) {
            console.error("Dashboard Error:", error);

            showMessage(
                "Unable to load dashboard data. Please check the backend connection.",
                "error"
            );

            setStatus(backendStatus, "Unavailable", "status-offline");

        } finally {
            refreshButton.disabled = false;
            refreshButton.textContent = "↻ Refresh";
        }
    }

    // --------------------------------------
    // Fetch Model Information
    // --------------------------------------

    // --------------------------------------
// Fetch ML Service Health and Model Information
// --------------------------------------

async function loadModelInformation() {
    try {
        const data = await CreditGuardAPI.get("health/ml");

        const isUp = String(data.status).toUpperCase() === "UP";
        const isLoaded = data.model_loaded === true;

        // ML Service Status
        if (isUp) {
            setStatus(mlStatus, "Connected", "status-online");
        } else {
            setStatus(mlStatus, "Degraded", "status-offline");
        }

        // Model Version
        modelVersion.textContent =
            data.model_version ?? "Unavailable";

        // Model Loading Status
        modelStatus.textContent = isLoaded
            ? "Loaded"
            : "Not Loaded";

        // Show threshold if the dashboard has an element for it
        const modelThreshold =
            document.getElementById("modelThreshold");

        if (modelThreshold) {
            modelThreshold.textContent =
                data.prediction_threshold != null
                    ? `${(Number(data.prediction_threshold) * 100).toFixed(0)}%`
                    : "Unavailable";
        }

    } catch (error) {
        console.error("ML Health API Error:", error);

        modelVersion.textContent = "Unavailable";
        modelStatus.textContent = "Unavailable";

        setStatus(mlStatus, "Unavailable", "status-offline");
    }
}

    // --------------------------------------
    // Event Listeners
    // --------------------------------------

    refreshButton.addEventListener("click", function () {
        loadDashboard();
        loadModelInformation();
    });

    dateRange.addEventListener("change", function () {
        loadDashboard();
    });

    // --------------------------------------
    // Initial Dashboard Load
    // --------------------------------------

    displayCurrentDate();

    loadDashboard();
    loadModelInformation();

});
