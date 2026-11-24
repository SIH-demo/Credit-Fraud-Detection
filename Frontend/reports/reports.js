
"use strict";

/*
 * CreditGuard - Reports Module
 * File: Frontend/reports/reports.js
 *
 * Backend note:
 * The SRS does not define report-generation or report-history APIs.
 * This file currently manages the UI with in-memory sample records.
 * Replace the sample data and generateReport() logic when the
 * backend report API contract is available.
 */

document.addEventListener("DOMContentLoaded", () => {
    const PAGE_SIZE = 8;

    const elements = {
        message: document.getElementById("pageMessage"),

        reportForm: document.getElementById("reportForm"),
        reportType: document.getElementById("reportType"),
        startDate: document.getElementById("startDate"),
        endDate: document.getElementById("endDate"),
        reportFormat: document.getElementById("reportFormat"),
        reportName: document.getElementById("reportName"),

        generateButton: document.getElementById("generateReportButton"),
        clearButton: document.getElementById("clearReportButton"),

        search: document.getElementById("searchReport"),
        statusFilter: document.getElementById("statusFilter"),
        typeFilter: document.getElementById("typeFilter"),

        refreshButton: document.getElementById("refreshReportsButton"),
        tableBody: document.getElementById("reportsTableBody"),

        paginationInfo: document.getElementById("paginationInfo"),
        previousButton: document.getElementById("previousPage"),
        nextButton: document.getElementById("nextPage"),

        totalReports: document.getElementById("totalReports"),
        generatedReports: document.getElementById("generatedReports"),
        pendingReports: document.getElementById("pendingReports"),
        failedReports: document.getElementById("failedReports")
    };

    let currentPage = 1;

    // Temporary sample data. Replace with backend data later.
    let reports = [
        {
            id: "RPT-1001",
            name: "Monthly Transaction Report",
            type: "TRANSACTION_REPORT",
            format: "PDF",
            status: "COMPLETED",
            createdAt: new Date().toISOString(),
            downloadUrl: null
        },
        {
            id: "RPT-1002",
            name: "Fraud Detection Summary",
            type: "FRAUD_DETECTION_REPORT",
            format: "CSV",
            status: "PENDING",
            createdAt: new Date().toISOString(),
            downloadUrl: null
        }
    ];

    function showMessage(message, type = "info") {
        if (!elements.message) return;

        elements.message.textContent = message;
        elements.message.className = `page-message ${type}`;
        elements.message.hidden = false;
    }

    function clearMessage() {
        if (!elements.message) return;

        elements.message.textContent = "";
        elements.message.hidden = true;
        elements.message.className = "page-message";
    }

    function escapeHTML(value) {
        return String(value ?? "").replace(/[&<>"']/g, character => {
            const entities = {
                "&": "&amp;",
                "<": "&lt;",
                ">": "&gt;",
                '"': "&quot;",
                "'": "&#39;"
            };

            return entities[character];
        });
    }

    function formatLabel(value) {
        return String(value || "")
            .toLowerCase()
            .replace(/_/g, " ")
            .replace(/\b\w/g, character => character.toUpperCase());
    }

    function formatDate(value) {
        if (!value) return "—";

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) return "—";

        return date.toLocaleString("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    function updateStatistics() {
        const total = reports.length;
        const completed = reports.filter(
            report => report.status === "COMPLETED"
        ).length;

        const pending = reports.filter(
            report => report.status === "PENDING"
        ).length;

        const failed = reports.filter(
            report => report.status === "FAILED"
        ).length;

        if (elements.totalReports) {
            elements.totalReports.textContent = total;
        }

        if (elements.generatedReports) {
            elements.generatedReports.textContent = completed;
        }

        if (elements.pendingReports) {
            elements.pendingReports.textContent = pending;
        }

        if (elements.failedReports) {
            elements.failedReports.textContent = failed;
        }
    }

    function getFilteredReports() {
        const searchText = elements.search?.value
            .trim()
            .toLowerCase() || "";

        const selectedStatus = elements.statusFilter?.value || "";
        const selectedType = elements.typeFilter?.value || "";

        return reports.filter(report => {
            const matchesSearch =
                !searchText ||
                report.name.toLowerCase().includes(searchText) ||
                report.id.toLowerCase().includes(searchText);

            const matchesStatus =
                !selectedStatus ||
                report.status === selectedStatus;

            const matchesType =
                !selectedType ||
                report.type === selectedType;

            return matchesSearch && matchesStatus && matchesType;
        });
    }

    function getStatusClass(status) {
        switch (status) {
            case "COMPLETED":
                return "status-completed";

            case "PENDING":
                return "status-pending";

            case "FAILED":
                return "status-failed";

            default:
                return "status-unknown";
        }
    }

    function renderReports() {
        if (!elements.tableBody) return;

        const filteredReports = getFilteredReports();

        const totalPages = Math.max(
            1,
            Math.ceil(filteredReports.length / PAGE_SIZE)
        );

        currentPage = Math.min(currentPage, totalPages);

        const startIndex = (currentPage - 1) * PAGE_SIZE;

        const pageReports = filteredReports.slice(
            startIndex,
            startIndex + PAGE_SIZE
        );

        if (pageReports.length === 0) {
            elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="empty-state">
                        No reports found.
                    </td>
                </tr>
            `;
        } else {
            elements.tableBody.innerHTML = pageReports.map(report => `
                <tr>
                    <td>${escapeHTML(report.id)}</td>

                    <td>${escapeHTML(report.name)}</td>

                    <td>${escapeHTML(formatLabel(report.type))}</td>

                    <td>${escapeHTML(report.format)}</td>

                    <td>
                        <span class="status-badge ${getStatusClass(report.status)}">
                            ${escapeHTML(formatLabel(report.status))}
                        </span>
                    </td>

                    <td>${escapeHTML(formatDate(report.createdAt))}</td>

                    <td>
                        ${
                            report.status === "COMPLETED" &&
                            report.downloadUrl
                                ? `<a href="${escapeHTML(report.downloadUrl)}"
                                      class="download-link"
                                      target="_blank"
                                      rel="noopener noreferrer">
                                      Download
                                   </a>`
                                : "—"
                        }
                    </td>
                </tr>
            `).join("");
        }

        const from = filteredReports.length
            ? startIndex + 1
            : 0;

        const to = Math.min(
            startIndex + PAGE_SIZE,
            filteredReports.length
        );

        if (elements.paginationInfo) {
            elements.paginationInfo.textContent =
                `Showing ${from}-${to} of ${filteredReports.length} reports`;
        }

        if (elements.previousButton) {
            elements.previousButton.disabled = currentPage <= 1;
        }

        if (elements.nextButton) {
            elements.nextButton.disabled = currentPage >= totalPages;
        }

        updateStatistics();
    }

    function validateForm() {
        const type = elements.reportType?.value;
        const format = elements.reportFormat?.value;
        const startDate = elements.startDate?.value;
        const endDate = elements.endDate?.value;
        const name = elements.reportName?.value.trim();

        if (!type) {
            showMessage("Please select a report type.", "error");
            elements.reportType?.focus();
            return false;
        }

        if (!format) {
            showMessage("Please select a report format.", "error");
            elements.reportFormat?.focus();
            return false;
        }

        if (!name) {
            showMessage("Please enter a report name.", "error");
            elements.reportName?.focus();
            return false;
        }

        if (startDate && endDate && startDate > endDate) {
            showMessage(
                "Start date cannot be later than end date.",
                "error"
            );

            elements.startDate?.focus();
            return false;
        }

        return true;
    }

    function createReport() {
        const report = {
            id: `RPT-${Date.now()}`,
            name: elements.reportName.value.trim(),
            type: elements.reportType.value,
            format: elements.reportFormat.value,
            status: "PENDING",
            createdAt: new Date().toISOString(),
            downloadUrl: null
        };

        reports.unshift(report);

        currentPage = 1;
        renderReports();

        showMessage(
            "Report request added to the demo list. Connect the backend API to generate a real report.",
            "success"
        );
    }

    function resetForm() {
        elements.reportForm?.reset();
        clearMessage();
    }

    function handleFormSubmit(event) {
        event.preventDefault();
        clearMessage();

        if (!validateForm()) return;

        if (elements.generateButton) {
            elements.generateButton.disabled = true;
            elements.generateButton.textContent = "Processing...";
        }

        try {
            createReport();
        } finally {
            if (elements.generateButton) {
                elements.generateButton.disabled = false;
                elements.generateButton.textContent = "Generate Report";
            }
        }
    }

    function handleFilters() {
        currentPage = 1;
        renderReports();
    }

    function refreshReports() {
        // Demo mode: refresh the current in-memory list.
        currentPage = 1;
        renderReports();

        showMessage(
            "Report list refreshed. Data is currently stored only in this browser session.",
            "info"
        );
    }

    // Event listeners
    elements.reportForm?.addEventListener(
        "submit",
        handleFormSubmit
    );

    elements.clearButton?.addEventListener(
        "click",
        resetForm
    );

    elements.search?.addEventListener(
        "input",
        handleFilters
    );

    elements.statusFilter?.addEventListener(
        "change",
        handleFilters
    );

    elements.typeFilter?.addEventListener(
        "change",
        handleFilters
    );

    elements.refreshButton?.addEventListener(
        "click",
        refreshReports
    );

    elements.previousButton?.addEventListener("click", () => {
        if (currentPage > 1) {
            currentPage--;
            renderReports();
        }
    });

    elements.nextButton?.addEventListener("click", () => {
        const totalPages = Math.max(
            1,
            Math.ceil(getFilteredReports().length / PAGE_SIZE)
        );

        if (currentPage < totalPages) {
            currentPage++;
            renderReports();
        }
    });

    // Initial page rendering
    renderReports();
});
