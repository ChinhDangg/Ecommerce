
window.onload = function() {
    initialize();
}

async function initialize() {
    const userAddress = await getUserAddress();
    displayUserAddress(userAddress);
    document.getElementById('address-form').addEventListener('submit', async function(e){
        e.preventDefault();
        await updateAddress();
    });
}

async function getUserAddress() {
    const response = await fetch("/api/user/info/address");
    if (!response.ok) {
        console.error(response.message);
        return;
    }
    return response.json();
}

function displayUserAddress(address) {
    document.getElementById("street-input").value = address.street;
    document.getElementById("city-input").value = address.city;
    document.getElementById("state-input").value = address.state;
    document.getElementById("zip-input").value = address.zipcode;
    document.getElementById("country-input").value = address.country;
}

async function updateAddress() {
    const data = {
        "street": document.getElementById("street-input").value,
        "city": document.getElementById("city-input").value,
        "state": document.getElementById("state-input").value,
        "zipcode": document.getElementById("zip-input").value,
        "country": document.getElementById("country-input").value
    }
    const response = fetch('/api/user/info/address', {
        method: 'PUT',
        body: JSON.stringify(data),
        headers: {
            "Content-Type": "application/json"
        }
    });
    if (!response.ok) {
        console.error(response.message);
        return;
    }
    alert('Updated address successfully.');
    displayUserAddress(await response.json());
}