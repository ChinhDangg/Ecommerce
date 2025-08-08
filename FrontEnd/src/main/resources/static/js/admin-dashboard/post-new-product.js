/* POST */
import {
    data_productLineImages,
    data_productLineDescriptionImages,
    data_allProductImages,
    data_allProductDescriptionImages,
    products,
    removeProductInfo,
} from "./add-new-product.js";


export function initializePost() {
    document.getElementById('publish-btn').addEventListener('click', async function () {
        try {
            if (products.length < 2) {
                console.log(products);
                alert('No product found to add');
                return;
            }

            const productLineInfo = await getProductLineInfo();
            const productInfos = [];

            let allProductFilled = true;
            const productIds = products.slice(1);
            for (const productId of productIds) {
                const productInfo = await getProductInfo(null, productId);
                if (productInfo) {
                    productInfos.push(productInfo);
                } else {
                    allProductFilled = false;
                    break;
                }
            }

            if (!(productInfos && allProductFilled))
                return;

            const ids = await postAllProductInfo(productLineInfo, productInfos);
            if (ids) {
                const maxTotalLengthExpected = productInfos.length + 1;
                let gotProductLineId = null;
                let gotFirstProductId = null;
                if (ids.length === maxTotalLengthExpected) {
                    gotProductLineId = ids[0];
                    gotFirstProductId = ids[1];
                    window.location.href = `http://localhost:8081/admin/dashboard?query=updateProduct&line=${gotProductLineId}&product=${gotFirstProductId}`;
                } else if (ids.length === maxTotalLengthExpected - 1) {
                    gotFirstProductId = ids[0];
                    window.location.href = `http://localhost:8081/admin/dashboard?query=updateProduct&product=${gotFirstProductId}`;
                } else {
                    alert('Failed to publish all info');
                    console.error('Fail to publish all info');
                }
            }
        } catch (error) {
            console.error(error);
            console.error('Fail to publish all info');
        }
    });
}

export async function getProductLineInfo(productLineId = null) {
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
        id: parseInt(productLineId),
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
                id: parseInt(entry.dataset.mediaId),
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
                    id: parseInt(descriptionEntry.dataset.descriptionId),
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
                        id: parseInt(descriptionEntry.dataset.descriptionId),
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
        id: parseInt(productId),
        productLineId: parseInt(productLineId),
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
            valueOption: select.value,
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
            valueOption: select.value,
        })
    })
    return specContent.length === 0 ? [] : specContent;
}

export function clearAllProductInfo() {
    products.forEach(productId => {
        if (productId !== 0)
            removeProductInfo(productId);
    });
}

// CRUD operations
export async function postAllProductInfo(productLineInfoData, productInfoDataList) {
    const productInfoWrapper = {
        productLineDTO: productLineInfoData,
        productDTOList: productInfoDataList
    }
    const response = await fetch('http://localhost:8080/api/productWrapper', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(productInfoWrapper)
    });
    if (!response.ok) {
        throw new Error('Failed uploading all products info');
    }
    return await response.json();
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