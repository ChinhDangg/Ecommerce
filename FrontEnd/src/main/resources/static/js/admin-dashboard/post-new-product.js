/* POST */
import {
    data_productLineImages,
    data_productLineDescriptionImages,
    data_allProductImages,
    data_allProductDescriptionImages,
    products, removeProductInfo,
} from "./add-new-product.js";


export async function getProductLineInfo() {
    const productLineNameInput = document.getElementById('product-line-name-input');
    const productLineName = productLineNameInput.value.trim();
    if (!productLineName)
        return null;
    const allMediaEntries = document.getElementById('product-line-images').querySelectorAll('.image-entry');
    const productLineImageContents = await getMediaContent(data_productLineImages, allMediaEntries);
    const allDescriptionEntries =
        Array.from(document.getElementById('product-line-descriptions').querySelectorAll('.description-entry')).slice(1);
    const descriptionContent = await getDescriptionContent(data_productLineDescriptionImages, allDescriptionEntries);
    return {
        name: productLineName,
        media: productLineImageContents,
        descriptions: descriptionContent
    };
}

async function getMediaContent(dataImageArray, allMediaTemplateEntries) {
    const mediaNames = dataImageArray.length > 0 ? await uploadImages(dataImageArray) : [];
    if (mediaNames == null) { // null - fail uploading images
        return null;
    }
    const mediaContent = [];
    allMediaTemplateEntries.forEach((entry, index) => {
        mediaContent.push(
            {
                id: entry.dataset.mediaId,
                contentType: 'IMAGE',
                content: mediaNames[index]
            }
        );
    });
    return mediaContent;
}

async function getDescriptionContent(dataImageArray, allDescriptionEntries) {
    let descriptionImageNames = dataImageArray.length > 0 ? await uploadImages(dataImageArray) : [];
    const descriptionTexts = [];
    const filteredDescriptionImages = descriptionImageNames.filter(item => item !== undefined && item !== null);
    allDescriptionEntries.forEach(descriptionEntry => {
        const image = descriptionEntry.querySelector('img');
        if (image.src && image.alt !== "empty") {
            descriptionTexts.push(
                {
                    id: descriptionEntry.dataset.descriptionId,
                    contentType: 'IMAGE',
                    content: filteredDescriptionImages.shift()
                }
            );
        }
        else {
            const descriptionTextValue = descriptionEntry.querySelector('textarea').value.trim();
            if (descriptionTextValue) {
                descriptionTexts.push(
                    {
                        id: descriptionEntry.dataset.descriptionId,
                        contentType: 'TEXT',
                        content: descriptionTextValue
                    }
                );
            }
        }
    });
    return descriptionTexts;
}


export async function getProductInfo(productLineId, productId) {
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

    const allMediaEntries = productContainer.querySelector('.product-images').querySelectorAll('.image-entry');
    const productImageContents = await getMediaContent(data_allProductImages.get(productId), allMediaEntries);

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
        price: productContainer.querySelector('.product-regular-price-input').value,
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

export function initializePost() {
    document.getElementById('publish-btn').addEventListener('click', async function () {
        try {
            const productLineInfo = await getProductLineInfo();
            if (productLineInfo) {
                const productLineId = await postProductLineInfo(productLineInfo);
                await uploadProducts(productLineId);
            } else {
                await uploadProducts(null);
            }
            clearProductLineSection();
            clearAllProductInfo();
        } catch (error) {
            console.error(error);
            console.error('Fail to publish all info');
        }
    });
}

async function uploadProducts(productLineId) {
    try {
        for (let i = 1; i < products.length; i++) {
            const productId = products[i];
            const productInfo = await getProductInfo(productLineId, productId);
            console.log(`Product info for ${productId}: `, productInfo);
            if (productInfo) {
                await postProductInfo(productInfo);
            } else {
                break;
            }
        }
    } catch (error) {
        console.error('Fail to upload all products');
        throw error;
    }
}

export function clearProductLineSection() {
    data_productLineImages.length = 0;
    data_productLineDescriptionImages.length = 0;
    document.getElementById('product-line-name-input').value = '';
    document.getElementById('product-line-images').innerHTML = '';
    // Remove all description entries except the first one
    Array.from(document.getElementById('product-line-descriptions')
        .querySelectorAll('.description-entry')).slice(1).forEach(item => item.remove());
}

export function clearAllProductInfo() {
    products.forEach(productId => {
        if (productId !== 0)
            removeProductInfo(productId);
    });
}

// CRUD operations
// product line POST
export async function postProductLineInfo(productLineInfoData) {
    const response = await fetch('http://localhost:8080/api/productLine', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(productLineInfoData)
    });
    if (!response.ok) {
        throw new Error('Failed uploading product line info');
    }
    return response.text();
}

// product group POST
export async function postProductInfo(productInfoData) {
    const response = await fetch('http://localhost:8080/api/product', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(productInfoData)
    });
    if (!response.ok) {
        throw new Error('Failed uploading product info');
    }
    return response.text();
}

async function uploadImages(dataImageArray) {
    if (dataImageArray.length === 0)
        return dataImageArray;

    const formData = new FormData();
    // Store indexes of image Files to update later
    const fileIndexes = [];

    dataImageArray.forEach((image, index) => {
        if (image instanceof File) {
            formData.append('images', image);
            fileIndexes.push(index);
        }
    });

    if (fileIndexes.length === 0) {
        return dataImageArray;
    }

    const response = await fetch('http://localhost:8080/api/product/uploadImages', {
        method: 'POST',
        body: formData,
    });

    if (response.status !== 201) {
        console.error('Failed to upload images');
        throw new Error('Fail uploading images');
    }
    const uploadedNames = await response.json(); // array of image names
    // Replace File entries in original array with returned image names
    fileIndexes.forEach((fileIndex, i) => {
        dataImageArray[fileIndex] = uploadedNames[i];
    });
    return dataImageArray;
}