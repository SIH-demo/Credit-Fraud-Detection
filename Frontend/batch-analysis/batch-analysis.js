// ==========================================
// CreditGuard - Batch Analysis JavaScript
// File: Frontend/batch-analysis/batch-analysis.js
// ==========================================

document.addEventListener("DOMContentLoaded", function () {

    // DOM Elements
    const uploadForm = document.getElementById("batchUploadForm");
    const csvFileInput = document.getElementById("csvFile");
    const browseButton = document.getElementById("browseButton");
    const uploadButton = document.getElementById("uploadButton");
    const resetButton = document.getElementById("resetButton");
    const removeFileButton = document.getElementById("removeFileButton");

    const uploadArea = document.getElementById("uploadArea");
    const selectedFile = document.getElementById("selectedFile");
    const fileName = document.getElementById("fileName");
    const fileSize = document.getElementById("fileSize");

    const uploadStatus = document.getElementById("uploadStatus");
    const latestBatch = document.getElementById("latestBatch");
    const viewResultsLink = document.getElementById("viewResultsLink");

    // API endpoint from CreditGuard SRS
    const API_URL = "/api/v1/batches";

    let currentFile = null;

    // --------------------------------------
    // Display status message
    // --------------------------------------

    function showStatus(message, type = "info") {
        uploadStatus.hidden = false;
        uploadStatus.textContent = message;

        const colors = {
            info: {
                background: "#eff6ff",
                color: "#1e40af"
            },
            success: {
                background: "#dcfce7",
                color: "#166534"
            },
            error: {
                background: "#fee2e2",
                color: "#991b1b"
            }
        };

        const style = colors[type] || colors.info;

        uploadStatus.style.backgroundColor = style.background;
        uploadStatus.style.color = style.color;
    }

    function hideStatus() {
        uploadStatus.hidden = true;
        uploadStatus.textContent = "";
    }

    // --------------------------------------
    // Format file size
    // --------------------------------------

    function formatFileSize(bytes) {
        if (bytes < 1024) {
            return bytes + " Bytes";
        }

        if (bytes < 1024 * 1024) {
            return (bytes / 1024).toFixed(2) + " KB";
        }

        return (bytes / (1024 * 1024)).toFixed(2) + " MB";
    }

    // --------------------------------------
    // Validate CSV file
    // --------------------------------------

    function validateFile(file) {
        if (!file) {
            showStatus("Please select a CSV file.", "error");
            return false;
        }

        if (!file.name.toLowerCase().endsWith(".csv")) {
            showStatus("Invalid file. Please select a CSV file.", "error");
            return false;
        }

        if (file.size === 0) {
            showStatus("The selected file is empty.", "error");
            return false;
        }

        return true;
    }

    // --------------------------------------
    // Display selected file
    // --------------------------------------

    function displaySelectedFile(file) {
        if (!validateFile(file)) {
            return;
        }

        currentFile = file;

        fileName.textContent = file.name;
        fileSize.textContent = formatFileSize(file.size);

        selectedFile.hidden = false;
        hideStatus();
    }

    // --------------------------------------
    // Browse files
    // --------------------------------------

    browseButton.addEventListener("click", function () {
        csvFileInput.click();
    });

    csvFileInput.addEventListener("change", function () {
        const file = csvFileInput.files[0];

        if (file) {
            displaySelectedFile(file);
        }
    });

    // --------------------------------------
    // Remove selected file
    // --------------------------------------

    removeFileButton.addEventListener("click", function () {
        currentFile = null;
        csvFileInput.value = "";

        selectedFile.hidden = true;
        fileName.textContent = "";
        fileSize.textContent = "";

        hideStatus();
    });

    // --------------------------------------
    // Drag and drop functionality
    // --------------------------------------

    uploadArea.addEventListener("dragover", function (event) {
        event.preventDefault();
        uploadArea.classList.add("dragover");
    });

    uploadArea.addEventListener("dragleave", function () {
        uploadArea.classList.remove("dragover");
    });

    uploadArea.addEventListener("drop", function (event) {
        event.preventDefault();

        uploadArea.classList.remove("dragover");

        const file = event.dataTransfer.files[0];

        if (!file) {
            return;
        }

        displaySelectedFile(file);

        if (currentFile) {
            try {
                const dataTransfer = new DataTransfer();
                dataTransfer.items.add(file);
                csvFileInput.files = dataTransfer.files;
            } catch (error) {
                // currentFile is still available for uploading.
            }
        }
    });

    // --------------------------------------
    // Upload CSV to Spring Boot backend
    // --------------------------------------

    uploadForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        if (!validateFile(currentFile)) {
            return;
        }

        const formData = new FormData();

        // Assumed multipart field name: "file".
        // Confirm this name with your Spring Boot controller.
        formData.append("file", currentFile);

        uploadButton.disabled = true;
        browseButton.disabled = true;
        resetButton.disabled = true;
        removeFileButton.disabled = true;

        uploadButton.textContent = "Uploading...";

        showStatus(
            "Uploading your CSV file. Please wait...",
            "info"
        );

        try {
            const response = await fetch(API_URL, {
                method: "POST",
                credentials: "include",
                body: formData
            });

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    data.error ||
                    `Upload failed. Server returned ${response.status}.`
                );
            }

            // Expected response field: batchId.
            // Confirm the actual response structure with your backend.
            const batchId = data.batchId ?? data.id;

            if (!batchId) {
                throw new Error(
                    "Upload response did not contain a batch ID."
                );
            }

            showStatus(
                "CSV uploaded successfully. Opening batch results...",
                "success"
            );

            latestBatch.textContent = "Batch ID: " + batchId;

            viewResultsLink.href =
                "batch-results.html?batchId=" +
                encodeURIComponent(batchId);

            viewResultsLink.hidden = false;

            // Open results page
            setTimeout(function () {
                window.location.href = viewResultsLink.href;
            }, 1000);

        } catch (error) {
            showStatus(
                error.message || "Unable to upload CSV file.",
                "error"
            );

            uploadButton.disabled = false;
            browseButton.disabled = false;
            resetButton.disabled = false;
            removeFileButton.disabled = false;

            uploadButton.textContent = "Upload and Analyze";
        }
    });

    // --------------------------------------
    // Reset form
    // --------------------------------------

    resetButton.addEventListener("click", function () {
        currentFile = null;
        csvFileInput.value = "";

        selectedFile.hidden = true;
        fileName.textContent = "";
        fileSize.textContent = "";

        latestBatch.textContent =
            "No batch uploaded in this session.";

        viewResultsLink.hidden = true;

        hideStatus();
    });

});
