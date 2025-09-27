document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("register-form");
    const errorDiv = document.getElementById("register-error");

    form.addEventListener("submit", async function (event) {
        event.preventDefault(); // Stop normal submit

        const formData = new FormData(form);
        const data = Object.fromEntries(formData.entries());

        const password = data.password;
        const confirmPassword = data.confirmPassword;

        // Regex: at least one uppercase, one lowercase, one digit, one special
        const passwordRegex =
            /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&^#()[\]{}|\\:;'",.<>/~`+-=_]).{8,30}$/;

        // Validation checks
        if (password !== confirmPassword) {
            errorDiv.textContent = "Passwords do not match.";
            errorDiv.classList.remove("hidden");
            return;
        }

        if (!passwordRegex.test(password)) {
            errorDiv.textContent =
                "Password must contain at least one uppercase, one lowercase, one number, and one special character.";
            errorDiv.classList.remove("hidden");
            return;
        }

        // Hide error before sending
        errorDiv.classList.add("hidden");

        try {
            const response = await fetch("/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(data),
            });

            if (response.status === 401) {
                errorDiv.textContent = await response.text();
                errorDiv.classList.remove("hidden");
                return;
            }

            if (response.status === 201) {
                // If backend created the account successfully → redirect
                window.location.href = "/";
                return;
            }

            // For any other status code, show server response
            errorDiv.textContent = 'Internal server error' || `Unexpected error (status ${response.status}).`;
            errorDiv.classList.remove("hidden");
        } catch (err) {
            errorDiv.textContent = "Network error: " + err.message;
            errorDiv.classList.remove("hidden");
        }
    });
});