import {
    addImageEntry,
    addNewProductEntry,
    addOptionKey,
    addOptionValue,
    addProductDescription,
    addProductFeature,
    addProductLineDescription,
    clearProductLineSection,
    addSpecificationKey,
    addSpecificationValue,
    addTopCategories,
    data_allProductDescriptionImages,
    data_allProductImages,
    data_productLineDescriptionImages,
    data_productLineImages,
    expandCategorySection,
    initializeAdd,
    products,
    removeProductInfo,
    updateDescriptionImage,
    categoryTree
} from './add-new-product.js';

import {updatePageUrl, updateProductQuery,} from './admin-navigation.js';

import {
    clearAllProductInfo,
    getAllProductInfoFromList,
    getProductLineInfo
} from "./post-new-product.js";

const mediaURL = document.getElementById('media-url').innerText;
const productLineURL = document.getElementById('productLine-url').innerText;
const productWrapperURL = document.getElementById('productWrapper-url').innerText;
const productURL = document.getElementById('product-url').innerText;
const productSearchURL = document.getElementById('productSearch-url').innerText;
const categoryURL = document.getElementById('category-url').innerText;
const categoryParentURL = document.getElementById('categoryParent-url').innerText;

export function initializeUpdate() {

    // document.getElementById('call-url').remove();

    data_productLineImages.length = 0;
    data_productLineDescriptionImages.length = 0;
    data_allProductImages.clear();
    data_allProductDescriptionImages.clear();
    products.splice(1);
    retrieved_product.length = 0;

    initializeAdd(); // initialize add new product
    initializeSearchSubmitBtn()
    initializeTopBarDiscardBtn();
    initializeTopUpdateBtn();
    initializeProductLineSection();

    expandCategorySection(document.getElementById('product-category-section').querySelector('.toggle-collapse'));
}

function initializeSearchSubmitBtn() {
    document.getElementById('search-bar-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const searchInput = document.getElementById('search-input');
        console.log('Search input: ', searchInput.value);
        if (searchInput.value)
            await searchProduct(searchInput.value);
        else {
            hideMainContent();
            clearSearchEntry();
        }
    });
}

function initializeTopBarDiscardBtn() {
    document.getElementById('discard-button').addEventListener('click',async function () {
        const confirmDelete = confirm('Are you sure you want to delete the entire product line');
        if (confirmDelete) {
            try {
                const productLineId = new URLSearchParams(window.location.search).get('line');
                const productIdList = products.slice(1);
                await deleteAllProductInfo(productLineId, productIdList);
                window.location.href = 'http://localhost:8081/admin/dashboard?query=updateProduct';
            } catch (error) {
                alert('Failed to delete the entire product line');
            }
        }
    });
}

function initializeTopUpdateBtn() {
    document.getElementById('update-btn').addEventListener('click', async function () {
        console.log('clicked update btn');
        const productLineId = new URLSearchParams(window.location.search).get('line');

        const formData = new FormData();
        const productLineInfo = getProductLineInfo(productLineId, formData);
        const updatingProductInfos = getAllProductInfoFromList(retrieved_product, productLineId, formData);
        const newIds = products.slice(1).filter(item => !existing_product.includes(item));
        const newProductInfos = getAllProductInfoFromList(newIds, productLineId, formData);

        if (productLineId && !productLineInfo) {
            const confirmDeleteProductLine = confirm('Product line name is empty - marking as deletion - continue?');
            if (!confirmDeleteProductLine)
                return;
        }

        if (productLineInfo)
            productLineInfo.productIdList = existing_product;

        if (productLineInfo || updatingProductInfos || newProductInfos) {
            try {
                const retrievedIds = await updateAllProductInfo(productLineInfo, updatingProductInfos, newProductInfos, formData);
                console.log('Updated all product info');
            } catch (error) {
                console.error(error);
                alert('Failed to update all product info');
            }
        } else {
            // check if a new category is picked
            const initialCategory = categoryTree[0].id;
            const newCategory = document.querySelector('input[name="category"]:checked')?.id.replace("category-", "");
            if (initialCategory !== newCategory) {
                try {
                    const retrievedProductIds = await updateProductCategory(products.slice(1), parseInt(newCategory));
                    console.log('Updated product category');
                } catch (error) {
                    alert('Failed to update all product category');
                }
            }
        }
    });
}

function displaySearchResult(content) {
    clearSearchEntry();
    content.forEach(result => {
        const productSearchContainer = document.getElementById('product-search-container');
        const searchEntry = productSearchContainer.querySelector('.search-entry').cloneNode(true);
        productSearchContainer.appendChild(searchEntry);
        searchEntry.classList.remove('hidden');
        searchEntry.querySelector('.product-name').innerHTML = result.name;
        searchEntry.querySelector('.product-image').src = `${mediaURL}${result.imageName}`;
        searchEntry.querySelector('.product-id').innerHTML = result.id;
        searchEntry.querySelector('.manufacturer-id').innerHTML = result.manufacturerId;
        searchEntry.querySelector('.product-quantity').innerHTML = result.quantity;
        searchEntry.querySelector('.product-price').innerHTML = result.price;
        searchEntry.querySelector('.product-discounted-price').innerHTML = result.discountedPrice;

        const searchImageAnchor = searchEntry.querySelector('.product-image-anchor');
        searchImageAnchor.href = getProductLink(result.id);
        searchImageAnchor.addEventListener('click', async function(e) {
            await clickOnProductResult(e, result.id);
        });
        const searchNameAnchor = searchEntry.querySelector('.product-name-anchor');
        searchNameAnchor.href = getProductLink(result.id);
        searchNameAnchor.addEventListener('click', async function(e) {
            await clickOnProductResult(e, result.id);
        });
    });
}

function displayNoSearchResult(content) {
    clearSearchEntry();
    const productSearchContainer = document.getElementById('product-search-container');
    const searchEntry = productSearchContainer.querySelector('.search-entry').cloneNode();
    searchEntry.innerHTML = content;
    searchEntry.classList.remove('hidden');
    showTopToolbar(false);
    productSearchContainer.appendChild(searchEntry);
}

function hideMainContent() {
    document.getElementById('main-content').classList.add('hidden');
}

function showMainContent() {
    document.getElementById('main-content').classList.remove('hidden');
}

function showTopToolbar(show) {
    if (show) {
        document.getElementById('discard-button').classList.remove('hidden');
        document.getElementById('update-btn').classList.remove('hidden');
    } else {
        document.getElementById('discard-button').classList.add('hidden');
        document.getElementById('update-btn').classList.add('hidden');
    }
}

function clearSearchEntry() {
    const productSearchContainer = document.getElementById('product-search-container');
    const allSearchEntries = productSearchContainer.querySelectorAll('.search-entry');
    Array.from(allSearchEntries).slice(1).forEach(item => item.remove()); // remove all item except first one
}

function getProductLink(productId) {
    return `/admin/dashboard?query=${updateProductQuery}&product=${productId}`;
}

async function clickOnProductResult(e, productId) {
    e.preventDefault();
    await handleProductResult(productId);
}

export async function handleProductResult(productId) {
    const newUrl = getProductLink(productId);
    const query = `${updateProductQuery}P${productId}`;
    updatePageUrl(newUrl, query);
    clearSearchEntry();
    clearProductLineSection();
    clearAllProductInfo();
    const currentCategory = await fetchProductCategory(productId);
    console.log(currentCategory);
    await addTopCategories(currentCategory, null, currentCategory[0], true);
    showMainContent();
    showTopToolbar(true);
    const productLineInfo = await fetchProductLineInfo(productId);
    if (productLineInfo.id) {
        displayProductLineInfo(productLineInfo);
    }
    productLineInfo.productIdList.forEach(productId => {
        addProductEntry(productId);
    });
}

function initializeProductLineSection() {
    const btn = document.getElementById('product-line-section').querySelector('.delete-product-btn');
    btn.innerText = 'Delete';
    btn.onclick = async function () {
        const confirmClear = confirm('Are you sure you want to delete the product line? All associated products will no longer be grouped');
        if (confirmClear) {
            try {
                const productLineId = new URLSearchParams(window.location.search).get('line');
                if (productLineId)
                    await deleteProductLine(productLineId);
                else
                    alert('No product line found to delete');
            } catch (error) {
                alert('Failed to delete product line - update fail');
            }
            clearProductLineSection();
        }
    };
}

function displayProductLineInfo(productLineInfo) {
    document.getElementById('product-line-name-input').value = productLineInfo.name;
    const productLineImageContainer = document.getElementById('product-line-images');
    productLineInfo.media.forEach(media => {
        const imageEntry = addImageEntry(
            data_productLineImages, productLineImageContainer, null, media.content
        );
        imageEntry.dataset.mediaId = media.id;
    });
    productLineInfo.descriptions.forEach(description => {
        const descriptionItem = addProductLineDescription();
        descriptionItem.dataset.descriptionId = description.id;

        const descriptionTextArea = descriptionItem.querySelector('.description-textarea-entry');
        if (description.contentType === "TEXT") {
            descriptionTextArea.innerHTML = description.content;
        } else if (description.contentType === "IMAGE") {
            updateDescriptionImage(descriptionItem, data_productLineDescriptionImages, description.content);
        }
    });
}

const existing_product = [];
const retrieved_product = [];
function addProductEntry(productId) {
    existing_product.push(productId);
    const [productOptionItem, productSpecItem, productItem] = addNewProductEntry(productId,true);
    productItem.querySelector('.toggle-collapse').addEventListener('click', async () => {
        if (retrieved_product.includes(productId))
            return;
        retrieved_product.push(productId);
        const productInfo = await fetchProductInfo(productId);
        if (!productInfo) {
            return;
        }
        productInfo.options.forEach((option) => {
            const optionItem = addOptionKey(option.name);
            addOptionValue(optionItem, option.name, option.valueOption);
            productOptionItem.querySelector(`select[data-option-id="${option.name}"]`).value = option.valueOption;
        });
        productInfo.specifications.forEach((spec) => {
            const specItem = addSpecificationKey(spec.name);
            addSpecificationValue(specItem, spec.name, spec.valueOption);
            productSpecItem.querySelector(`select[data-spec-id="${spec.name}"]`).value = spec.valueOption;
        });
        displayProductInfo(productItem, productInfo);
    });
    productItem.querySelector('.delete-product-btn').onclick = async function() {
        const deleteConfirm = confirm("Are you sure you want to delete this product?");
        if (!deleteConfirm)
            return;
        const deleted = await deleteProduct(productId);
        if (!deleted) {
            alert('Fail deleting product');
            return;
        }
        removeProductInfo(productId);
    };
}

function displayProductInfo(productItem, content) {
    productItem.querySelector('.product-name-input').value = content.name;
    productItem.querySelector('.product-brand-input').value = content.brand;
    productItem.querySelector('.product-manufacturer-part-number-input').value = content.manufacturerId;
    productItem.querySelector('.product-quantity-input').value = content.quantity;
    productItem.querySelector('.product-condition-select').value = content.conditionType;
    productItem.querySelector('.product-regular-price-input').value = content.price;
    productItem.querySelector('.product-sale-price-input').value = content.salePrice;
    productItem.querySelector('.product-sale-end-date-input').value = content.saleEndDate;
    content.features.forEach(feature => {
        const featureEntry = addProductFeature(productItem);
        featureEntry.querySelector('.product-feature-input').value = feature;
    });
    content.media.forEach(media => {
        if (media.contentType === 'IMAGE') {
            const imageEntry = addImageEntry(
                data_allProductImages.get(content.id),
                productItem.querySelector('.product-images'),
                null,
                media.content
            );
            imageEntry.dataset.mediaId = media.id;
        }
    });
    content.descriptions.forEach(description => {
        const descriptionItem = addProductDescription(productItem, content.id);
        descriptionItem.dataset.descriptionId = description.id;

        const descriptionTextArea = descriptionItem.querySelector('.description-textarea-entry');
        if (description.contentType === "TEXT") {
            descriptionTextArea.innerHTML = description.content;
        } else if (description.contentType === "IMAGE") {
            updateDescriptionImage(descriptionItem, data_allProductDescriptionImages.get(content.id), description.content);
        }
    });
}


// CRUD operations
async function updateAllProductInfo(productLineInfoData, updatingProductInfoDataList, newProductInfoDataList, formData) {
    const productInfoWrapper = {
        productLineDTO: productLineInfoData,
        updatingProductDTOList: updatingProductInfoDataList,
        newProductDTOList: newProductInfoDataList
    }
    formData.append(
        'productUpdateDTO',
        new Blob([JSON.stringify(productInfoWrapper)], { type: 'application/json' })
    );

    const response = await fetch(productWrapperURL, {
        method: 'PUT',
        body: formData
    });
    if (!response.ok) {
        throw new Error('Failed updating all product info');
    }
    return await response.json();
}

async function updateProductCategory(productIdList, categoryId) {
    const content = {
        productIds: productIdList,
        categoryId: categoryId,
    }
    const response = await fetch(categoryURL, {
        method: 'PUT',
        headers: {
            'Content-Type':'application/json',
        },
        body: JSON.stringify(content)
    });
    if (!response.ok) {
        throw new Error('Failed to update all product category');
    }
    return await response.json();
}

async function searchProduct(productNameSearch) {
    try {
        const page = 0;
        const queryParams = new URLSearchParams({
            q: productNameSearch,
            page: page.toString()
        });
        const url = `${productSearchURL}?${queryParams.toString()}`;
        const response = await fetch(url);
        if (!response.ok) {
            displayNoSearchResult('Failed searching product');
            return;
        }
        const searchResult = await response.json();
        if (searchResult.productResults.page.totalElements) {
            displaySearchResult(searchResult.productResults.content);
        } else {
            displayNoSearchResult('No result found with search');
        }
    } catch (error) {
        console.error('Error searching for product:', error);
        displayNoSearchResult('Error searching for product');
    }
}

async function fetchProductLineInfo(productId) {
    const response = await fetch(`${productWrapperURL}/${productId}`);
    if (!response.ok) {
        throw new Error('Failed to get product line with product id: ' + productId);
    }
    return await response.json();
}

async function fetchProductCategory(productId) {
    const response = await fetch(`${categoryParentURL}/${productId}`);
    if (!response.ok) {
        throw new Error(`Failed to get product category with product id: ${productId}`);
    }
    return await response.json();
}

async function fetchProductInfo(productId) {
    const response = await fetch(`${productURL}/${productId}`);
    if (!response.ok) {
        throw new Error(`Failed to get product with id: ${productId}`);
    }
    return await response.json();
}

async function deleteAllProductInfo(productLineId, productIdList) {
    const content = {
        productLineId: productLineId,
        productIdList: productIdList
    }
    const response = await fetch(productWrapperURL, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(content)
    });
    if (response.status !== 204) {
        throw new Error('Failed to delete all product info');
    }
}

async function deleteProductLine(productLineId) {
    const response = await fetch(`${productLineURL}/${productLineId}`, {
        method: 'DELETE'
    });
    if (response.status !== 204) { // no content
        throw new Error('Fail deleting product line');
    }
}

async function deleteProduct(productId) {
    const response = await fetch(`${productURL}/${productId}`, {
        method: 'DELETE'
    });
    if (response.status !== 204) { // no content
        throw new Error('Fail deleting product');
    }
}