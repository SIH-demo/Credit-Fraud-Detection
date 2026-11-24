
/* =========================================
   CREDITGUARD - SETTINGS JAVASCRIPT
   File: Frontend/settings/settings.js
========================================= */

document.addEventListener("DOMContentLoaded", () => {

    const settingsForm = document.getElementById("settingsForm");
    const settingsMessage = document.getElementById("settingsMessage");
    const saveButton = document.getElementById("saveSettingsButton");
    const resetButton = document.getElementById("resetSettingsButton");

    const STORAGE_KEY = "creditguard_settings";

    // Default application settings
    const defaultSettings = {
        systemName: "CreditGuard",
        timezone: "Asia/Kolkata",
        dateFormat: "DD/MM/YYYY",
        currency: "INR",

        sessionTimeout: "30",
        passwordExpiry: "90",

        twoFactorAuth: false,
        loginAlerts: true,
        auditLogging: true,

        fraudAlerts: true,
        investigationAlerts: true,
        batchAlerts: true,
        systemAlerts: true
    };

    // Fields that can be saved
    const fieldIds = Object.keys(defaultSettings);

    // Display messages
    function showMessage(message, type = "success") {
        if (!settingsMessage) return;

        settingsMessage.textContent = message;
        settingsMessage.className = "message " + type;

        // Clear message after 4 seconds
        setTimeout(() => {
            settingsMessage.textContent = "";
            settingsMessage.className = "message";
        }, 4000);
    }

    // Get current form values
    function getSettings() {
        const settings = {};

        fieldIds.forEach(id => {
            const field = document.getElementById(id);

            if (!field) return;

            if (field.type === "checkbox") {
                settings[id] = field.checked;
            } else {
                settings[id] = field.value;
            }
        });

        return settings;
    }

    // Apply settings to form
    function applySettings(settings) {
        fieldIds.forEach(id => {
            const field = document.getElementById(id);

            if (!field || settings[id] === undefined) return;

            if (field.type === "checkbox") {
                field.checked = Boolean(settings[id]);
            } else {
                field.value = settings[id];
            }
        });
    }

    // Load saved settings
    function loadSettings() {
        try {
            const savedSettings = localStorage.getItem(STORAGE_KEY);

            if (!savedSettings) {
                applySettings(defaultSettings);
                return;
            }

            const parsedSettings = JSON.parse(savedSettings);

            if (
                !parsedSettings ||
                typeof parsedSettings !== "object" ||
                Array.isArray(parsedSettings)
            ) {
                throw new Error("Invalid settings data");
            }

            // Merge saved settings with defaults
            const mergedSettings = {
                ...defaultSettings,
                ...parsedSettings
            };

            applySettings(mergedSettings);

        } catch (error) {
            console.error("Settings loading error:", error);
            applySettings(defaultSettings);
            showMessage("Could not load saved settings. Defaults restored.", "error");
        }
    }

    // Save settings
    function saveSettings() {
        try {
            const settings = getSettings();

            // Basic validation
            if (!settings.systemName.trim()) {
                showMessage("Application name is required.", "error");
                document.getElementById("systemName").focus();
                return;
            }

            const timeout = Number(settings.sessionTimeout);

            if (!Number.isFinite(timeout) || timeout <= 0) {
                showMessage("Please select a valid session timeout.", "error");
                return;
            }

            localStorage.setItem(
                STORAGE_KEY,
                JSON.stringify(settings)
            );

            showMessage("Settings saved successfully in this browser.");

        } catch (error) {
            console.error("Settings saving error:", error);
            showMessage("Unable to save settings in this browser.", "error");
        }
    }

    // Reset settings to defaults
    function resetSettings() {
        const confirmed = window.confirm(
            "Are you sure you want to reset all settings to default values?"
        );

        if (!confirmed) {
            loadSettings();
            return;
        }

        try {
            localStorage.removeItem(STORAGE_KEY);
            applySettings(defaultSettings);

            showMessage("Settings restored to default values.");

        } catch (error) {
            console.error("Settings reset error:", error);
            showMessage("Unable to reset settings.", "error");
        }
    }

    // Form submit event
    settingsForm.addEventListener("submit", event => {
        event.preventDefault();
        saveSettings();
    });

    // Reset button event
    resetButton.addEventListener("click", event => {
        event.preventDefault();
        resetSettings();
    });

    // Load settings when page opens
    loadSettings();

});

