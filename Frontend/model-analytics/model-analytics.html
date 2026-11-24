/* ==========================================
   CREDITGUARD - INVESTIGATIONS
   File: Frontend/investigations/investigations.js
========================================== */

document.addEventListener("DOMContentLoaded", () => {
    const API_URL = "/api/v1/investigations";

    // DOM Elements
    const tableBody = document.getElementById(
        "investigationsTableBody"
    );

    const totalCases = document.getElementById("totalCases");
    const openCases = document.getElementById("openCases");
    const progressCases = document.getElementById("progressCases");
    const closedCases = document.getElementById("closedCases");

    const searchInput = document.getElementById("searchInput");
    const statusFilter = document.getElementById("statusFilter");
    const priorityFilter = document.getElementById("priorityFilter");
    const riskFilter = document.getElementById("riskFilter");

    const refreshButton = document.getElementById("refreshButton");
    const previousButton = document.getElementById("previousPage");
    const nextButton = document.getElementById("nextPage");

    const paginationInfo = document.getElementById("paginationInfo");
    const pageMessage = document.getElementById("pageMessage");

    // State
    let investigations = [];
    let currentPage = 1;
    const pageSize = 10;

    // ======================================
    // SAFE HTML OUTPUT
    // ======================================

    function escapeHTML(value) {
        return String(value ?? "").replace(
            /[&<>"']/g,
            character => ({
                "&": "&amp;",
                "<": "&lt;",
                ">": "&gt;",
                '"': "&quot;",
                "'": "&#039;"
            })[character]
        );
    }

    // ======================================
    // NORMALIZE API DATA
    // ======================================

    function normalizeCase(item) {
        return {
            id: item.id ?? item.investigationId ?? "",
            caseId: item.caseId ?? item.id ?? "",
            transactionId:
                item.transactionId ??
                item.transaction?.id ??
                "—",

            amount:
                item.amount ??
                item.transaction?.amount ??
                null,

            currency:
                item.currency ??
                item.transaction?.currency ??
                "INR",

            riskScore:
                item.riskScore ??
                item.prediction?.riskScore ??
                item.prediction?.risk_score ??
                null,

            riskLevel:
                item.riskLevel ??
                item.prediction?.riskLevel ??
                item.prediction?.risk_level ??
                "UNKNOWN",

            priority: item.priority ?? "MEDIUM",

            assignedTo:
                item.assignedTo?.name ??
                item.analyst?.name ??
                item.assignedTo ??
                "Unassigned",

            status: item.status ?? "OPEN",

            createdAt:
                item.createdAt ??
                item.assignedAt ??
                item.created_at ??
                null
        };
    }

    // ======================================
    // API REQUEST
    // ======================================

    async function fetchInvestigations() {
        try {
            showMessage("Loading investigations...", "");

            tableBody.innerHTML = `
                <tr>
                    <td colspan="9" class="empty-state">
                        Loading investigation records...
                    </td>
                </tr>
            `;

            const response = await fetch(API_URL, {
                method: "GET",
                credentials: "include",
                headers: {
                    "Accept": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error(
                    `Unable to load investigations. HTTP ${response.status}`
                );
            }

            const data = await response.json();

            // Supports either a direct array or common wrapper.
            const records = Array.isArray(data)
                ? data
                : data.content ??
                  data.investigations ??
                  data.data ??
                  data.records;

            if (!Array.isArray(records)) {
                throw new Error(
                    "Unexpected investigation API response format."
                );
            }

            investigations = records.map(normalizeCase);

            currentPage = 1;

            updateStatistics();
            applyFilters();

            showMessage(
                `${investigations.length} investigation records loaded.`,
                "success"
            );

        } catch (error) {
            console.error("Investigation API Error:", error);

            investigations = [];

            updateStatistics();

            tableBody.innerHTML = `
                <tr>
                    <td colspan="9" class="empty-state">
                        ${escapeHTML(error.message)}
                    </td>
                </tr>
            `;

            paginationInfo.textContent = "No records loaded";

            previousButton.disabled = true;
            nextButton.disabled = true;

            showMessage(error.message, "error");
        }
    }

    // ======================================
    // STATISTICS
    // ======================================

    function updateStatistics() {
        totalCases.textContent = investigations.length;

        openCases.textContent = investigations.filter(
            item => item.status.toUpperCase() === "OPEN"
        ).length;

        progressCases.textContent = investigations.filter(
            item =>
                item.status.toUpperCase() === "IN_PROGRESS" ||
                item.status.toUpperCase() === "UNDER_REVIEW"
        ).length;

        closedCases.textContent = investigations.filter(
            item => item.status.toUpperCase() === "CLOSED"
        ).length;
    }

    // ======================================
    // FILTERS
    // ======================================

    function applyFilters() {
        const searchValue = searchInput.value
            .trim()
            .toLowerCase();

        const selectedStatus = statusFilter.value.toUpperCase();
        const selectedPriority = priorityFilter.value.toUpperCase();
        const selectedRisk = riskFilter.value.toUpperCase();

        let filtered = investigations.filter(item => {

            const matchesSearch =
                String(item.caseId).toLowerCase().includes(searchValue) ||
                String(item.transactionId).toLowerCase().includes(searchValue);

            const matchesStatus =
                !selectedStatus ||
                item.status.toUpperCase() === selectedStatus;

            const matchesPriority =
                !selectedPriority ||
                item.priority.toUpperCase() === selectedPriority;

            const matchesRisk =
                !selectedRisk ||
                item.riskLevel.toUpperCase() === selectedRisk;

            return (
                matchesSearch &&
                matchesStatus &&
                matchesPriority &&
                matchesRisk
            );
        });

        renderTable(filtered);
    }

    // ======================================
    // STATUS CSS CLASS
    // ======================================

    function getStatusClass(status) {
        switch (status.toUpperCase()) {
            case "OPEN":
                return "status-open";

            case "IN_PROGRESS":
                return "status-progress";

            case "UNDER_REVIEW":
                return "status-review";

            case "CLOSED":
                return "status-closed";

            default:
                return "";
        }
    }

    function formatStatus(status) {
        return status
            .replaceAll("_", " ")
            .toLowerCase()
            .replace(/\b\w/g, letter => letter.toUpperCase());
    }

    // ======================================
    // PRIORITY CSS CLASS
    // ======================================

    function getPriorityClass(priority) {
        switch (priority.toUpperCase()) {
            case "HIGH":
                return "priority-high";

            case "MEDIUM":
                return "priority-medium";

            case "LOW":
                return "priority-low";

            default:
                return "";
        }
    }

    // ======================================
    // RISK CSS CLASS
    // ======================================

    function getRiskClass(risk) {
        switch (risk.toUpperCase()) {
            case "HIGH":
                return "risk-high";

            case "MEDIUM":
                return "risk-medium";

            case "LOW":
                return "risk-low";

            default:
                return "";
        }
    }

    // ======================================
    // FORMAT AMOUNT
    // ======================================

    function formatAmount(amount, currency) {
        if (amount === null || amount === undefined) {
            return "—";
        }

        const number = Number(amount);

        if (!Number.isFinite(number)) {
            return "—";
        }

        try {
            return new Intl.NumberFormat("en-IN", {
                style: "currency",
                currency: currency || "INR"
            }).format(number);
        } catch {
            return `${currency || "INR"} ${number.toFixed(2)}`;
        }
    }

    // ======================================
    // FORMAT DATE
    // ======================================

    function formatDate(dateValue) {
        if (!dateValue) {
            return "—";
        }

        const date = new Date(dateValue);

        if (Number.isNaN(date.getTime())) {
            return "—";
        }

        return date.toLocaleDateString("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric"
        });
    }

    // ======================================
    // RENDER TABLE
    // ======================================

    function renderTable(filteredCases) {

        const totalPages = Math.max(
            1,
            Math.ceil(filteredCases.length / pageSize)
        );

        currentPage = Math.min(currentPage, totalPages);

        const startIndex = (currentPage - 1) * pageSize;

        const pageRecords = filteredCases.slice(
            startIndex,
            startIndex + pageSize
        );

        if (pageRecords.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="9" class="empty-state">
                        No investigation records found.
                    </td>
                </tr>
            `;
        } else {

            tableBody.innerHTML = pageRecords.map(item => {

                const riskScore = Number(item.riskScore);

                // SRS score range is 0 to 1.
                const scoreText = Number.isFinite(riskScore)
                    ? (riskScore <= 1
                        ? `${(riskScore * 100).toFixed(2)}%`
                        : `${riskScore.toFixed(2)}%`)
                    : "—";

                return `
                    <tr>
                        <td>
                            ${escapeHTML(item.caseId)}
                        </td>

                        <td>
                            ${escapeHTML(item.transactionId)}
                        </td>

                        <td>
                            ${escapeHTML(
                                formatAmount(item.amount, item.currency)
                            )}
                        </td>

                        <td>
                            <span class="${getRiskClass(item.riskLevel)}">
                                ${escapeHTML(scoreText)}
                            </span>
                        </td>

                        <td>
                            <span class="table-priority ${getPriorityClass(item.priority)}">
                                ${escapeHTML(item.priority)}
                            </span>
                        </td>

                        <td>
                            ${escapeHTML(item.assignedTo)}
                        </td>

                        <td>
                            <span class="table-status ${getStatusClass(item.status)}">
                                ${escapeHTML(formatStatus(item.status))}
                            </span>
                        </td>

                        <td>
                            ${escapeHTML(formatDate(item.createdAt))}
                        </td>

                        <td>
                            <a
                                class="case-link"
                                href="investigations-details.html?id=${encodeURIComponent(item.id)}"
                            >
                                View Details
                            </a>
                        </td>
                    </tr>
                `;
            }).join("");
        }

        // Pagination
        const startRecord = filteredCases.length
            ? startIndex + 1
            : 0;

        const endRecord = Math.min(
            startIndex + pageSize,
            filteredCases.length
        );

        paginationInfo.textContent =
            `Showing ${startRecord}-${endRecord} of ${filteredCases.length} records`;

        previousButton.disabled = currentPage <= 1;
        nextButton.disabled = currentPage >= totalPages;
    }

    // ======================================
    // MESSAGE
    // ======================================

    function showMessage(message, type) {
        pageMessage.textContent = message;
        pageMessage.className = "message";

        if (type) {
            pageMessage.classList.add(type);
        }
    }

    // ======================================
    // EVENT LISTENERS
    // ======================================

    searchInput.addEventListener("input", () => {
        currentPage = 1;
        applyFilters();
    });

    statusFilter.addEventListener("change", () => {
        currentPage = 1;
        applyFilters();
    });

    priorityFilter.addEventListener("change", () => {
        currentPage = 1;
        applyFilters();
    });

    riskFilter.addEventListener("change", () => {
        currentPage = 1;
        applyFilters();
    });

    refreshButton.addEventListener("click", () => {
        fetchInvestigations();
    });

    previousButton.addEventListener("click", () => {
        if (currentPage > 1) {
            currentPage--;
            applyFilters();
        }
    });

    nextButton.addEventListener("click", () => {
        currentPage++;
        applyFilters();
    });

    // Initial Load
    fetchInvestigations();
});
