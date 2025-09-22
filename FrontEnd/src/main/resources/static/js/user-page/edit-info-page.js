
async function fetchUserInfo() {
    const response = await fetch('http://localhost:8080/api/user/info');
    if (!response.ok) {
        console.error(response);
        return;
    }
    return response.json();
}

const r = await fetchUserInfo();
initializeEditNameSection(r.firstname, r.lastname);
initializeEditEmailSection(r.email);
initializeEditPasswordSection();

async function updateUserInfo(url, data) {
    const response = await fetch(url, {
        method: 'PUT',
        body: JSON.stringify(data),
        headers: {
            'Content-Type': 'application/json'
        }
    });
    if (!response.ok) {
        console.error(response.message);
    }
    return response;
}

function initializeEditNameSection(firstname, lastname) {
    document.getElementById('user-name').innerText = firstname + ' ' + lastname;
    const firstNameInput = document.getElementById('user-first-name-input');
    const lastNameInput = document.getElementById('user-last-name-input');
    firstNameInput.value = firstname;
    lastNameInput.value = lastname;
    document.getElementById('user-name-edit-btn').addEventListener('click', function() {
        document.getElementById('user-name-edit-section').classList.toggle('hidden');
    });
    document.getElementById('update-name-btn').addEventListener('click', async function() {
        const data = {
            "firstname": firstNameInput.value,
            "lastname": lastNameInput.value,
        }
        const response = await updateUserInfo('/api/user/info/name', data);
        if (response.ok) {
            const name = await response.text();
            document.getElementById('user-name').innerText = name;
            firstNameInput.value = name.split(' ')[0];
            lastNameInput.value = name.split(' ')[1];
            document.getElementById('user-name-edit-section').classList.toggle('hidden');
        }
    });
}

function initializeEditEmailSection(email) {
    document.getElementById('user-email').innerText = email;
    const emailInput = document.getElementById('new-email-input');
    emailInput.value = email;
    document.getElementById('user-email-edit-btn').addEventListener('click', function() {
        document.getElementById('user-email-section').classList.toggle('hidden');
    });
    document.getElementById('update-email-btn').addEventListener('click', async function() {
        const data = {
            "email": emailInput.value,
        }
        const response = await updateUserInfo('/api/user/info/email', data);
        if (response.ok) {
            emailInput.value = await response.text();
            document.getElementById('user-email').innerText = emailInput.value;
            document.getElementById('user-email-section').classList.toggle('hidden');
        }
    });
}

function initializeEditPasswordSection() {
    document.getElementById('user-pass-edit-btn').addEventListener('click', function() {
        document.getElementById('user-pass-edit-section').classList.toggle('hidden');
    });
    document.getElementById('update-pass-btn').addEventListener('click', async function() {
        const data = {
            'password': document.getElementById('current-pass-input').value,
            'newPass': document.getElementById('new-pass-input').value,
            'confirmPass': document.getElementById('confirm-pass-input').value
        }
        const response = await updateUserInfo('/api/user/info/password', data);
        if (response.ok) {
            document.getElementById('current-pass-input').value = '';
            document.getElementById('new-pass-input').value = '';
            document.getElementById('confirm-pass-input').value = '';
            document.getElementById('user-pass-edit-section').classList.toggle('hidden');
        }
    });
}