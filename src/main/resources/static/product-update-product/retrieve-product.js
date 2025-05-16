import {
    data_productLineImages,
    data_productLineDescriptionImages,
    data_allProductImages,
    data_allProductDescriptionImages,
    products,
    initializeImageButtons,
    addProductLineDescription
} from "./add-new-product.js";

document.getElementById('search-bar-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const searchInput = document.getElementById('search-input');
    console.log(searchInput.value);
});

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

async function searchProduct(productName) {

}

const productLineInfo = await fetchProductLine();
displayProductLineInfo(productLineInfo);