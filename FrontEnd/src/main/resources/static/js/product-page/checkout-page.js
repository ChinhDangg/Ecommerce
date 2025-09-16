
window.onload = async function () {
    const result = await getUserCheckout();
    if (!result) {
        console.log('Failed to get checkout info, redirect next - maybe to cart')
    }
}

async function getUserCheckout() {
    const response = await fetch('http://localhost:8080/api/order/checkout');
    if (!response.ok) {
        console.log('Failed to get checkout info');
        return false;
    }
    const data = await response.json();
    if (data === null) {
        console.log('No checkout info found');
        return false;
    }
    if (!data.productInfo.productList.length) {
        console.log('No productInfo found');
        return false;
    }

    displayUserInfo(data);
    displayOrderSummary(data.productInfo);
    displayAllItems(data.productInfo.productList);

    return true;
}

async function displayUserInfo(orderInfo) {
    document.getElementById('user-display-name').innerText = orderInfo.displayName;
    document.getElementById('user-address').innerText = orderInfo.address;
}

async function displayOrderSummary(orderInfo) {
    const item = orderInfo.totalQuantity > 1 ? 'items' : 'item';
    document.getElementById('order-num-item').innerText = `(${orderInfo.totalQuantity} ${item})`
    document.getElementById('price-before-tax').innerText = '$' + orderInfo.totalPrice;
    document.getElementById('tax-amount').innerText = '$' + orderInfo.taxAmount;
    document.getElementById('price-after-tax').innerText = '$' + orderInfo.priceAfterTax;
    document.getElementById('price-before-tax').classList.remove('hidden');
    const orderContainer = document.getElementById('order-summary-container');
    orderContainer.querySelector('.shipping-section').classList.remove('hidden');
    orderContainer.querySelector('.tax-section').classList.remove('hidden');
    orderContainer.querySelector('.total-section').classList.remove('hidden');
    orderContainer.querySelector('.check-out-btn').classList.remove('hidden');
}

async function removeOrderSummary() {
    const orderContainer = document.getElementById('order-summary-container');
    document.getElementById('price-before-tax').classList.add('hidden');
    document.getElementById('order-num-item').innerText = '(0 item)';
    orderContainer.querySelector('.shipping-section').classList.add('hidden');
    orderContainer.querySelector('.tax-section').classList.add('hidden');
    orderContainer.querySelector('.total-section').classList.add('hidden');
    orderContainer.querySelector('.check-out-btn').classList.add('hidden');
}

async function displayAllItems(items) {
    items.forEach((item) => {
        addItemDisplay(item);
    });
}

const mediaURL = 'http://localhost:8080/media';

async function addItemDisplay(item) {
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
    if (item.maxQuantity > 0) {
        productItem.querySelector('.out-stock-label').remove();
        const quantitySelect = productItem.querySelector('#quantity-select');
        productItem.querySelector('label[for="quantity-select"]')
            .setAttribute('for', `quantity-select-${item.id}`);
        quantitySelect.id = `quantity-select-${item.id}`;
        quantitySelect.innerHTML = '';
        for (let i = 0; i < item.maxQuantity; i++) {
            const option = document.createElement('option');
            const index = (i + 1) + '';
            option.value = index;
            option.textContent = index;
            quantitySelect.appendChild(option);
        }
        quantitySelect.value = item.quantity;
        quantitySelect.addEventListener('change', async function(event) {
            await updateCartQuantity(item.id, event.target.value);
        });
    } else {
        productItem.querySelector('label[for="quantity-select"]').remove();
        productItem.querySelector('#quantity-select').remove();
        productItem.querySelector('.in-stock-label').remove();
    }
    productItemContainer.appendChild(productItem);
}