// ==========================================
// CreditGuard - Authentication JavaScript
// File: Frontend/auth/auth.js
// ==========================================

// Show message on the page
function showMessage(message, type = "error") {
    const messageElement = document.getElementById("message");

    if (!messageElement) return;

    messageElement.style.display = "block";
    messageElement.textContent = message;

    messageElement.style.color =
        type === "success" ? "#16a34a" : "#dc2626";
}


// ==========================================
// REGISTER FUNCTION
// ==========================================

const registerForm = document.getElementById("registerForm");

if (registerForm) {
    registerForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const fullName = document.getElementById("fullName").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;
        const confirmPassword =
            document.getElementById("confirmPassword").value;

        const registerButton = document.getElementById("registerButton");

        if (!fullName || !email || !password || !confirmPassword) {
            showMessage("Please fill in all fields.");
            return;
        }

        if (password.length < 8) {
            showMessage("Password must be at least 8 characters long.");
            return;
        }

        if (password !== confirmPassword) {
            showMessage("Passwords do not match.");
            return;
        }

        registerButton.disabled = true;
        registerButton.textContent = "Creating Account...";

        try {
            const response = await fetch("/api/v1/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    fullName: fullName,
                    email: email,
                    password: password
                })
            });

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                throw new Error(data.message || "Registration failed.");
            }

            showMessage("Account created successfully. Please login.", "success");

            registerForm.reset();

            setTimeout(function () {
                window.location.href = "login.html";
            }, 1500);

        } catch (error) {
            showMessage(error.message || "Unable to connect to server.");
        } finally {
            registerButton.disabled = false;
            registerButton.textContent = "Create Account";
        }
    });
}


// ==========================================
// LOGIN FUNCTION
// ==========================================

const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value;

        const loginButton = document.getElementById("loginButton");

        if (!email || !password) {
            showMessage("Please enter email and password.");
            return;
        }

        loginButton.disabled = true;
        loginButton.textContent = "Logging in...";

        try {
            const response = await fetch("/api/v1/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: "include",
                body: JSON.stringify({
                    email: email,
                    password: password
                })
            });

            const data = await response.json().catch(() => ({}));

            if (!response.ok) {
                throw new Error(data.message || "Invalid email or password.");
            }

            showMessage("Login successful!", "success");

            // Redirect after successful login
            setTimeout(function () {
                window.location.href = "../dashboard/dashboard.html";
            }, 800);

        } catch (error) {
            showMessage(error.message || "Unable to connect to server.");
        } finally {
            loginButton.disabled = false;
            loginButton.textContent = "Login";
        }
    });
}
