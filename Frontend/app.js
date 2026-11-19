
const totalTransactions = document.getElementById("totalTransactions");
const fraudDetected = document.getElementById("fraudDetected");
const systemStatus = document.getElementById("systemStatus");
const testButton = document.getElementById("testButton");
const testResult = document.getElementById("testResult");

// Placeholder values; not real transaction statistics.
totalTransactions.textContent = "—";
fraudDetected.textContent = "—";

// Check Spring Boot backend connectivity
async function checkBackend() {
    try {
        const response = await fetch("/api/health");

        if (!response.ok) {
            throw new Error("Backend returned HTTP " + response.status);
        }

        const data = await response.json();

        systemStatus.textContent = data.status;
    } catch (error) {
        systemStatus.textContent = "Unavailable";
        console.error("Backend connection failed:", error);
    }
}

// Run backend check when the page loads
checkBackend();

// Test frontend JavaScript
testButton.addEventListener("click", () => {
    testResult.textContent = "Frontend JavaScript is working!";
});
