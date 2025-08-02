async function initiate() {
    initializeProductInfoTabs();

    const productId = document.getElementById('product-id').innerText;
    document.getElementById('product-id').remove();
    const productInfo = await fetchProductInfo(productId);
    showProductDetails(productInfo);
}

document.addEventListener("DOMContentLoaded", async () => {
    window.addEventListener("popstate", async () => {
        await initiate();
    });
    await initiate();
});

async function fetchProductInfo(productId) {
    if (!productId) {
        throw new Error('No product id found.');
    }
    const response = await fetch(`http://localhost:8080/api/productWrapper/card/${productId}`);
    if (!response.ok) {
        throw new Error('Failed to fetch product');
    }
    return await response.json();
}

function showProductDetails(productInfo) {

    showCategoryChainLinks(productInfo.productCategoryChain);

    document.getElementById('product-title').innerHTML = productInfo.name;
    document.getElementById('manufacturer-id').innerHTML = 'MFR # ' + productInfo.manufacturerId;

    showProductMedia(productInfo.media);

    //quantity
    if (productInfo.quantity > 0) {
        document.getElementById('in-stock-label').classList.remove('hidden');
        document.getElementById('out-stock-label').classList.add('hidden');
    }

    // pricing
    showProductPrice(productInfo);

    showProductConfig(productInfo.options);
    addDifferentProductConfig(productInfo.productGroupedOptions);

    const featureListContainer = document.getElementById('feature-list-container');
    const featureItemTem = featureListContainer.querySelector('.feature-item');
    productInfo.features.forEach(feature => {
        const featureItem = featureItemTem.cloneNode(true);
        featureItem.classList.remove('hidden');
        featureItem.querySelector('.feature').innerHTML = feature;
        featureListContainer.appendChild(featureItem);
    });

    if (productInfo.quantity < 10) {
        const quantitySelect = document.getElementById('quantity-select');
        quantitySelect.innerHTML = '';
        for (let i = 0; i < productInfo.quantity; i++) {
            const option = document.createElement('option');
            const index = (i + 1) + '';
            option.value = index;
            option.textContent = index;
            quantitySelect.appendChild(option);
        }
    }

    populateProductDescription(productInfo.descriptions);

    populateProductSpecification(productInfo.specifications);

    showContentTab(document.getElementById('description-tab'));
}

function showCategoryChainLinks(productCategoryChain) {
    const categoryNavContainer = document.getElementById('category-nav-container');
    const linkTem = categoryNavContainer.querySelector('.link-list');
    const fSlashTem = categoryNavContainer.querySelector('.forward-slash');
    productCategoryChain.forEach((category, index) => {
        const link = linkTem.cloneNode(true);
        link.querySelector('a').innerHTML = category.name;
        categoryNavContainer.appendChild(link);
        if (index !== productCategoryChain.length - 1) {
            categoryNavContainer.appendChild(fSlashTem.cloneNode(true));
        }
        link.querySelector('a').href = `http://localhost:8081/product/search?q=Category - ${category.name}&cateId=${category.id}`;
    });
}

function showProductPrice(productInfo) {
    const priceSaleRemainingDay = getDayDifference(productInfo.saleEndDate);
    if (priceSaleRemainingDay < 0) {
        document.getElementById('sale-price').innerHTML = '$' + productInfo.price.toFixed(2);
        document.getElementById('price').classList.add('hidden');
        document.getElementById('saved-price').classList.add('hidden');
        document.getElementById('price-end-date').classList.add('hidden');
    } else {
        document.getElementById('sale-price').innerHTML = productInfo.salePrice;
        document.getElementById('price').innerHTML = '$' + productInfo.price;
        document.getElementById('saved-price').innerHTML = `Save $${(productInfo.price - productInfo.salePrice)}`;
        const day = priceSaleRemainingDay > 1 ? 'days' : 'day';
        document.getElementById('price-end-date').innerHTML =
            `Special pricing ends in ${priceSaleRemainingDay} ${day}`;
    }
}

let currentClickedThumbnail = null;
let currentShowMainImage = null;
function showProductMedia(productMedia) {
    const mainImageContainer = document.getElementById('main-image-container');
    const mainImageItemTem = mainImageContainer.querySelector('.main-image');
    const thumbnailContainer = document.getElementById('thumbnail-container');
    const thumbnailItemTem = thumbnailContainer.querySelector('.thumbnail-item');
    let count = 0;
    productMedia.forEach(media => {
        const mainImageItem = mainImageItemTem.cloneNode(true);
        const thumbnailItem = thumbnailItemTem.cloneNode(true);
        mainImageItem.src = media.content;
        thumbnailItem.querySelector('img').src = media.content;
        if (count < 5) {
            if (count === 0) {
                mainImageItem.classList.remove('hidden');
                thumbnailItem.classList.add('border-2', 'border-blue-600');
                currentShowMainImage = mainImageItem;
                currentClickedThumbnail = thumbnailItem;
            }
            thumbnailItem.classList.remove('hidden');
        }
        thumbnailItem.addEventListener('click',function () {
            clickOnThumbnail(this, mainImageItem, count, productMedia.length);
        });
        mainImageContainer.appendChild(mainImageItem);
        thumbnailContainer.appendChild(thumbnailItem);
        count++;
    });
    updateImageCountIndication(1, count);
}

function clickOnThumbnail(clickedThumbnail, mainImageItem, currentCount, maxCount) {
    currentShowMainImage.classList.add('hidden');
    currentClickedThumbnail.classList.remove('border-2', 'border-blue-600');
    mainImageItem.classList.remove('hidden');
    clickedThumbnail.classList.add('border-2', 'border-blue-600');
    currentShowMainImage = mainImageItem;
    currentClickedThumbnail = clickedThumbnail;
    updateImageCountIndication(currentCount, maxCount);
}

document.getElementById('left-image-btn').addEventListener('click', function() {
    const mainImageArray = Array.from(
        document.getElementById('main-image-container').children);
    const thumbnailArray = Array.from(
        document.getElementById('thumbnail-container').children);
    const previousIndex = mainImageArray.indexOf(currentShowMainImage) - 1;
    if (previousIndex === -2) {
        throw new Error('Null child exists');
    }
    const lastIndex = thumbnailArray.length - 1;
    if (previousIndex === 0) {
        clickOnThumbnail(thumbnailArray[lastIndex], mainImageArray[lastIndex], lastIndex, lastIndex);
    } else {
        clickOnThumbnail(thumbnailArray[previousIndex], mainImageArray[previousIndex], previousIndex, lastIndex)
    }
});

document.getElementById('right-image-btn').addEventListener('click', function() {
    const mainImageArray = Array.from(
        document.getElementById('main-image-container').children);
    const thumbnailArray = Array.from(
        document.getElementById('thumbnail-container').children);
    const nextIndex = mainImageArray.indexOf(currentShowMainImage) + 1;
    if (nextIndex === 0) {
        throw new Error('Null child exists');
    }
    const lastIndex = thumbnailArray.length - 1;
    if (nextIndex === mainImageArray.length) {
        clickOnThumbnail(thumbnailArray[1], mainImageArray[1], 1, lastIndex);
    } else {
        clickOnThumbnail(thumbnailArray[nextIndex], mainImageArray[nextIndex], nextIndex, lastIndex)
    }
});

function updateImageCountIndication(currentCount, maxCount) {
    document.getElementById('image-count-indicator').innerText = `Image ${currentCount} of ${maxCount}`;
}

function addDifferentProductConfig(productGroupedOptions) {
    productGroupedOptions.forEach((option) => {
        addProductConfig(option.name, option.valueOption, option.productId, false);
    });
}

function showProductConfig(productOptions) {
    productOptions.forEach(item => {
        addProductConfig(item.name, item.valueOption, null, true);
    });
}

function addProductConfig(name, optionValue, productId = null, selected = false) {
    const configContainer = document.getElementById('config-container');
    let configEntry = configContainer.querySelector(`.config-entry[data-config-id="${name}"]`);
    if (configEntry) {
        const existingConfigOption = configEntry.querySelector(`.config-option-btn[data-config-value="${optionValue}"]`);
        if (existingConfigOption) {
            return;
        }
    } else {
        configEntry = document.querySelector('.config-entry').cloneNode(true);
        configEntry.classList.remove('hidden');
        configEntry.dataset.configId = name;
        configEntry.querySelector('.config-title').innerText = name;
        configContainer.appendChild(configEntry);
    }
    const configOption = configEntry.querySelector('.config-option-btn').cloneNode(true);
    configOption.classList.remove('hidden');
    configOption.dataset.configValue = optionValue;
    configOption.innerText = optionValue;
    if (selected) {
        configOption.classList.remove('border', 'border-gray-500', 'hover:bg-gray-50');
        configOption.classList.add('border-2', 'border-blue-600', 'bg-blue-50', 'text-blue-600');
    }
    configEntry.querySelector('.config-option-container').appendChild(configOption);
    if (productId) {
        configOption.addEventListener('click', function() {
            window.location.href = 'http://localhost:8081/product/card/' + productId;
        });
    }
}

function populateProductDescription(productDescriptions) {
    const descriptionTab = document.getElementById('description-tab');
    const descriptionImageEntryTem = descriptionTab.querySelector('.description-image-entry');
    const descriptionTextEntryTem = descriptionTab.querySelector('.description-text-entry');
    productDescriptions.forEach(description => {
        let descriptionEntry = null;
        if (description.contentType === 'TEXT') {
            descriptionEntry = descriptionTextEntryTem.cloneNode(true);
            descriptionEntry.innerText = description.content;
        } else if (description.contentType === 'IMAGE') {
            descriptionEntry = descriptionImageEntryTem.cloneNode(true);
            descriptionEntry.src = description.content;
        }
        descriptionEntry.classList.remove('hidden');
        descriptionTab.appendChild(descriptionEntry);
    });
}

function populateProductSpecification(productSpecification) {
    const specGrid = document.getElementById('specification-grid');
    const specNameTem = specGrid.querySelector('.spec-name');
    productSpecification.forEach(spec => {
        const specName = specNameTem.cloneNode(true);
        specName.classList.remove('hidden');
        specName.innerText = spec.name;
        const specValue = document.createElement('div');
        specValue.innerText = spec.valueOption;
        specGrid.appendChild(specName);
        specGrid.appendChild(specValue);
    });
}

function showContentTab(tab) {
    const mainTab = document.getElementById('current-tab-content');
    mainTab.innerHTML = '';
    const tabClone = tab.cloneNode(true);
    tabClone.id = tab.id + '-tem';
    tabClone.classList.remove('hidden');
    mainTab.appendChild(tabClone);
}

let currentSelectedTabBtn = document.getElementById('overview-btn');
function initializeProductInfoTabs() {
    document.getElementById('overview-btn').addEventListener('click', function() {
        if (currentSelectedTabBtn === this)
            return;
        showContentTab(document.getElementById('description-tab'));
        selectThisTabButton(this);
    });
    document.getElementById('specification-btn').addEventListener('click', function() {
        if (currentSelectedTabBtn === this)
            return;
        showContentTab(document.getElementById('specification-tab'));
        selectThisTabButton(this);
    });
}

function selectThisTabButton(button) {
    currentSelectedTabBtn.classList.remove('border-blue-600', 'text-blue-600');
    currentSelectedTabBtn.classList.add('border-transparent', 'text-gray-600');
    currentSelectedTabBtn = button;
    button.classList.add('border-blue-600', 'text-blue-600');
    button.classList.remove('border-transparent', 'text-gray-600');
}

function getDayDifference(jsonDate) {
    // Parse the JSON date string into a Date object
    const givenDate = new Date(jsonDate);

    // Get the current date
    const currentDate = new Date();

    // Calculate the difference in milliseconds
    const diffInMs = givenDate - currentDate;

    // Convert milliseconds to days (1 day = 86,400,000 ms)
    const diffInDays = diffInMs / (1000 * 60 * 60 * 24);

    // Return the rounded day difference (negative if given date is in the past)
    return Math.round(diffInDays);
}
