
/* =========================================
   CREDITGUARD - SHARED API SERVICE
   File: Frontend/shared/js/api.js
========================================= */

(function () {
    "use strict";

    // Use relative URL because Nginx proxies /api/ to Spring Boot.
    const API_BASE_URL = "/api/v1";

    /**
     * Custom API Error
     */
    class ApiError extends Error {
        constructor(message, status, data) {
            super(message);
            this.name = "ApiError";
            this.status = status;
            this.data = data;
        }
    }

    /**
     * Convert response into JSON safely.
     */
    async function parseResponse(response) {
        const contentType = response.headers.get("content-type") || "";

        if (response.status === 204) {
            return null;
        }

        if (contentType.includes("application/json")) {
            return response.json();
        }

        return response.text();
    }

    /**
     * Main request handler.
     */
    async function request(endpoint, options = {}) {
        const url = endpoint.startsWith("http")
            ? endpoint
            : `${API_BASE_URL}${endpoint.startsWith("/") ? "" : "/"}${endpoint}`;

        const headers = new Headers(options.headers || {});

        const isFormData = options.body instanceof FormData;

        if (options.body && !isFormData && !headers.has("Content-Type")) {
            headers.set("Content-Type", "application/json");
        }

        if (!headers.has("Accept")) {
            headers.set("Accept", "application/json");
        }

        const config = {
            ...options,
            headers,
            credentials: "include"
        };

        try {
            const response = await fetch(url, config);
            const data = await parseResponse(response);

            if (!response.ok) {
                const message =
                    typeof data === "object" && data !== null
                        ? data.message || data.error || `Request failed (${response.status})`
                        : data || `Request failed (${response.status})`;

                if (response.status === 401) {
                    window.dispatchEvent(
                        new CustomEvent("creditguard:unauthorized")
                    );
                }

                throw new ApiError(
                    String(message),
                    response.status,
                    data
                );
            }

            return data;

        } catch (error) {
            if (error instanceof ApiError) {
                throw error;
            }

            throw new ApiError(
                error.message || "Unable to connect to the server.",
                0,
                null
            );
        }
    }

    /**
     * GET request
     */
    function get(endpoint, options = {}) {
        return request(endpoint, {
            ...options,
            method: "GET"
        });
    }

    /**
     * POST request
     */
    function post(endpoint, body, options = {}) {
        return request(endpoint, {
            ...options,
            method: "POST",
            body: body instanceof FormData
                ? body
                : JSON.stringify(body)
        });
    }

    /**
     * PATCH request
     */
    function patch(endpoint, body, options = {}) {
        return request(endpoint, {
            ...options,
            method: "PATCH",
            body: JSON.stringify(body)
        });
    }

    /**
     * DELETE request
     */
    function remove(endpoint, options = {}) {
        return request(endpoint, {
            ...options,
            method: "DELETE"
        });
    }

    /**
     * Upload a file using FormData.
     * Do not manually set Content-Type for FormData.
     */
    function upload(endpoint, formData, options = {}) {
        if (!(formData instanceof FormData)) {
            throw new TypeError("Upload requires FormData.");
        }

        return request(endpoint, {
            ...options,
            method: "POST",
            body: formData
        });
    }

    /**
     * Download a file.
     */
    async function download(endpoint, filename, options = {}) {
        const url = endpoint.startsWith("http")
            ? endpoint
            : `${API_BASE_URL}${endpoint.startsWith("/") ? "" : "/"}${endpoint}`;

        try {
            const response = await fetch(url, {
                ...options,
                method: "GET",
                credentials: "include"
            });

            if (!response.ok) {
                const data = await parseResponse(response);
                const message =
                    typeof data === "object" && data !== null
                        ? data.message || data.error || "File download failed."
                        : data || "File download failed.";

                throw new ApiError(
                    String(message),
                    response.status,
                    data
                );
            }

            const blob = await response.blob();
            const objectUrl = URL.createObjectURL(blob);
            const link = document.createElement("a");

            link.href = objectUrl;
            link.download = filename || "download";
            document.body.appendChild(link);
            link.click();
            link.remove();

            URL.revokeObjectURL(objectUrl);

        } catch (error) {
            if (error instanceof ApiError) {
                throw error;
            }

            throw new ApiError(
                error.message || "Unable to download file.",
                0,
                null
            );
        }
    }

    /**
     * Public API methods
     */
    window.CreditGuardAPI = {
        request,
        get,
        post,
        patch,
        delete: remove,
        upload,
        download,
        ApiError
    };

})();
