document.getElementById('login-form').addEventListener('submit', async function (e) {
    e.preventDefault();
    const login = {
        username: document.getElementById('email').value,
        password: document.getElementById('password').value,
    }
    const response = await fetch('http://localhost:8080/api/auth/authenticate', {
        method: 'POST',
        credentials: "include",
        body: JSON.stringify(login),
        headers: {
            'Content-Type': 'application/json'
        }
    });
    if (response.ok) {
        console.log('success');
    }
});