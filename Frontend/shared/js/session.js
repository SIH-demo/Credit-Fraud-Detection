
/* =========================================
   CREDITGUARD - SESSION MANAGEMENT
   File: Frontend/shared/js/session.js
========================================= */

(function () {
    "use strict";

    const STORAGE_KEY = "creditguard_user";

    const Session = {

        /**
         * Read locally stored user display information.
         * This is NOT proof of authentication.
         */
        getUser() {
            try {
                const user = sessionStorage.getItem(STORAGE_KEY);
                return user ? JSON.parse(user) : null;
            } catch (error) {
                console.error("Unable to read session information:", error);
                return null;
            }
        },

        /**
         * Save only non-sensitive user display information.
         * Never save passwords, session IDs or access tokens here.
         */
        setUser(user) {
            if (!user || typeof user !== "object") {
                throw new Error("Invalid user information.");
            }

            const safeUser = {
                id: user.id ?? null,
                name: user.name ?? user.fullName ?? "",
                email: user.email ?? "",
                role: user.role ?? ""
            };

            sessionStorage.setItem(
                STORAGE_KEY,
                JSON.stringify(safeUser)
            );

            window.dispatchEvent(
                new CustomEvent("creditguard:session-updated", {
                    detail: { user: safeUser }
                })
            );

            return safeUser;
        },

        /**
         * Login using the confirmed Spring Boot endpoint.
         * Exact response fields must be matched to backend DTO.
         */
        async login(credentials) {
            if (!window.CreditGuardAPI) {
                throw new Error("API service is not loaded.");
            }

            if (!credentials || !credentials.email ||
                !credentials.password) {
                throw new Error("Email and password are required.");
            }

            const response = await CreditGuardAPI.post(
                "/auth/login",
                {
                    email: credentials.email,
                    password: credentials.password
                }
            );

            // Supports common response wrappers.
            const user =
                response?.user ??
                response?.data?.user ??
                response?.data ??
                response;

            this.setUser(user);

            window.dispatchEvent(
                new CustomEvent("creditguard:login", {
                    detail: { user: this.getUser() }
                })
            );

            return response;
        },

        /**
         * Get currently stored display profile.
         */
        getCurrentUser() {
            return this.getUser();
        },

        /**
         * Check whether a display profile exists.
         * This does NOT validate the backend session.
         */
        hasUserProfile() {
            return Boolean(this.getUser());
        },

        /**
         * Check the locally stored role for UI purposes.
         * Backend must independently enforce permissions.
         */
        hasRole(allowedRoles) {
            const user = this.getUser();

            if (!user || !user.role) {
                return false;
            }

            const roles = Array.isArray(allowedRoles)
                ? allowedRoles
                : [allowedRoles];

            return roles.includes(user.role);
        },

        /**
         * Show/hide UI elements based on data-role attribute.
         * This is only a UI convenience, not authorization.
         */
        applyRoleVisibility() {
            const user = this.getUser();

            document.querySelectorAll("[data-role]").forEach((element) => {
                const allowedRoles = element.dataset.role
                    .split(",")
                    .map(role => role.trim());

                const visible = user &&
                    allowedRoles.includes(user.role);

                element.hidden = !visible;
            });
        },

        /**
         * Clear local profile information.
         * Does not invalidate the server's HttpOnly cookie.
         */
        clearLocalSession() {
            sessionStorage.removeItem(STORAGE_KEY);

            window.dispatchEvent(
                new CustomEvent("creditguard:logout")
            );
        },

        /**
         * Redirect to login after local session cleanup.
         * Call a backend logout endpoint here once it is defined.
         */
        logout() {
            this.clearLocalSession();
            window.location.href = "/auth/login.html";
        },

        /**
         * Display the current user's name.
         */
        updateUserDisplay() {
            const user = this.getUser();

            if (!user) return;

            document.querySelectorAll("[data-user-name]").forEach(
                element => {
                    element.textContent = user.name || "User";
                }
            );

            document.querySelectorAll("[data-user-role]").forEach(
                element => {
                    element.textContent = user.role || "User";
                }
            );

            this.applyRoleVisibility();
        }
    };

    // Make session utilities available globally.
    window.CreditGuardSession = Session;

    // Update profile information after page load.
    document.addEventListener("DOMContentLoaded", () => {
        Session.updateUserDisplay();
    });

})();

