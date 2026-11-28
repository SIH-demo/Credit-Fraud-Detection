
"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const elements = {
        message: document.getElementById("pageMessage"),
        refreshButton: document.getElementById("refreshReportsButton"),
        reportsTable: document.getElementById("reportsTableBody"),
        detailsSection: document.getElementById("batchDetailsSection"),
        selectedBatchId: document.getElementById("selectedBatchId"),
        statusMessage: document.getElementById("statusMessage"),
        downloadButton: document.getElementById("downloadBtn"),
        searchInput: document.getElementById("searchInput"),
        resultsTable: document.getElementById("resultsTableBody"),
        errorsSection: document.getElementById("batchErrorsSection"),
        errorsBody: document.getElementById("batchErrorsBody"),
        errorCount: document.getElementById("errorCount"),
        skippedSection: document.getElementById("batchSkippedSection"),
        skippedBody: document.getElementById("batchSkippedBody"),
        skippedRowsCount: document.getElementById("skippedRowsCount")
    };

    let currentBatchId = null;
    let batchResults = [];
    let refreshTimer = null;

    function showMessage(message, type = "info") {
        elements.message.textContent = message;
        elements.message.className = `page-message ${type}`;
        elements.message.hidden = false;
    }

    function clearMessage() {
        elements.message.textContent = "";
        elements.message.hidden = true;
        elements.message.className = "page-message";
    }

    function showStatus(message) {
        elements.statusMessage.textContent = message;
        elements.statusMessage.style.display = "block";
    }

    function formatNumber(value) {
        return Number(value || 0).toLocaleString("en-IN");
    }

    function formatPercentage(value) {
        return `${Number(value || 0).toFixed(2)}%`;
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

    function createCell(row, value) {
        const cell = document.createElement("td");
        cell.textContent = value ?? "—";
        row.appendChild(cell);
        return cell;
    }

    function renderBatches(batches) {
        elements.reportsTable.innerHTML = "";

        if (!batches.length) {
            elements.reportsTable.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-state">
                        No batches found. Upload a CSV file from Batch Analysis first.
                    </td>
                </tr>
            `;
            return;
        }

        batches.forEach(batch => {
            const row = document.createElement("tr");

            createCell(row, batch.batchId);
            createCell(row, formatDate(batch.createdAt));
            createCell(
                row,
                `${formatNumber(batch.processedRows)} / ${formatNumber(batch.totalRows)}`
            );
            createCell(row, formatNumber(batch.errorCount));

            const statusCell = document.createElement("td");
            const badge = document.createElement("span");
            badge.className = "status-badge " +
                (batch.status === "COMPLETED" ? "status-completed" : "status-pending");
            badge.textContent = batch.status || "UNKNOWN";
            statusCell.appendChild(badge);
            row.appendChild(statusCell);

            const actionCell = document.createElement("td");
            const viewButton = document.createElement("button");

            viewButton.type = "button";
            viewButton.className = "btn btn-primary view-results";
            viewButton.dataset.batchId = batch.batchId;
            viewButton.textContent = "View Results";

            actionCell.appendChild(viewButton);
            row.appendChild(actionCell);

            elements.reportsTable.appendChild(row);
        });
    }

    async function loadReports() {
        clearMessage();
        elements.refreshButton.disabled = true;

        elements.reportsTable.innerHTML = `
            <tr>
                <td colspan="6" class="empty-state">Loading batch history...</td>
            </tr>
        `;

        try {
            const responseBody = await CreditGuardAPI.get(
                "batches?page=0&size=100"
            );

            const batches = responseBody.data?.batches ??
                responseBody.batches ?? [];

            renderBatches(batches);

            showMessage(
                `${batches.length} batch(es) found.`,
                "success"
            );
        } catch (error) {
            elements.reportsTable.innerHTML = `
                <tr>
                    <td colspan="6" class="empty-state">
                        Unable to load batch history.
                    </td>
                </tr>
            `;

            showMessage(
                error.message || "Failed to load batch history.",
                "error"
            );
        } finally {
            elements.refreshButton.disabled = false;
        }
    }

    function getRiskClass(risk) {
        const value = String(risk || "").toLowerCase();

        if (value === "high") return "badge-high";
        if (value === "medium") return "badge-medium";

        return "badge-low";
    }

    function renderResults(results) {
        elements.resultsTable.innerHTML = "";

        if (!results || results.length === 0) {
            elements.resultsTable.innerHTML = `
                <tr>
                    <td colspan="5" class="empty-state">
                        No successful transaction results found.
                    </td>
                </tr>
            `;
            return;
        }

        results.forEach(transaction => {
            const row = document.createElement("tr");

            createCell(row, transaction.transactionId ?? "N/A");

            const amount = Number(transaction.amount ?? 0);
            createCell(row, `₹${amount.toLocaleString("en-IN", {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            })}`);

            const isFraud = Boolean(transaction.isFraud);

            const fraudCell = document.createElement("td");
            const fraudBadge = document.createElement("span");
            fraudBadge.className = `badge ${
                isFraud ? "badge-fraud" : "badge-normal"
            }`;
            fraudBadge.textContent = isFraud ? "Fraud" : "Normal";
            fraudCell.appendChild(fraudBadge);
            row.appendChild(fraudCell);

            const risk = transaction.riskLevel ?? "LOW";
            const riskCell = document.createElement("td");
            const riskBadge = document.createElement("span");
            riskBadge.className = `badge ${getRiskClass(risk)}`;
            riskBadge.textContent = risk;
            riskCell.appendChild(riskBadge);
            row.appendChild(riskCell);

            createCell(
                row,
                formatPercentage(transaction.riskScore)
            );

            elements.resultsTable.appendChild(row);
        });
    }

    function renderErrors(errors) {
        elements.errorsBody.innerHTML = "";

        const failedRows = errors || [];

        elements.errorsSection.hidden = failedRows.length === 0;
        elements.errorCount.textContent = `${failedRows.length} errors`;

        failedRows.forEach(error => {
            const row = document.createElement("tr");

            createCell(row, error.sourceRowNumber ?? "N/A");
            createCell(row, error.errorMessage ?? "Processing failed.");

            elements.errorsBody.appendChild(row);
        });
    }

    function renderSkippedRows(rows) {
        elements.skippedBody.innerHTML = "";

        const skipped = rows || [];

        elements.skippedSection.hidden = skipped.length === 0;
        elements.skippedRowsCount.textContent = `${skipped.length} skipped`;

        skipped.forEach(item => {
            const row = document.createElement("tr");

            createCell(row, item.sourceRowNumber ?? "N/A");
            createCell(row, item.message ?? "Duplicate row skipped.");

            elements.skippedBody.appendChild(row);
        });
    }

    function updateSummary(data) {
        document.getElementById("totalTransactions").textContent =
            formatNumber(data.totalTransactions);

        document.getElementById("fraudCount").textContent =
            formatNumber(data.fraudCount);

        document.getElementById("normalCount").textContent =
            formatNumber(data.normalCount);

        document.getElementById("fraudPercentage").textContent =
            formatPercentage(data.fraudPercentage);

        document.getElementById("lowRisk").textContent =
            formatNumber(data.lowRiskCount);

        document.getElementById("mediumRisk").textContent =
            formatNumber(data.mediumRiskCount);

        document.getElementById("highRisk").textContent =
            formatNumber(data.highRiskCount);

        document.getElementById("skippedCount").textContent =
            formatNumber(data.skippedCount);
    }

    async function loadBatchDetails(batchId) {
        if (!batchId) return;

        currentBatchId = batchId;

        if (refreshTimer) {
            clearTimeout(refreshTimer);
            refreshTimer = null;
        }

        elements.detailsSection.hidden = false;
        elements.selectedBatchId.textContent = `Batch ID: ${batchId}`;
        elements.downloadButton.disabled = true;

        showStatus("Loading batch results...");

        try {
            const responseBody = await CreditGuardAPI.get(
                `batches/${encodeURIComponent(batchId)}`
            );

            // Backend response: { data: { ... } }
            const data = responseBody.data ?? responseBody;

            // Ignore stale responses if another batch was selected.
            if (currentBatchId !== batchId) return;

            batchResults = data.results ?? [];

            updateSummary(data);
            renderResults(batchResults);
            renderErrors(data.errors ?? []);
            renderSkippedRows(data.skippedRows ?? []);

            const status = String(data.status || "UNKNOWN").toUpperCase();

            if (status === "RECEIVED" || status === "PROCESSING") {
                showStatus(
                    `Batch ${batchId} is ${status.toLowerCase()}. Results will refresh automatically.`
                );

                refreshTimer = setTimeout(() => {
                    loadBatchDetails(batchId);
                }, 2000);

                return;
            }

            if (status === "COMPLETED") {
                showStatus(
                    `Batch processing completed. ${data.processedRows} processed, ${data.skippedCount ?? 0} duplicates skipped.`
                );
                elements.downloadButton.disabled = false;
            } else if (status === "PARTIAL") {
                showStatus(
                    `Batch partially completed. ${data.processedRows} successful, ${data.errorCount} failed, ${data.skippedCount ?? 0} duplicates skipped.`
                );
                elements.downloadButton.disabled = false;
            } else if (status === "FAILED") {
                showStatus("Batch processing failed. Check the errors below.");
            } else {
                showStatus(`Batch status: ${status}`);
            }

            // Refresh history so the displayed status stays current.
            loadReports();
        } catch (error) {
            if (currentBatchId !== batchId) return;

            showStatus(error.message || "Unable to load batch results.");
        }
    }

    async function downloadReport() {
        if (!currentBatchId) {
            showStatus("Select a batch before downloading its report.");
            return;
        }

        const batchId = currentBatchId;

        elements.downloadButton.disabled = true;
        elements.downloadButton.textContent = "Downloading...";

        try {
            await CreditGuardAPI.download(
                `batches/${encodeURIComponent(batchId)}/report`,
                `CreditGuard_Batch_${batchId}_report.csv`
            );

            showStatus("Batch report downloaded successfully.");
        } catch (error) {
            showStatus(error.message || "Report download failed.");
        } finally {
            elements.downloadButton.textContent = "Download CSV";
            elements.downloadButton.disabled = currentBatchId !== batchId;
        }
    }

    elements.refreshButton.addEventListener("click", async () => {
        await loadReports();

        if (currentBatchId) {
            await loadBatchDetails(currentBatchId);
        }
    });

    elements.reportsTable.addEventListener("click", event => {
        const button = event.target.closest(".view-results");

        if (!button) return;

        loadBatchDetails(button.dataset.batchId);
    });

    elements.searchInput.addEventListener("input", function () {
        const searchTerm = this.value.trim().toLowerCase();

        const filteredResults = batchResults.filter(transaction =>
            String(transaction.transactionId ?? "")
                .toLowerCase()
                .includes(searchTerm)
        );

        renderResults(filteredResults);
    });

    elements.downloadButton.addEventListener("click", downloadReport);

    const requestedBatchId =
    new URLSearchParams(window.location.search).get("batchId");

loadReports().then(() => {
    if (requestedBatchId) {
        loadBatchDetails(requestedBatchId);
    }
});
});
