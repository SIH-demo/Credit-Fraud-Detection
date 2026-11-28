
document.addEventListener("DOMContentLoaded", () => {
    const message = document.getElementById("usersMessage");
    const tableBody = document.getElementById("usersTableBody");

    function showMessage(text) {
        message.textContent = text;
    }

    function addCell(row, value) {
        const cell = document.createElement("td");
        cell.textContent = value ?? "-";
        row.appendChild(cell);
        return cell;
    }

    async function loadUsers() {
        tableBody.replaceChildren();
        showMessage("Loading users...");

        try {
            const users = await CreditGuardAPI.get("admin/users");

            if (!Array.isArray(users)) {
                throw new Error("Invalid users response.");
            }

            if (users.length === 0) {
                showMessage("No users found.");
                return;
            }

            users.forEach(user => {
                const row = document.createElement("tr");

                addCell(row, user.fullName);
                addCell(row, user.email);
                addCell(row, user.role);
                addCell(row, user.status);

                const actionCell = document.createElement("td");
                const button = document.createElement("button");

                if (user.status === "PENDING") {
                    button.textContent = "Pending verification";
                    button.disabled = true;
                } else {
                    const activate = user.status !== "ACTIVE";

                    button.textContent = activate ? "Activate" : "Lock";

                    button.addEventListener("click", async () => {
                        button.disabled = true;

                        try {
                            await CreditGuardAPI.patch(
                                `admin/users/${user.id}/status`,
                                {
                                    status: activate ? "ACTIVE" : "LOCKED"
                                }
                            );

                            await loadUsers();
                        } catch (error) {
                            showMessage(
                                error.status === 403
                                    ? "You are not authorized to perform this action."
                                    : error.message || "Unable to update user status."
                            );

                            button.disabled = false;
                        }
                    });
                }

                actionCell.appendChild(button);
                row.appendChild(actionCell);
                tableBody.appendChild(row);
            });

            showMessage(`${users.length} user(s) found.`);
        } catch (error) {
            tableBody.replaceChildren();

            if (error.status === 403) {
                showMessage("Administrator access required.");
            } else if (error.status === 401) {
                showMessage("Please log in to continue.");
            } else {
                showMessage(error.message || "Unable to load users.");
            }
        }
    }

    loadUsers();
});
