const cartKey = 'cart-items';

export async function updateCart(productId, quantity, addToCartBtn = null, maxQuantity = null) {
    addToCartBtn.disabled = true;
    const cartInfo = {
        productId: productId,
        quantity: quantity,
    }
    const response = await fetch('http://localhost:8080/api/user/cart', {
        method: 'POST',
        body: JSON.stringify(cartInfo),
        headers: {
            'Content-Type': 'application/json'
        }
    });
    addToCartBtn.disabled = false;
    if (response.status === 201) {
        const quantity = await response.text();
        showCartQuantity(quantity);
    } else if (response.status === 401) { // unauthorized then save to localstorage as guest
        quantity = quantity > maxQuantity ? maxQuantity : quantity;
        updateLocalCartItemQuantity(productId, quantity);
        showCartQuantity(getLocalTotalQuantity());
    }
}

function showCartQuantity(quantity) {
    quantity = parseInt(quantity);
    if (quantity <= 0) {
        document.getElementById('cart-quantity').classList.add('hidden');
        return;
    }
    else if (quantity > 99)
        quantity = '99+';
    document.getElementById('cart-quantity').innerText = quantity;
    document.getElementById('cart-quantity').classList.remove('hidden');
}

function getLocalTotalQuantity() {
    const cartItems = JSON.parse(localStorage.getItem(cartKey)) || [];
    return cartItems.reduce((sum, item) => sum + item.quantity, 0);
}

function updateLocalCartItemQuantity(productId, newQuantity) {
    let cartItems = JSON.parse(localStorage.getItem(cartKey)) || [];
    const itemIndex = cartItems.findIndex(item => item.productId === productId);

    if (itemIndex !== -1) {
        // ✅ update quantity if product exists
        cartItems[itemIndex].quantity = newQuantity;
    } else {
        // ❌ optionally add it if it doesn't exist
        cartItems.push({ productId, quantity: newQuantity });
    }
    localStorage.setItem(cartKey, JSON.stringify(cartItems));
}

async function showCartTotal() {
    const response = await fetch('http://localhost:8080/api/user/cart/total');
    if (response.ok) {
        showCartQuantity(await response.text());
    } else {
        showCartQuantity(getLocalTotalQuantity());
    }
}

showCartTotal();