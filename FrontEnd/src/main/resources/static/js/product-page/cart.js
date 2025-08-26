export const cartKey = 'cart-items';

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
        showCartQuantity(getLocalCartTotalQuantity());
    }
}

function showCartQuantity(quantity) {
    quantity = parseInt(quantity);
    if (quantity <= 0) {
        document.getElementById('cart-quantity').classList.add('hidden');
        return 0;
    }
    else if (quantity > 99)
        quantity = '99+';
    document.getElementById('cart-quantity').innerText = quantity;
    document.getElementById('cart-quantity').classList.remove('hidden');
    return quantity;
}

function getLocalCartTotalQuantity() {
    const cartItems = JSON.parse(localStorage.getItem(cartKey)) || [];
    return cartItems
        .filter(item => item.type === 'CART')
        .reduce((sum, item) => sum + item.quantity, 0);
}

function updateLocalCartItemQuantity(productId, newQuantity) {
    let cartItems = JSON.parse(localStorage.getItem(cartKey)) || [];
    const itemIndex = cartItems.findIndex(item => item.productId === productId);

    if (itemIndex !== -1) {
        // update quantity if product exists
        cartItems[itemIndex].quantity = newQuantity;
    } else {
        // optionally add it if it doesn't exist
        cartItems.push({ productId, quantity: newQuantity, type: 'CART' });
        console.log(cartItems);
    }
    localStorage.setItem(cartKey, JSON.stringify(cartItems));
}

export async function showCartTotal(getLocal = false) {
    if (getLocal) {
        return showCartQuantity(getLocalCartTotalQuantity());
    } else {
        const response = await fetch('http://localhost:8080/api/user/cart/total');
        if (response.ok)
            return showCartQuantity(await response.text());
        else
            return showCartQuantity(getLocalCartTotalQuantity());
    }
}

export function getLocalCartItem(getCart = true) {
    const cartInfo = JSON.parse(localStorage.getItem(cartKey)) || [];
    if (cartInfo) {
        let getWhich = getCart ? 'CART' : 'SAVED'
        return cartInfo.filter(item => item.type === getWhich);
    }
    return [];
}

export function updateLocalCartItemType(productId, cart = true) {
    let cartItems = JSON.parse(localStorage.getItem(cartKey)) || [];
    const itemIndex = cartItems.findIndex(item => item.productId === productId);
    if (itemIndex !== -1) {
        cartItems[itemIndex].type = cart ? 'CART' : 'SAVED';
        localStorage.setItem(cartKey, JSON.stringify(cartItems));
        return true;
    }
    return false;
}

showCartTotal();