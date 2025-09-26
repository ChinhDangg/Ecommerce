
document.getElementById('sign-out-btn').addEventListener('click', async function() {
    const response = await fetch('/api/auth/sign-out', {
        method: 'POST',
    });
    if (response.ok) {
        window.location.href = '/';
        return;
    }
    alert('Failed to sign out');
});