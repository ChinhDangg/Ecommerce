import {cartKey} from "./cart.js";

async function loadUserCart() {
    const response = await fetch('http://localhost:8080/api/user/cart');
    if (response.ok) {
        const cartInfo = await response.json();
        displayCartProduct(cartInfo);
    } else if (response.status === 401) { // not login / unauthorized
        console.log("Not authorized yet");
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
                displayCartProduct(localCartInfo);
            } else {
                console.error('Fail to load local cart info');
            }
        } else {
            console.log("No local cart");
        }
    } else {
        console.error('Fail to load user cart');
    }
}

function displayCartProduct(content) {
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
        productItemContainer.appendChild(productItem);
    });
}

loadUserCart();