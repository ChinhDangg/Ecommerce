import {
    addProductLineDescription,
    addImageEntry,
    addProductFeature,
    updateDescriptionImage,
    addProductDescription,
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
    initializeAdd
} from './add-new-product.js';

import {
    updatePageUrl,
    updateProductQuery,
} from './admin-navigation.js';

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

    document.getElementById('publish-btn').addEventListener('click', (e) => {
        clearAllProductInfo();
    });

    initializeAdd(); // initialize add new product
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
        setProductLink(searchImageAnchor, result.id);
        searchImageAnchor.addEventListener('click', async function(e) {
            await clickOnProductResult(e, this, result.id, result.productLineId);
        });
        const searchNameAnchor = searchEntry.querySelector('.product-name-anchor');
        setProductLink(searchNameAnchor, result.id);
        searchNameAnchor.addEventListener('click', async function(e) {
            await clickOnProductResult(e, this, result.id, result.productLineId);
        });
    });
}

function clearSearchEntry() {
    const productSearchContainer = document.getElementById('product-search-container');
    const allSearchEntries = productSearchContainer.querySelectorAll('.search-entry');
    Array.from(allSearchEntries).slice(1).forEach(item => item.remove()); // remove all item except first one
}

function setProductLink(anchor, productId) {
    anchor.href = `/admin/dashboard?query=${updateProductQuery}&product=${productId}`;
}

async function clickOnProductResult(e, anchor, productId, productLineId) {
    e.preventDefault();
    const newUrl = `/admin/dashboard?query=${updateProductQuery}&product=${productId}`;
    anchor.href = newUrl;
    const query = `${updateProductQuery}P${productId}`;
    updatePageUrl(newUrl, query);
    clearSearchEntry();
    clearProductLineSection();
    clearAllProductInfo();
    document.getElementById('main-content').classList.remove('hidden');
    if (productLineId) {
        const productLineInfo = await fetchProductLineInfo(productLineId);
        displayProductLineInfo(productLineInfo);
        productLineInfo.productIdList.forEach(productId => {
            console.log('adding product entry: ', productId);
            addProductEntry(productId);
        });
    } else {
        addProductEntry(productId);
    }
}

async function fetchProductLineInfo(productLineId) {
    try {
        // const response = await fetch('http://localhost:8080/api/product/productLine/' + productLineId);
        // return await response.json();
        return {
            name: "Product Line Name",
            media: [
                {
                    contentType: "IMAGE",
                    content: "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg"
                },
                {
                    contentType: "IMAGE",
                    content: "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg"
                }
            ],
            descriptions: [
                {
                    contentType: "IMAGE",
                    content: "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg"
                },
                {
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

function clearProductLineSection() {
    data_productLineImages.length = 0;
    data_productLineDescriptionImages.length = 0;
    console.log(data_productLineImages);
    console.log(data_productLineDescriptionImages);
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
        addImageEntry(data_productLineImages, productLineImageContainer, null, media.content);
    });
    productLineInfo.descriptions.forEach(description => {
        const descriptionItem = addProductLineDescription();
        const descriptionTextArea = descriptionItem.querySelector('.description-textarea-entry');
        if (description.contentType === "TEXT") {
            descriptionTextArea.innerHTML = description.content;
        } else if (description.contentType === "IMAGE") {
            updateDescriptionImage(descriptionItem, data_productLineDescriptionImages, description.content);
        }
    });
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
                    name: 'Option 1',
                    value: 'value 1'
                },
                {
                    name: 'Option 2',
                    value: 'value 2'
                }
            ],
            specifications: [
                {
                    name: 'Spec 1',
                    value: 'value 1'
                },
                {
                    name: 'Spec 2',
                    value: 'value 2'
                }
            ],
            features: [
                'feature 1', 'feature 2', 'feature 3'
            ],
            media: [
                {
                    contentType: 'IMAGE',
                    content: '/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg'
                },
                {
                    contentType: 'IMAGE',
                    content: '/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg'
                }
            ],
            descriptions: [
                {
                    contentType: 'IMAGE',
                    content: '/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg'
                },
                {
                    contentType: 'TEXT',
                    content: 'some description some description'
                }
            ]
        }
    } catch (error) {
        console.error('Error fetching for product:', error);
    }
}

const retrieved_product = [];
function addProductEntry(productId) {
    const [productOptionItem, productSpecItem, productItem] = addNewProductEntry(productId,true);
    productItem.querySelector('.toggle-collapse').addEventListener('click', async () => {
        if (retrieved_product.includes(productId))
            return;
        retrieved_product.push(productId);
        console.log(retrieved_product);
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
        if (media.contentType === 'IMAGE')
            addImageEntry(
                data_allProductImages.get(content.id),
                productItem.querySelector('.product-images'),
                null, media.content
            );
    });
    content.descriptions.forEach(description => {
        const descriptionItem = addProductDescription(productItem, content.id);
        const descriptionTextArea = descriptionItem.querySelector('.description-textarea-entry');
        if (description.contentType === "TEXT") {
            descriptionTextArea.innerHTML = description.content;
        } else if (description.contentType === "IMAGE") {
            updateDescriptionImage(descriptionItem, data_allProductDescriptionImages.get(content.id), description.content);
        }
    });
}