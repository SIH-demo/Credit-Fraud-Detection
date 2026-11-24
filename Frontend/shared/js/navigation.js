
/* =========================================
   CREDITGUARD - NAVIGATION CONTROLLER
   File: Frontend/shared/js/navigation.js
========================================= */

(function () {
    "use strict";

    const Navigation = {

        init() {
            document.addEventListener("DOMContentLoaded", () => {
                this.setActiveLink();
                this.initializeSidebarToggle();
                this.initializeSubmenus();
                this.initializeNavigationLinks();
                this.initializeOutsideClick();
            });
        },

        // Highlight the current page in sidebar.
        setActiveLink() {
            const currentPath = window.location.pathname
                .replace(/\/+$/, "")
                .toLowerCase();

            const links = document.querySelectorAll(
                ".sidebar a[href], .nav-link[href]"
            );

            links.forEach((link) => {
                const href = link.getAttribute("href");

                if (!href || href.startsWith("#")) return;

                let targetPath;

                try {
                    targetPath = new URL(href, window.location.href)
                        .pathname.replace(/\/+$/, "")
                        .toLowerCase();
                } catch {
                    return;
                }

                if (targetPath === currentPath) {
                    link.classList.add("active");
                    link.setAttribute("aria-current", "page");

                    const parentSubmenu = link.closest(".submenu");

                    if (parentSubmenu) {
                        parentSubmenu.classList.add("open");

                        const parentButton = parentSubmenu
                            .previousElementSibling;

                        if (parentButton) {
                            parentButton.classList.add("active");
                            parentButton.setAttribute(
                                "aria-expanded",
                                "true"
                            );
                        }
                    }
                } else {
                    link.classList.remove("active");
                    link.removeAttribute("aria-current");
                }
            });
        },

        // Mobile sidebar toggle.
        initializeSidebarToggle() {
            const sidebar = document.querySelector(".sidebar");
            const toggleButton = document.querySelector(
                ".sidebar-toggle"
            );

            if (!sidebar || !toggleButton) return;

            toggleButton.setAttribute("aria-expanded", "false");

            toggleButton.addEventListener("click", (event) => {
                event.stopPropagation();

                const isOpen = sidebar.classList.toggle("open");

                toggleButton.setAttribute(
                    "aria-expanded",
                    String(isOpen)
                );
            });
        },

        // Handle expandable navigation menus.
        initializeSubmenus() {
            const submenuButtons = document.querySelectorAll(
                ".submenu-toggle, [data-submenu-toggle]"
            );

            submenuButtons.forEach((button) => {
                button.addEventListener("click", () => {
                    const submenu = button.nextElementSibling;

                    if (!submenu ||
                        !submenu.classList.contains("submenu")) {
                        return;
                    }

                    const isOpen = submenu.classList.toggle("open");

                    button.classList.toggle("active", isOpen);
                    button.setAttribute(
                        "aria-expanded",
                        String(isOpen)
                    );
                });
            });
        },

        // Close mobile sidebar after navigation.
        initializeNavigationLinks() {
            const links = document.querySelectorAll(
                ".sidebar a[href], .nav-link[href]"
            );

            links.forEach((link) => {
                link.addEventListener("click", () => {
                    if (window.innerWidth <= 600) {
                        this.closeSidebar();
                    }
                });
            });
        },

        // Close sidebar when clicking outside it.
        initializeOutsideClick() {
            document.addEventListener("click", (event) => {
                if (window.innerWidth > 600) return;

                const sidebar = document.querySelector(".sidebar");
                const toggleButton = document.querySelector(
                    ".sidebar-toggle"
                );

                if (!sidebar || !sidebar.classList.contains("open")) {
                    return;
                }

                if (
                    !sidebar.contains(event.target) &&
                    !toggleButton?.contains(event.target)
                ) {
                    this.closeSidebar();
                }
            });
        },

        // Close sidebar.
        closeSidebar() {
            const sidebar = document.querySelector(".sidebar");
            const toggleButton = document.querySelector(
                ".sidebar-toggle"
            );

            sidebar?.classList.remove("open");

            toggleButton?.setAttribute(
                "aria-expanded",
                "false"
            );
        },

        // Navigate programmatically.
        goTo(url) {
            if (typeof url !== "string" || !url.trim()) return;

            // Prevent javascript: and other non-web protocols.
            const target = new URL(url, window.location.href);

            if (!["http:", "https:"].includes(target.protocol)) {
                return;
            }

            window.location.assign(target.href);
        }
    };

    // Initialize navigation.
    Navigation.init();

    // Make navigation available to other scripts.
    window.CreditGuardNavigation = Navigation;

})();
