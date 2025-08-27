import {cartKey, getLocalCartItem, showCartTotal, updateLocalCartItemType} from "./cart.js";

let localLoaded = false;
async function loadUserCart() {
    const response = await fetch('http://localhost:8080/api/user/cart');
    try {
        if (response.ok) {
            const cartInfo = await response.json();
            if (cartInfo.productList.length) {
                displayAllItems(cartInfo.productList);
                displayOrderSummary(cartInfo);
                localLoaded = false;
                return;
            } else
                removeOrderSummary();
        } else if (response.status === 401) { // not login / unauthorized
            if (localStorage.getItem(cartKey)) {
                const cartInfo = getLocalCartItem();
                const getLocal = await fetch('http://localhost:8080/api/product/cart', {
                    method: 'POST',
                    body: JSON.stringify(cartInfo),
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
                if (getLocal.ok) {
                    console.log('get local');
                    const localCartInfo = await getLocal.json();
                    if (localCartInfo.productList.length) {
                        displayAllItems(localCartInfo.productList);
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
        console.error(error);
    }
    removeOrderSummary();
}

const mediaURL = document.getElementById('media-url').innerText;
const cardPageURL = document.getElementById('cardPage-url').innerText;

function displayAllItems(content) {
    content.forEach(item => {
        console.log(item);
        console.log(item.itemType);
        if (item.itemType === 'CART') {
            showEmptyCart(false);
            addItemDisplayToCart(item);
        }
        else if (item.itemType === 'SAVED')
            addItemDisplayToSaved(item);
    });
}

function addItemDisplayToCart(item) {
    const productItemContainer = document.getElementById('product-item-container');
    productItemContainer.classList.remove('hidden');
    const productItem = productItemContainer.querySelector('.product-item').cloneNode(true);
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
    productItem.querySelector('.save-later-btn').addEventListener('click', async function() {
        await moveCartToSaved(item);
    });
    productItemContainer.appendChild(productItem);
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
    await getTotalCartAndUpdateLayout(localLoaded);
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

async function getTotalCartAndUpdateLayout(getLocal) {
    const quantity = await showCartTotal(getLocal);
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


async function moveCartToSaved(item) {
    if (localLoaded) {
        moveLocalCartToSaved(item.id);
    } else {
        await moveUserCartToSaved(item.id);
    }
    removeItemDisplayFromCart(item.id);
    addItemDisplayToSaved(item);
    await getTotalCartAndUpdateLayout(localLoaded);
}

async function moveUserCartToSaved(productId) {
    const response = await fetch('http://localhost:8080/api/user/cart/to-save', {
        method: 'POST',
        body: JSON.stringify({productId}),
        headers: {
            'Content-Type': 'application/json'
        }
    });
    if (!response.ok) {
        throw new Error('Fail to move product to saved');
    }
}

function moveLocalCartToSaved(productId) {
    updateLocalCartItemType(productId, false);
}

function removeItemDisplayFromCart(productId) {
    const cartItemContainer = document.getElementById('product-item-container');
    const item = cartItemContainer.querySelector(`.product-item[data-product-id="${productId}"]`);
    if (item) {
        item.remove();
    } else {
        console.error('Unable to remove item from cart');
    }
}

function addItemDisplayToSaved(item) {
    const savedItemContainer = document.getElementById('saved-item-container');
    const savedItem = savedItemContainer.querySelector('.saved-item').cloneNode(true);
    savedItem.classList.add('hidden');
    savedItem.dataset.productId = item.id;
    savedItem.classList.remove('hidden');
    savedItem.dataset.productId = item.id;
    savedItem.querySelectorAll('.product-link').forEach(link => {
        link.href = `${cardPageURL}/${item.id}`;
    });
    savedItem.querySelector('.product-img').src = `${mediaURL}/${item.imageName}`;
    savedItem.querySelector('.product-name').innerHTML = item.name;
    savedItem.querySelector('.product-id').innerHTML = `CPN # ${item.id} | MFR # ${item.manufacturerId}`;
    if (item.discountedPrice) {
        savedItem.querySelector('.sale-price').innerHTML =
            '$' + Number(item.discountedPrice).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
        savedItem.querySelector('.price').innerHTML =
            '$' + Number(item.price).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
    } else {
        savedItem.querySelector('.sale-price').innerHTML =
            '$' + Number(item.price).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
        savedItem.querySelector('.price').remove();
    }
    if (item.quantity > 0) {
        savedItem.querySelector('.out-stock-label').remove();
    } else {
        savedItem.querySelector('.in-stock-label').remove();
    }
    savedItemContainer.appendChild(savedItem);
}


function removeItemDisplayFromSaved(productId) {

}

loadUserCart();