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

            const formData = new FormData();
            const productLineInfo = getProductLineInfo(null, formData);
            const productInfos = getAllProductInfoFromList(products.slice(1), null, formData);

            if (!productInfos)
                return;

            const ids = await postAllProductInfo(productLineInfo, productInfos, formData);
            if (ids) {
                const maxTotalLengthExpected = productInfos.length + 1;
                let gotProductLineId = null;
                let gotFirstProductId = null;
                if (ids.length === maxTotalLengthExpected) {
                    gotProductLineId = ids[0];
                    gotFirstProductId = ids[1];
                } else if (ids.length === maxTotalLengthExpected - 1) {
                    gotFirstProductId = ids[0];
                } else {
                    alert('Failed to publish all info');
                    console.error('Fail to publish all info');
                    return;
                }
                let redirect = `http://localhost:8081/admin/dashboard?query=updateProduct&product=${gotFirstProductId}`;
                if (gotProductLineId && gotProductLineId !== -1)
                    redirect += redirect + `&line=${gotProductLineId}`;
                window.location.href = redirect
            }
        } catch (error) {
            console.error(error);
            console.error('Fail to publish all info');
        }
    });
}

export function getProductLineInfo(productLineId = null, formData = null) {
    const productLineNameInput = document.getElementById('product-line-name-input');
    const productLineName = productLineNameInput.value.trim();
    const allMediaEntries = document.getElementById('product-line-images').querySelectorAll('.image-entry');
    const productLineImageContents = getMediaContent(data_productLineImages, allMediaEntries, formData);
    const allDescriptionEntries =
        Array.from(document.getElementById('product-line-descriptions').querySelectorAll('.description-entry')).slice(1);
    const descriptionContent = getDescriptionContent(data_productLineDescriptionImages, allDescriptionEntries, formData);
    return {
        id: parseInt(productLineId),
        name: productLineName,
        media: productLineImageContents,
        descriptions: descriptionContent
    };
}

function getMediaContent(dataImageArray, allMediaTemplateEntries, formData = null) {
    const mediaNames = wrapImages(dataImageArray, formData);
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

function getDescriptionContent(dataImageArray, allDescriptionEntries, formData = null) {
    let descriptionImageNames = wrapImages(dataImageArray, formData);
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

export function getAllProductInfoFromList(productIdList, productLineId, formData = null) {
    const productInfos = [];
    for (const productId of productIdList) {
        const productInfo = getProductInfo(productLineId, productId, formData);
        if (productInfo) {
            productInfos.push(productInfo);
        } else {
            return null; // one product fail so all fail
        }
    }
    return productInfos;
}

function getProductInfo(productLineId, productId, formData = null) {
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
    const productImageContents = getMediaContent(data_allProductImages.get(productId), allMediaEntries, formData);

    const allDescriptionEntries = Array.from(productContainer.querySelectorAll('.description-entry')).slice(1);
    const productDescriptionContent = getDescriptionContent(data_allProductDescriptionImages.get(productId), allDescriptionEntries, formData);
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
async function postAllProductInfo(productLineInfoData, productInfoDataList, formData) {
    const productInfoWrapper = {
        productLineDTO: productLineInfoData,
        productDTOList: productInfoDataList
    }
    formData.append(
        'productWrapperDTO',
        new Blob([JSON.stringify(productInfoWrapper)], { type: 'application/json' })
    );

    for (let [key, value] of formData.entries()) {
        if (value instanceof Blob) {
            console.log(key, "(Blob)", value.type, value.size, value);
            // Optionally read blob content if it's text
            if (value.type.startsWith("application/json") || value.type.startsWith("text")) {
                value.text().then(text => console.log(`  → ${text}`));
            }
        } else {
            console.log(key, value);
        }
    }


    const response = await fetch('http://localhost:8080/api/productWrapper', {
        method: 'POST',
        body: formData
    });
    if (!response.ok) {
        throw new Error('Failed uploading all products info');
    }
    return await response.json();
}

function wrapImages(dataImageArray, formData = null) {
    dataImageArray.forEach((image, index) => {
        if (image instanceof File) {
            if (formData)
                formData.append(image.name, image);
            dataImageArray[index] = image.name;
        }
    });
    return dataImageArray;
}