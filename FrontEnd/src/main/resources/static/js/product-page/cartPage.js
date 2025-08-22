import {cartKey, showCartTotal} from "./cart.js";

let localLoaded = false;
async function loadUserCart() {
    const response = await fetch('http://localhost:8080/api/user/cart');
    try {
        if (response.ok) {
            const cartInfo = await response.json();
            if (cartInfo.productList.length) {
                displayCartProduct(cartInfo.productList);
                displayOrderSummary(cartInfo);
                localLoaded = false;
                return;
            } else
                removeOrderSummary();
        } else if (response.status === 401) { // not login / unauthorized
            if (localStorage.getItem(cartKey)) {
                const cartInfo = JSON.parse(localStorage.getItem(cartKey));
                console.log(cartInfo);
                const getLocal = await fetch('http://localhost:8080/api/product/cart', {
                    method: 'POST',
                    body: JSON.stringify(cartInfo),
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
                if (getLocal.ok) {
                    const localCartInfo = await getLocal.json();
                    if (localCartInfo.productList.length) {
                        displayCartProduct(localCartInfo.productList);
                        displayOrderSummary(localCartInfo);
                        localLoaded = true;
                        return;
                    }
                } else {
                    console.error('Fail to load local cart info');
                }
            } else {
                console.log('No local cart info');
            }
        }
    } catch (error) {
    }
    removeOrderSummary();
}

function displayCartProduct(content) {
    showEmptyCart(false);
    const mediaURL = document.getElementById('media-url').innerText;
    const cardPageURL = document.getElementById('cardPage-url').innerText;
    const productItemContainer = document.getElementById('product-item-container');
    productItemContainer.classList.remove('hidden');
    const productItemTem = productItemContainer.querySelector('.product-item');
    content.forEach((item) => {
        const productItem = productItemTem.cloneNode(true);
        productItem.classList.remove('hidden');
        productItem.dataset.productId = item.id;
        productItem.querySelectorAll('.product-link').forEach(link => {
            link.href = `${cardPageURL}/${item.id}`;
        });
        productItem.querySelector('.product-img').src = `${mediaURL}/${item.imageName}`;
        productItem.querySelector('.product-name').innerHTML = item.name;
        productItem.querySelector('.product-id').innerHTML = `CPN # ${item.id} | MFR # ${item.manufacturerId}`;
        if (item.discountedPrice) {
            productItem.querySelector('.sale-price').innerHTML =
                '$' + Number(item.discountedPrice).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
            productItem.querySelector('.price').innerHTML =
                '$' + Number(item.price).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
        } else {
            productItem.querySelector('.sale-price').innerHTML =
                '$' + Number(item.price).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
            productItem.querySelector('.price').remove();
        }
        if (item.productOptions.length) {
            const optionContainer = productItem.querySelector('.option-container');
            const optionItemTem = optionContainer.querySelector('.option-item');
            item.productOptions.forEach(option => {
                const optionItem = optionItemTem.cloneNode(true);
                optionItem.classList.remove('hidden');
                optionItem.querySelector('.option-item-name').innerHTML = option.name;
                optionItem.querySelector('.option-item-value').innerHTML = option.valueOption;
                optionContainer.appendChild(optionItem);
            });
            optionItemTem.remove();
        }
        if (item.quantity > 0) {
            productItem.querySelector('.out-stock-label').remove();
            const quantitySelect = document.getElementById('quantity-select');
            quantitySelect.innerHTML = '';
            for (let i = 0; i < item.quantity; i++) {
                const option = document.createElement('option');
                const index = (i + 1) + '';
                option.value = index;
                option.textContent = index;
                quantitySelect.appendChild(option);
            }
        } else {
            productItem.querySelector('.in-stock-label').remove();
        }
        productItem.querySelector('.remove-cart-btn').addEventListener('click', async function() {
            await removeFromCart(item.id);
            productItem.remove();
        });
        productItemContainer.appendChild(productItem);
    });
}

function displayOrderSummary(cartInfo) {
    const item = cartInfo.totalQuantity > 1 ? 'items' : 'item';
    document.getElementById('order-num-item').innerText = `(${cartInfo.totalQuantity} ${item})`
    document.getElementById('price-before-tax').innerText = '$' + cartInfo.totalPrice;
    document.getElementById('tax-amount').innerText = '$' + cartInfo.taxAmount;
    document.getElementById('price-after-tax').innerText = '$' + cartInfo.priceAfterTax;
}

function removeOrderSummary() {
    const orderContainer = document.getElementById('order-summary-container');
    document.getElementById('price-before-tax').remove();
    orderContainer.querySelector('.shipping-section').remove();
    orderContainer.querySelector('.tax-section').remove();
    orderContainer.querySelector('.total-section').remove();
    orderContainer.querySelector('.check-out-btn').remove();
}

async function removeFromCart(productId) {
    if (localLoaded) {
        removeProductFromLocalCart(productId);
    } else {
        await removeProductFromUserCart(productId);
    }
    const quantity = await showCartTotal();
    if (quantity === 0) {
        removeOrderSummary();
        showEmptyCart(true);
    }
}

function showEmptyCart(show = true) {
    if (show) {
        document.getElementById('empty-cart-container').classList.remove('hidden');
    } else {
        document.getElementById('empty-cart-container').classList.add('hidden');
    }
}

async function removeProductFromUserCart(productId) {
    const response = await fetch('http://localhost:8080/api/user/cart', {
        method: 'DELETE',
        body: JSON.stringify({productId}),
        headers: {
            'Content-Type': 'application/json'
        }
    });
    if (!response.ok) {
        throw new Error('Fail to remove product from user cart');
    }
}

function removeProductFromLocalCart(productId) {
    let cartItems = JSON.parse(localStorage.getItem(cartKey)) || [];
    cartItems = cartItems.filter(item => item.productId !== productId);
    localStorage.setItem(cartKey, JSON.stringify(cartItems));
}

loadUserCart();