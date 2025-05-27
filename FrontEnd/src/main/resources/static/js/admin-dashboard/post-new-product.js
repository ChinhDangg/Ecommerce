/* POST */
import {
    data_productLineImages,
    data_productLineDescriptionImages,
    data_allProductImages,
    data_allProductDescriptionImages,
    products,
} from "./add-new-product.js";

function uploadImages(dataImageArray) {
    const formData = new FormData();
    dataImageArray.forEach(image => {
        formData.append('images', image);
    })
    return fetch('http://localhost:8080/api/product/uploadImages', {
        method: 'POST',
        body: formData,
    })
        .then(response => {
            if (response.status === 201) // created
                return response.json(); // return list of image names
            throw new Error('Fail upload images');
        })
        .then(data => {
            console.log('Success upload images: ', data);
            return data;
        })
        .catch(error => {
            console.error('Error uploading files: ', error);
            return null;
        })
}

// product line POST
function postProductLineInfo(productLineInfoData) {
    const url = 'http://localhost:8080/api/product/newProductLine';
    return fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(productLineInfoData)
    })
        .then(response => {
            if (!response.ok)
                throw new Error('Fail upload product line info');
            return response.text();
        })
        .then(data => {
            console.log('Success upload product line info');
            return data;
        })
        .catch(error => {
            console.error('Error uploading product line info', error);
            return null;
        });
}

async function getProductLineInfo() {
    const productLineNameInput = document.getElementById('product-line-name-input');
    const productLineName = productLineNameInput.value.trim();
    if (!productLineName)
        return null;
    const productLineImageNames = data_productLineImages.length > 0 ? await uploadImages(data_productLineImages) : [];
    const productLineImageContents = [];
    productLineImageNames.forEach(name => {
        productLineImageContents.push({
            contentType: 'IMAGE',
            content: name,
        });
    })
    const allDescriptionEntries =
        Array.from(document.getElementById('product-line-descriptions').querySelectorAll('.description-entry')).slice(1);
    const descriptionContent = await getDescriptionContent(data_productLineDescriptionImages, allDescriptionEntries);
    return {
        name: productLineName,
        media: productLineImageContents,
        descriptions: descriptionContent
    };
}

async function getDescriptionContent(dataImageArray, allDescriptionEntries) {
    const descriptionImageNames = dataImageArray.length > 0 ? await uploadImages(dataImageArray) : [];
    const descriptionTexts = [];
    allDescriptionEntries.forEach(descriptionEntry => {
        const image = descriptionEntry.querySelector('img');
        if (image.src && image.alt !== "empty") {
            descriptionTexts.push(
                {
                    type: "IMAGE",
                    content: descriptionImageNames.shift()
                }
            );
        }
        else {
            const descriptionTextValue = descriptionEntry.querySelector('textarea').value.trim();
            if (descriptionTextValue) {
                descriptionTexts.push(
                    {
                        type: "TEXT",
                        content: descriptionTextValue
                    }
                );
            }
        }
    });
    return descriptionTexts;
}


// product group POST
function postProductInfo(productInfoData) {
    const url = 'http://localhost:8080/api/product/newProduct';
    return fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(productInfoData)
    })
        .then(response => {
            if (response.status === 201) //created
                return response.text();
            throw new Error('Fail upload product info');
        })
        .then(data => {
            console.log('Success upload product info: ', data);
            return data;
        })
        .catch(error => {
            console.error('Error uploading product info', error);
            return null;
        });
}

async function getProductInfo(productLineId, productId) {
    const productGroupContainer = document.getElementById('product-group-container');
    const productContainer = productGroupContainer.querySelector(`[data-product-id="${productId}"]`);
    if (!productContainer)
        return null;
    if (!checkProductInfoFilled(productId))
        return null;
    const productFeatures = [];
    productContainer.querySelectorAll('.product-feature-input').forEach(input => {
        const featureContent = input.value.trim();
        if (featureContent)
            productFeatures.push(featureContent);
    });
    const productImageNames = data_allProductImages.get(productId).length > 0 ? await uploadImages(data_allProductImages.get(productId)) : [];
    const productImageContents = [];
    productImageNames.forEach(name => {
        productImageContents.push({
            contentType: 'IMAGE',
            content: name,
        });
    })
    const allDescriptionEntries = Array.from(productContainer.querySelectorAll('.description-entry')).slice(1);
    const productDescriptionContent = await getDescriptionContent(data_allProductDescriptionImages.get(productId), allDescriptionEntries);
    return {
        productLineId: productLineId,
        name: productContainer.querySelector('.product-name-input').value,
        brand: productContainer.querySelector('.product-brand-input').value,
        manufacturerId: productContainer.querySelector('.product-manufacturer-part-number-input').value,
        quantity: productContainer.querySelector('.product-quantity-input').value,
        conditionType: productContainer.querySelector('.product-condition-select').value,
        categoryId: document.querySelector('input[name="category"]:checked')?.id.replace("category-", ""),
        regularPrice: productContainer.querySelector('.product-regular-price-input').value,
        salePrice: productContainer.querySelector('.product-sale-price-input').value,
        saleEndDate: productContainer.querySelector('.product-sale-end-date-input').value,
        options: getProductOptionContent(productId),
        specifications: getProductSpecificationContent(productId),
        features: productFeatures,
        media: productImageContents,
        descriptions: productDescriptionContent
    };
}

function checkProductInfoFilled(productId) {
    const productGroupContainer = document.getElementById('product-group-container');
    const productContainer = productGroupContainer.querySelector(`[data-product-id="${productId}"]`);
    if (!productContainer.querySelector('.product-name-input').value) {
        alert('Product name is required');
        return false;
    } else if (!productContainer.querySelector('.product-brand-input').value) {
        alert('Product brand is required');
        return false;
    } else if (!productContainer.querySelector('.product-manufacturer-part-number-input').value) {
        alert('Product manufacturer is required');
        return false;
    } else if (!productContainer.querySelector('.product-quantity-input').value) {
        alert('Product quantity is required');
        return false;
    } else if (!productContainer.querySelector('.product-condition-select').value) {
        alert('Product condition is required');
        return false;
    } else if (!document.querySelector('input[name="category"]:checked')?.id.replace("category-", "")) {
        alert('Product category is required');
        return false;
    } else if (!productContainer.querySelector('.product-regular-price-input').value) {
        alert('Product price is required');
        return false;
    }
    return true;
}

function getProductOptionContent(productId) {
    const optionBodyContainer = document.getElementById('options-body');
    const productOptionItem = optionBodyContainer.querySelector(`[data-product-id="${productId}"]`);
    if (!productOptionItem)
        return null;
    const optionContent = [];
    productOptionItem.querySelectorAll('select').forEach(select => {
        optionContent.push({
            name: select.dataset.optionId,
            value: select.value,
        });
    })
    return optionContent.length === 0 ? [] : optionContent;
}

function getProductSpecificationContent(productId) {
    const specBodyContainer = document.getElementById('spec-body');
    const productSpecItem = specBodyContainer.querySelector(`[data-product-id="${productId}"]`);
    if (!productSpecItem)
        return null;
    const specContent = [];
    productSpecItem.querySelectorAll('select').forEach(select => {
        specContent.push({
            name: select.dataset.specId,
            value: select.value,
        })
    })
    return specContent.length === 0 ? [] : specContent;
}

function initializeEventListeners() {
    document.getElementById('publish-btn').addEventListener('click', async function () {
        const productLineInfo = await getProductLineInfo();
        let productLineId = null;
        console.log('Product line info: ', productLineInfo);
        if (productLineInfo)
            productLineId = await postProductLineInfo(productLineInfo);
        async function processProducts() {
            for (const productId of products) {
                const productInfo = await getProductInfo(productLineId, productId);
                console.log(`Product info for ${productId}: `, productInfo);
                if (productInfo) {
                    await postProductInfo(productInfo);
                } else {
                    break;
                }
            }
        }
        await processProducts();
    });
}