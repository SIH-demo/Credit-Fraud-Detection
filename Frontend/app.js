
/* =========================================================
   CreditGuard - Main Application Controller
   File: Frontend/app.js
   ========================================================= */

(function () {
    "use strict";

    const CreditGuardApp = {
        init() {
            this.initLogout();
            this.initGlobalEvents();
            this.initActiveNavigation();
        },

        // Logout button handling
        initLogout() {
            document.addEventListener("click", (event) => {
                const logoutButton = event.target.closest(
                    "[data-logout], .logout-btn"
                );

                if (!logoutButton) return;

                event.preventDefault();

                if (window.CreditGuardSession) {
                    window.CreditGuardSession.logout();
                } else {
                    window.location.href = "/auth/login.html";
                }
            });
        },

        // Global application events
        initGlobalEvents() {
            // Session expired / unauthorized response
            document.addEventListener(
                "creditguard:unauthorized",
                () => {
                    if (window.CreditGuardSession) {
                        window.CreditGuardSession.clearLocalSession();
                    }

                    window.location.href = "/auth/login.html";
                }
            );

            // Global error logging
            window.addEventListener("error", (event) => {
                console.error(
                    "CreditGuard application error:",
                    event.message
                );
            });

            // Handle unhandled promise errors
            window.addEventListener("unhandledrejection", (event) => {
                console.error(
                    "CreditGuard promise error:",
                    event.reason
                );
            });
        },

        // Highlight current page in sidebar
        initActiveNavigation() {
            const currentPath = window.location.pathname
                .replace(/\/+$/, "")
                .toLowerCase();

            document.querySelectorAll(".sidebar a[href]").forEach((link) => {
                const href = link.getAttribute("href");

                if (!href || href.startsWith("#")) return;

                const linkUrl = new URL(href, window.location.href);

                const linkPath = linkUrl.pathname
                    .replace(/\/+$/, "")
                    .toLowerCase();

                const isCurrentPage = linkPath === currentPath;

                link.classList.toggle("active", isCurrentPage);

                if (isCurrentPage) {
                    link.setAttribute("aria-current", "page");
                } else {
                    link.removeAttribute("aria-current");
                }
            });
        },

        // Display success/error/info messages
        showMessage(elementId, message, type = "info") {
            const element = document.getElementById(elementId);

            if (!element) return;

            element.textContent = message;
            element.className = `alert alert-${type}`;
            element.hidden = false;
        },

        // Hide a message
        hideMessage(elementId) {
            const element = document.getElementById(elementId);

            if (!element) return;

            element.textContent = "";
            element.hidden = true;
        },

        // Navigate to another page
        navigate(url) {
            if (typeof url !== "string" || !url.trim()) return;

            window.location.href = url;
        }
    };

    // Make globally accessible
    window.CreditGuardApp = CreditGuardApp;

    // Initialize after HTML loads
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", () => {
            CreditGuardApp.init();
        });
    } else {
        CreditGuardApp.init();
    }

})();
