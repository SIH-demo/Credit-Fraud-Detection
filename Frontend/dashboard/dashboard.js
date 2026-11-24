// ==========================================
// CreditGuard - Dashboard JavaScript
// File: Frontend/dashboard/dashboard.js
// ==========================================

document.addEventListener("DOMContentLoaded", function () {

    // --------------------------------------
    // API Configuration
    // --------------------------------------

    const API_BASE = "/api/v1";

    const SUMMARY_API = `${API_BASE}/dashboard/summary`;
    const MODELS_API = `${API_BASE}/models`;

    // --------------------------------------
    // DOM Elements
    // --------------------------------------

    const totalTransactions =
        document.getElementById("totalTransactions");

    const highRiskTransactions =
        document.getElementById("highRiskTransactions");

    const mediumRiskTransactions =
        document.getElementById("mediumRiskTransactions");

    const lowRiskTransactions =
        document.getElementById("lowRiskTransactions");

    const recentTransactionsBody =
        document.getElementById("recentTransactionsBody");

    const dashboardMessage =
        document.getElementById("dashboardMessage");

    const refreshButton =
        document.getElementById("refreshButton");

    const dateRange =
        document.getElementById("dateRange");

    const backendStatus =
        document.getElementById("backendStatus");

    const mlStatus =
        document.getElementById("mlStatus");

    const modelVersion =
        document.getElementById("modelVersion");

    const modelStatus =
        document.getElementById("modelStatus");

    let trendChart = null;
    let riskChart = null;

    // --------------------------------------
    // Utility Functions
    // --------------------------------------

    function formatNumber(value) {
        return Number(value ?? 0).toLocaleString("en-IN");
    }

    function formatAmount(value) {
        return "₹" + Number(value ?? 0).toLocaleString("en-IN", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
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
    // Current Date
    // --------------------------------------

    function displayCurrentDate() {
        const currentDate = document.getElementById("currentDate");

        if (currentDate) {
            currentDate.textContent =
                new Date().toLocaleDateString("en-IN", {
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

        const response = await fetch(SUMMARY_API, {
            method: "GET",
            credentials: "include",
            headers: {
                "Accept": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(
                `Dashboard API error: ${response.status}`
            );
        }

        return await response.json();
    }

    // --------------------------------------
    // Update Statistics
    // --------------------------------------

    function updateStatistics(data) {

        const summary = data.summary || data;

        totalTransactions.textContent = formatNumber(
            summary.totalTransactions ??
            summary.totalTransactionCount
        );

        highRiskTransactions.textContent = formatNumber(
            summary.highRiskTransactions ??
            summary.highRiskCount
        );

        mediumRiskTransactions.textContent = formatNumber(
            summary.mediumRiskTransactions ??
            summary.mediumRiskCount
        );

        lowRiskTransactions.textContent = formatNumber(
            summary.lowRiskTransactions ??
            summary.lowRiskCount
        );
    }

    // --------------------------------------
    // Transaction Trend Chart
    // --------------------------------------

    function renderTransactionTrend(data) {

        const canvas = document.getElementById(
            "transactionTrendChart"
        );

        const emptyMessage = document.getElementById("trendEmpty");

        const trendData = getArray(
            data.transactionTrend ||
            data.trend ||
            data.dailyTransactions
        );

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

        const labels = trendData.map(item =>
            item.date || item.label || item.period || ""
        );

        const values = trendData.map(item =>
            Number(
                item.count ??
                item.totalTransactions ??
                item.transactions ??
                0
            )
        );

        if (trendChart) {
            trendChart.destroy();
        }

        trendChart = new Chart(canvas, {
            type: "line",

            data: {
                labels: labels,

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

        const canvas = document.getElementById(
            "riskDistributionChart"
        );

        const emptyMessage = document.getElementById("riskEmpty");

        const low = Number(
            data.lowRiskTransactions ?? data.lowRiskCount ?? 0
        );

        const medium = Number(
            data.mediumRiskTransactions ?? data.mediumRiskCount ?? 0
        );

        const high = Number(
            data.highRiskTransactions ?? data.highRiskCount ?? 0
        );

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
                labels: [
                    "Low Risk",
                    "Medium Risk",
                    "High Risk"
                ],

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
            data.recentSuspiciousTransactions ||
            data.recentTransactions ||
            data.transactions
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

            const transactionId =
                transaction.transactionId ??
                transaction.id ??
                "N/A";

            const date =
                transaction.transactionDate ??
                transaction.createdAt ??
                transaction.timestamp;

            const amount = transaction.amount ?? 0;

            const score =
                transaction.riskScore ??
                transaction.score ??
                0;

            const risk = String(
                transaction.riskLevel ??
                transaction.risk ??
                "Unknown"
            ).toLowerCase();

            const status =
                transaction.status ??
                "Pending Review";

            const values = [
                transactionId,
                formatDate(date),
                formatAmount(amount),
                `${Number(score).toFixed(2)}%`,
                risk,
                status
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

            setStatus(
                backendStatus,
                "Connected",
                "status-online"
            );

            hideMessage();

        } catch (error) {

            console.error("Dashboard Error:", error);

            showMessage(
                "Unable to load dashboard data. Please check the backend connection.",
                "error"
            );

            setStatus(
                backendStatus,
                "Unavailable",
                "status-offline"
            );

        } finally {

            refreshButton.disabled = false;
            refreshButton.textContent = "↻ Refresh";
        }
    }

    // --------------------------------------
    // Fetch Model Information
    // --------------------------------------

    async function loadModelInformation() {

        try {

            const response = await fetch(MODELS_API, {
                method: "GET",
                credentials: "include",
                headers: {
                    "Accept": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error("Unable to fetch model information.");
            }

            const data = await response.json();

            const models = Array.isArray(data)
                ? data
                : data.models || [];

            const activeModel =
                models.find(model =>
                    model.active === true ||
                    model.isActive === true ||
                    String(model.status || "").toLowerCase() === "active"
                ) || models[0];

            if (!activeModel) {

                modelVersion.textContent = "Not available";
                modelStatus.textContent = "No model information";

                setStatus(
                    mlStatus,
                    "Not reported",
                    "status-unknown"
                );

                return;
            }

            modelVersion.textContent =
                activeModel.version ??
                activeModel.modelVersion ??
                activeModel.name ??
                "N/A";

            modelStatus.textContent =
                activeModel.status ??
                (activeModel.active ? "Active" : "Available");

            setStatus(
                mlStatus,
                "Model information available",
                "status-online"
            );

        } catch (error) {

            console.error("Model API Error:", error);

            modelVersion.textContent = "Unavailable";
            modelStatus.textContent = "Unavailable";

            setStatus(
                mlStatus,
                "Not reported",
                "status-unknown"
            );
        }
    }

    // --------------------------------------
    // Refresh Dashboard
    // --------------------------------------

    refreshButton.addEventListener("click", function () {
        loadDashboard();
        loadModelInformation();
    });

    // --------------------------------------
    // Date Range Selection
    // --------------------------------------

    dateRange.addEventListener("change", function () {

        showMessage(
            "The selected range is " + this.options[this.selectedIndex].text +
            ". Date-range filtering requires backend API support.",
            "info"
        );

    });

    // --------------------------------------
    // Initial Dashboard Load
    // --------------------------------------

    displayCurrentDate();

    loadDashboard();
    loadModelInformation();

});
