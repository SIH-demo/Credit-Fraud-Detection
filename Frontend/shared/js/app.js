
/* =========================================
   CREDITGUARD - MAIN APPLICATION CONTROLLER
   File: Frontend/app.js
========================================= */

(function () {
    "use strict";

    const CreditGuardApp = {

        /**
         * Initialize the application.
         */
        init() {
            document.addEventListener("DOMContentLoaded", () => {
                this.initializeSidebar();
                this.initializeNavigation();
                this.initializeLogout();
                this.initializeGlobalEvents();
                this.highlightActivePage();
            });
        },

        /**
         * Sidebar toggle for mobile screens.
         */
        initializeSidebar() {
            const sidebar = document.querySelector(".sidebar");
            const toggleButton = document.querySelector(".sidebar-toggle");

            if (!sidebar || !toggleButton) return;

            toggleButton.addEventListener("click", () => {
                sidebar.classList.toggle("open");

                const isOpen = sidebar.classList.contains("open");
                toggleButton.setAttribute("aria-expanded", String(isOpen));
            });

            document.addEventListener("click", (event) => {
                if (
                    window.innerWidth <= 600 &&
                    sidebar.classList.contains("open") &&
                    !sidebar.contains(event.target) &&
                    !toggleButton.contains(event.target)
                ) {
                    sidebar.classList.remove("open");
                    toggleButton.setAttribute("aria-expanded", "false");
                }
            });
        },

        /**
         * Handle navigation links.
         */
        initializeNavigation() {
            const links = document.querySelectorAll(
                ".sidebar a, .nav-link"
            );

            links.forEach((link) => {
                link.addEventListener("click", () => {
                    if (window.innerWidth <= 600) {
                        document.querySelector(".sidebar")
                            ?.classList.remove("open");
                    }
                });
            });
        },

        /**
         * Highlight the current page in sidebar.
         */
        highlightActivePage() {
            const currentPage = window.location.pathname
                .split("/")
                .pop()
                .toLowerCase();

            const links = document.querySelectorAll(
                ".sidebar a, .nav-link"
            );

            links.forEach((link) => {
                const linkPage = link.getAttribute("href");

                if (!linkPage || linkPage.startsWith("#")) return;

                const targetPage = linkPage
                    .split("/")
                    .pop()
                    .toLowerCase();

                if (targetPage === currentPage) {
                    link.classList.add("active");
                    link.setAttribute("aria-current", "page");
                } else {
                    link.classList.remove("active");
                    link.removeAttribute("aria-current");
                }
            });
        },

        /**
         * Handle logout button.
         * Backend logout endpoint must be confirmed.
         */
        initializeLogout() {
            const logoutButtons = document.querySelectorAll(
                "#logoutButton, .logout-button"
            );

            logoutButtons.forEach((button) => {
                button.addEventListener("click", async (event) => {
                    event.preventDefault();

                    const confirmed = window.confirm(
                        "Are you sure you want to logout?"
                    );

                    if (!confirmed) return;

                    try {
                        if (window.CreditGuardAPI) {
                            // Assumed endpoint: confirm with backend team.
                            await CreditGuardAPI.post("/auth/logout", {});
                        }

                        window.location.href = "/auth/login.html";

                    } catch (error) {
                        this.showMessage(
                            error.message || "Logout failed.",
                            "error"
                        );
                    }
                });
            });
        },

        /**
         * Global application events.
         */
        initializeGlobalEvents() {
            window.addEventListener(
                "creditguard:unauthorized",
                () => {
                    window.location.href = "/auth/login.html";
                }
            );

            window.addEventListener("online", () => {
                this.showMessage("Internet connection restored.", "success");
            });

            window.addEventListener("offline", () => {
                this.showMessage(
                    "Internet connection lost. Check your network.",
                    "warning"
                );
            });
        },

        /**
         * Display common notification.
         */
        showMessage(message, type = "info") {
            let notification = document.getElementById(
                "appNotification"
            );

            if (!notification) {
                notification = document.createElement("div");
                notification.id = "appNotification";
                notification.setAttribute("role", "status");
                notification.setAttribute("aria-live", "polite");

                Object.assign(notification.style, {
                    position: "fixed",
                    top: "20px",
                    right: "20px",
                    zIndex: "9999",
                    maxWidth: "350px",
                    padding: "12px 18px",
                    borderRadius: "8px",
                    color: "#ffffff",
                    fontSize: "14px",
                    boxShadow: "0 4px 12px rgba(0,0,0,0.15)"
                });

                document.body.appendChild(notification);
            }

            const colors = {
                success: "#16a34a",
                error: "#dc2626",
                warning: "#d97706",
                info: "#2563eb"
            };

            notification.textContent = message;
            notification.style.backgroundColor =
                colors[type] || colors.info;

            notification.style.display = "block";

            clearTimeout(this.notificationTimer);

            this.notificationTimer = setTimeout(() => {
                notification.style.display = "none";
            }, 3500);
        }
    };

    // Start application.
    CreditGuardApp.init();

    // Make utilities accessible to other frontend files.
    window.CreditGuardApp = CreditGuardApp;

})();
