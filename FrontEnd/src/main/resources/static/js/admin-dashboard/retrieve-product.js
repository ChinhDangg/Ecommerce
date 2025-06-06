import {
    addProductLineDescription,
    addImageEntry,
    addProductFeature,
    updateDescriptionImage,
    addProductDescription,
    addTopCategories,
    addOptionKey,
    addOptionValue,
    addSpecificationKey,
    addSpecificationValue,
    addNewProductEntry,
    removeProductInfo,
    data_productLineImages,
    data_allProductImages,
    data_allProductDescriptionImages,
    data_productLineDescriptionImages,
    products,
    initializeAdd,
    expandCategorySection
} from './add-new-product.js';

import {
    updatePageUrl,
    updateProductQuery,
} from './admin-navigation.js';

import {
    getProductLineInfo,
    postProductLineInfo,
    getProductInfo,
    postProductInfo
} from "./post-new-product.js";


export function initializeUpdate() {

    data_productLineImages.length = 0;
    data_productLineDescriptionImages.length = 0;
    data_allProductImages.clear();
    data_allProductDescriptionImages.clear();
    products.splice(1);
    retrieved_product.length = 0;

    document.getElementById('search-bar-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const searchInput = document.getElementById('search-input');
        console.log('Search input: ', searchInput.value);
        if (searchInput.value)
            await searchProduct(searchInput.value);
        else {
            document.getElementById('main-content').classList.add('hidden');
            clearSearchEntry();
        }
    });

    document.getElementById('update-btn').addEventListener('click', async () => {
        const productLineInfo = await getProductLineInfo();
        // await postProductLineInfo(productLineInfo);
        // const productInfo = await getProductInfo(null, 1);
        // await postProductInfo(productInfo);
        const productLineId = new URLSearchParams(window.location.search).get('line');
        if (productLineId && !productLineInfo) {
            const confirmDeleteProductLine = confirm('Product line name is empty - marking as deletion - continue?');
            if (!confirmDeleteProductLine)
                return;
            const deletedProductLine = await deleteProductLine(productLineId);
            if (!deletedProductLine) {
                alert('Fail deleting product line');
                return;
            }
        }
        else if (productLineId) {
            const updatedProductLine = await updateProductLineInfo(productLineId, productLineInfo);
            if (!updatedProductLine) {
                alert('Fail updating product line - abort all');
                return;
            }
        }
        for (let j = 0; j < retrieved_product.length; j++) {
            const productInfo = await getProductInfo(productLineId, retrieved_product[j]);
            if (productInfo) {
                const updatedProduct = await updateProductInfo(retrieved_product[j], productInfo);
                if (!updatedProduct) {
                    alert('Stopping update due to Fail updating product: ' + retrieved_product[j]);
                    return;
                }
            }
        }
    });

    expandCategorySection(document.getElementById('product-category-section').querySelector('.toggle-collapse'));

    initializeAdd(); // initialize add new product
}


function displaySearchResult(content) {
    clearSearchEntry();
    content.forEach(result => {
        const productSearchContainer = document.getElementById('product-search-container');
        const searchEntry = productSearchContainer.querySelector('.search-entry').cloneNode(true);
        productSearchContainer.appendChild(searchEntry);
        searchEntry.classList.remove('hidden');
        searchEntry.querySelector('.product-name').innerHTML = result.name;
        searchEntry.querySelector('.product-image').src = result.imageName;
        searchEntry.querySelector('.product-id').innerHTML = result.id;
        searchEntry.querySelector('.manufacturer-id').innerHTML = result.manufacturerId;
        searchEntry.querySelector('.product-quantity').innerHTML = result.quantity;
        searchEntry.querySelector('.product-price').innerHTML = result.price;
        searchEntry.querySelector('.product-discounted-price').innerHTML = result.discountedPrice;

        const searchImageAnchor = searchEntry.querySelector('.product-image-anchor');
        searchImageAnchor.href = getProductLink(result.id, result.productLineId);
        searchImageAnchor.addEventListener('click', async function(e) {
            await clickOnProductResult(e, result.id, result.productLineId, result.categoryId);
        });
        const searchNameAnchor = searchEntry.querySelector('.product-name-anchor');
        searchNameAnchor.href = getProductLink(result.id, result.productLineId);
        searchNameAnchor.addEventListener('click', async function(e) {
            await clickOnProductResult(e, result.id, result.productLineId, result.categoryId);
        });
    });
}

function clearSearchEntry() {
    const productSearchContainer = document.getElementById('product-search-container');
    const allSearchEntries = productSearchContainer.querySelectorAll('.search-entry');
    Array.from(allSearchEntries).slice(1).forEach(item => item.remove()); // remove all item except first one
}

function getProductLink(productId, productLineId) {
    return `/admin/dashboard?query=${updateProductQuery}&product=${productId}&line=${productLineId}`;
}

async function clickOnProductResult(e, productId, productLineId, categoryId) {
    e.preventDefault();
    await handleProductResult(productId, productLineId, categoryId);
}

export async function handleProductResult(productId, productLineId, categoryId) {
    const newUrl = getProductLink(productId, productLineId);
    const query = `${updateProductQuery}P${productId}`;
    updatePageUrl(newUrl, query);
    clearSearchEntry();
    clearProductLineSection();
    clearAllProductInfo();
    const currentCategory = await fetchProductCategory(categoryId);
    console.log(currentCategory);
    await addTopCategories(currentCategory, false, true);
    document.getElementById('main-content').classList.remove('hidden');
    if (productLineId) {
        const productLineInfo = await fetchProductLineInfo(productLineId);
        displayProductLineInfo(productLineInfo);
        productLineInfo.productIdList.forEach(productId => {
            addProductEntry(productId);
        });
    } else {
        addProductEntry(productId);
    }
}

function clearProductLineSection() {
    data_productLineImages.length = 0;
    data_productLineDescriptionImages.length = 0;
    document.getElementById('product-line-name-input').value = '';
    document.getElementById('product-line-images').innerHTML = '';
    // Remove all description entries except the first one
    Array.from(document.getElementById('product-line-descriptions')
        .querySelectorAll('.description-entry')).slice(1).forEach(item => item.remove());
}

function displayProductLineInfo(productLineInfo) {
    document.getElementById('product-line-name-input').value = productLineInfo.name;
    const productLineImageContainer = document.getElementById('product-line-images');
    productLineInfo.media.forEach(media => {
        const imageEntry = addImageEntry(data_productLineImages, productLineImageContainer, null, media.content);
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

const retrieved_product = [];
function addProductEntry(productId) {
    const [productOptionItem, productSpecItem, productItem] = addNewProductEntry(productId,true);
    productItem.querySelector('.toggle-collapse').addEventListener('click', async () => {
        if (retrieved_product.includes(productId))
            return;
        retrieved_product.push(productId);
        const productInfo = await fetchProductInfo(productId);
        productInfo.options.forEach((option) => {
            const optionItem = addOptionKey(option.name);
            addOptionValue(optionItem, option.name, option.value);
            productOptionItem.querySelector(`select[data-option-id="${option.name}"]`).value = option.value;
        });
        productInfo.specifications.forEach((spec) => {
            const specItem = addSpecificationKey(spec.name);
            addSpecificationValue(specItem, spec.name, spec.value);
            productSpecItem.querySelector(`select[data-spec-id="${spec.name}"]`).value = spec.value;
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

function clearAllProductInfo() {
    products.forEach(productId => {
        if (productId !== 0)
            removeProductInfo(productId);
    });
}

function displayProductInfo(productItem, content) {
    console.log(content);
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
                null, media.content
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

function updateProductLineInfo(productLineId, productLineInfoData) {
    const url = 'http://localhost:8080/api/productLine/' + productLineId;
    return fetch(url, {
        method: 'PUT',
        headers: {
            'Content-Type':'application/json',
        },
        body: JSON.stringify(productLineInfoData)
    })
        .then(response => {
            if (response.status === 200) // ok
                return response.text();
            throw new Error('Fail update product line info');
        })
        .then(data => {
            console.log('Success update product line info: ', data);
            return data;
        })
        .catch(error => {
            console.error('Error updating product line info', error);
            return null;
        });
}

function updateProductInfo(productId, productInfoData) {
    const url = 'http://localhost:8080/api/product/' + productId;
    return fetch(url, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(productInfoData)
    })
        .then(response => {
            if (response.status === 200) // ok
                return response.text();
            throw new Error('Fail update product info')
        })
        .then(data => {
            console.log('Success update product info: ', data);
            return data;
        })
        .catch(error => {
            console.error('Error updating product info', error);
            return null;
        });
}

async function searchProduct(productNameSearch) {
    try {
        // const page = 0;
        // const baseUrl = 'http://localhost:8080/api/product/search';
        // const queryParams = new URLSearchParams({
        //     page: page.toString(),
        //     search: productNameSearch
        // });
        // const url = `${baseUrl}?${queryParams.toString()}`;
        // const response = await fetch(url);
        // const searchResult = await response.json();
        const searchResult = {
            content: [
                {
                    productLineId: 1,
                    categoryId: 1,
                    id: 1,
                    manufacturerId: 1,
                    name: 'Some product name to show',
                    quality: 50,
                    price: 50.5,
                    features: [],
                    imageName: "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg",
                    discountPrice: '',
                }
            ]
        };
        displaySearchResult(searchResult.content);
    } catch (error) {
        console.error('Error searching for product:', error);
    }
}

async function fetchProductLineInfo(productLineId) {
    try {
        // const url = await fetch('http://localhost:8080/api/product/productLine/' + productLineId);
        // const response = await fetch(url);
        // if (!response.ok) {
        //     throw new Error(`Failed to get product with id: ${productId}`);
        // }
        //const result = await response.json();
        return {
            name: "Product Line Name",
            media: [
                {
                    id: 1,
                    contentType: "IMAGE",
                    content: "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg"
                },
                {
                    id: 2,
                    contentType: "IMAGE",
                    content: "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg"
                }
            ],
            descriptions: [
                {
                    id: 1,
                    contentType: "IMAGE",
                    content: "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg"
                },
                {
                    id: 2,
                    contentType: "TEXT",
                    content: "Some description about the product line"
                }
            ],
            productIdList: [
                1, 2
            ]
        }
    } catch (error) {
        console.error('Error fetching product line:', error);
        return null;
    }
}

async function fetchProductCategory(productCategoryId) {
    try {
        // const url = `http://localhost:8080/api/product/category/` + productCategoryId;
        // const response = await fetch(url);
        // if (!response.ok) {
        //     throw new Error(`Failed to get product with id: ${productCategoryId}`);
        // }
        // const result = await response.json();
        return [
            {
                id: 1,
                name: 'Electronics'
            }
        ]
    } catch (error) {
        console.error('Error fetching for product category:', error);
    }
}

async function fetchProductInfo(productId) {
    try {
        // const url = `http://localhost:8080/api/product/${productId}`;
        // const response = await fetch(url);
        // if (!response.ok) {
        //     throw new Error(`Failed to get product with id: ${productId}`);
        // }
        //const result = await response.json();
        return {
            productLineId: 1,
            id: 1,
            manufacturerId: 'Manu 2',
            name: 'Product name with something',
            brand: 'Brand name',
            quantity: 45,
            conditionType: 'USED',
            categoryId: 1,
            price: '20.5',
            salePrice: '10.5',
            saleEndDate: '2025-05-19',
            options: [
                {
                    id: 1,
                    name: 'Option 1',
                    value: 'value 1'
                },
                {
                    id: 2,
                    name: 'Option 2',
                    value: 'value 2'
                }
            ],
            specifications: [
                {
                    id: 1,
                    name: 'Spec 1',
                    value: 'value 1'
                },
                {
                    id: 2,
                    name: 'Spec 2',
                    value: 'value 2'
                }
            ],
            features: [
                'feature 1', 'feature 2', 'feature 3'
            ],
            media: [
                {
                    id: 1,
                    contentType: 'IMAGE',
                    content: '/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg'
                },
                {
                    id: 2,
                    contentType: 'IMAGE',
                    content: '/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg'
                }
            ],
            descriptions: [
                {
                    id: 1,
                    contentType: 'IMAGE',
                    content: '/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg'
                },
                {
                    id: 2,
                    contentType: 'TEXT',
                    content: 'some description some description'
                }
            ]
        }
    } catch (error) {
        console.error('Error fetching for product:', error);
    }
}

async function deleteProductLine(productLineId) {
    const url = `http://localhost:8080/api/productLine/${productLineId}`;
    return fetch(url, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.status === 204) {// no content
                console.log('Success deleting product line');
                return true;
            }
            throw new Error('Fail deleting product line');
        })
        .catch(error => {
            console.error('Error deleting product line', error);
            return false;
        });
}

async function deleteProduct(productId) {
    const url = `http://localhost:8080/api/product/${productId}`;
    return fetch(url, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.status === 204) { // no content
                console.log('Success deleting product');
                return true;
            }
            throw new Error('Fail deleting product');
        })
        .catch(error => {
            console.error('Error deleting product', error);
            return false;
        });
}