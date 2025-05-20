import {
    data_productLineImages,
    data_productLineDescriptionImages,
    data_allProductImages,
    data_allProductDescriptionImages,
    products,
    addProductForSpec,
    addProductForOption,
    addNewProductGroupTemplate,
    initializeImageButtons,
    addProductLineDescription
} from "./add-new-product.js";

async function fetchProductLine(productLineId) {
    try {
        // const response = await fetch('http://localhost:8080/api/product/productLine/' + productLineId);
        // return await response.json();
        return {
            name: "Product Line Name",
            media: [
                {
                    contentType: "IMAGE",
                    content: "/static/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg"
                },
                {
                    contentType: "IMAGE",
                    content: "/static/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg"
                }
            ],
            descriptions: [
                {
                    contentType: "IMAGE",
                    content: "/static/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg"
                },
                {
                    contentType: "TEXT",
                    content: "Some description about the product line"
                }
            ]
        }
    } catch (error) {
        console.error('Error fetching product line:', error);
        return null;
    }
}

function displayProductLineInfo(productLineInfo) {
    document.getElementById('product-line-name-input').value = productLineInfo.name;
    const productLineImageContainer = document.getElementById('product-line-images');
    const imageEntryTemplate = document.querySelector('#image-entry-template').cloneNode(true);
    imageEntryTemplate.classList.remove('hidden');
    productLineInfo.media.forEach(media => {
        const newImageEntry = imageEntryTemplate.cloneNode(true);
        newImageEntry.querySelector('.image-entry-img').src = media.content;
        productLineImageContainer.appendChild(newImageEntry);
        data_productLineImages.push(media.content);
        initializeImageButtons(productLineImageContainer, newImageEntry, data_productLineImages);
    });
    productLineInfo.descriptions.forEach(description => {
        addProductLineDescription(description);
    });
}

async function searchProduct(productNameSearch) {
    try {
        const page = 0;
        const baseUrl = 'http://localhost:8080/api/product/search';
        const queryParams = new URLSearchParams({
            page: page.toString(),
            search: productNameSearch
        });
        const url = `${baseUrl}?${queryParams.toString()}`;
        const response = await fetch(url);
        const searchResult = await response.json();
        displaySearchResult(searchResult.content);
    } catch (error) {
        console.error('Error searching for product:', error);
    }
}

function displaySearchResult(content) {
    const productSearchContainer = document.getElementById('product-search-container');
    const allSearchEntries = productSearchContainer.querySelectorAll('.search-entry');
    Array.from(allSearchEntries).slice(1).forEach(item => item.remove()); // remove all item except first one
    content.forEach(result => {
        const searchEntry = productSearchContainer.querySelector('.search-entry').cloneNode(true);
        productSearchContainer.appendChild(searchEntry);
        searchEntry.classList.remove('hidden');
        const productName = searchEntry.querySelector('.product-name');
        productName.innerHTML = result.name;
        searchEntry.querySelector('.product-image').src = result.imageName;
        searchEntry.querySelector('.product-id').innerHTML = result.id;
        searchEntry.querySelector('.manufacturer-id').innerHTML = result.manufacturerId;
        searchEntry.querySelector('.product-quantity').innerHTML = result.quantity;
        searchEntry.querySelector('.product-price').innerHTML = result.price;
        searchEntry.querySelector('.product-discounted-price').innerHTML = result.discountedPrice;
        productName.addEventListener('click', function() {
            console.log(result.id);
            getProductInfo(result.id);
        });
        searchEntry.querySelector('.image-container').addEventListener('click', function() {
            console.log(result.id);
            getProductInfo(result.id);
        })
    });
}

document.getElementById('search-bar-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const searchInput = document.getElementById('search-input');
    console.log('Search input: ', searchInput.value);
    searchProduct(searchInput.value);
});

async function getProductInfo(productId) {
    try {
        const url = `http://localhost:8080/api/product/${productId}`;
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Failed to get product with id: ${productId}`);
        }
        //const result = await response.json();
        const result = {
            productLineId: 1,
            id: 1,
            manufacturerId: 'Manu 2',
            name: 'Product name with something',
            brand: 'Brand name',
            quantity: 45,
            conditionType: 'NEW',
            categoryId: 1,
            price: '20.5',
            salePrice: '10.5',
            saleEndDate: '05-20-2025',
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
                    content: '/static/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg'
                },
                {
                    contentType: 'IMAGE',
                    content: '/static/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg'
                }
            ],
            descriptions: [
                {
                    contentType: 'IMAGE',
                    content: '/static/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg'
                },
                {
                    contentType: 'TEXT',
                    content: 'some description some description'
                }
            ]
        }
        displayProductInfo(result);
    } catch (error) {
        console.error('Error fetching for product:', error);
    }
}

function displayProductInfo(content) {
    console.log(content);
    const optionItem = addProductForOption(content.id);

    const specItem = addProductForSpec(content.id);
    const productItem = addNewProductGroupTemplate(content.id);
}

// const productLineInfo = await fetchProductLine();
// displayProductLineInfo(productLineInfo);