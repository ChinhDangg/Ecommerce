const productLineImageInput = document.getElementById('add-line-image-input');
let data_productLineImages = []

document.getElementById('add-line-image-btn').addEventListener('click', function () {
    productLineImageInput.click();
});

productLineImageInput.addEventListener('change', function () {
    const productLineImageContainer = document.getElementById('product-line-images');
    setInputChangeListener(productLineImageInput, data_productLineImages, productLineImageContainer);
});

function  setInputChangeListener(input, dataImageArray, allImageContainer) {
    const inputImageFiles = Array.from(input.files);
    const imageEntryTemplate = document.querySelector('#image-entry-template').cloneNode(true);
    imageEntryTemplate.classList.remove('hidden');
    let max = 5 - dataImageArray.length;
    inputImageFiles.some(file => {
        if (max <= 0)
            return true;
        dataImageArray.push(file);
        const reader = new FileReader();
        reader.onload = function (e) {
            const newImageEntry = imageEntryTemplate.cloneNode(true);
            newImageEntry.querySelector('.image-entry-img').src = e.target.result;
            initializeImageButtons(allImageContainer, newImageEntry, dataImageArray);
            allImageContainer.appendChild(newImageEntry);
        }
        reader.readAsDataURL(file); // start the reading process which trigger the onload
        max--;
    });
    input.value = null;
    console.log(dataImageArray);
}

function initializeImageButtons(allImageContainer, imageContainer, dataImageArray) {
    imageContainer.querySelector('.delete-image-btn').addEventListener('click', function () {
        let index = getChildIndex(imageContainer);
        dataImageArray.splice(index, 1);
        console.log(dataImageArray);
        imageContainer.remove();
    });
    imageContainer.querySelector('.move-left-btn').addEventListener('click', function () {
        const previousSib = imageContainer.previousElementSibling;
        if (previousSib) {
            const currentIndex = getChildIndex(imageContainer);
            [dataImageArray[currentIndex], dataImageArray[currentIndex - 1]] = [dataImageArray[currentIndex - 1], dataImageArray[currentIndex]]
            console.log(dataImageArray);
            allImageContainer.insertBefore(imageContainer, previousSib);
        }
    });
    imageContainer.querySelector('.move-right-btn').addEventListener('click', function () {
        const nextSib = imageContainer.nextElementSibling;
        if (nextSib) {
            const currentIndex = getChildIndex(imageContainer);
            [dataImageArray[currentIndex], dataImageArray[currentIndex + 1]] = [dataImageArray[currentIndex + 1], dataImageArray[currentIndex]]
            console.log(dataImageArray);
            allImageContainer.insertBefore(nextSib, imageContainer);
        }
    });
}

function getChildIndex(child) {
    let index = 0;
    while (child = child.previousElementSibling)
        index++;
    return index;
}

const data_productLineDescriptionImages = []

document.getElementById('add-line-description-btn').addEventListener('click', function () {
    const productLineDescriptionContainer = document.getElementById('product-line-descriptions');
    const newDescription = productLineDescriptionContainer.querySelector('.description-entry').cloneNode(true);
    newDescription.classList.remove('hidden');
    initializeDescriptionButtons(newDescription, data_productLineDescriptionImages);
    productLineDescriptionContainer.appendChild(newDescription);
});

function initializeDescriptionButtons(descriptionContainer, dataImageArray) {
    descriptionContainer.querySelector('.delete-description-btn').addEventListener('click', function () {
        dataImageArray.splice(getChildIndex(descriptionContainer), 1);
        console.log(dataImageArray);
        descriptionContainer.remove();
    });
    const descriptionImageInput = descriptionContainer.querySelector('.description-image-input');
    descriptionImageInput.addEventListener('change', function () {
        const file = this.files[0];
        if (file) {
            const index = getChildIndex(descriptionContainer);
            dataImageArray[index] = file;
            console.log(dataImageArray);
            const reader = new FileReader();
            reader.onload = function (e) {
                descriptionContainer.querySelector('.description-textarea-entry').classList.add('hidden');
                const descriptionImageContainer = descriptionContainer.querySelector('.description-image-entry');
                descriptionImageContainer.classList.remove('hidden');
                const imgTag = descriptionImageContainer.querySelector('img');
                imgTag.src = e.target.result;
                imgTag.alt = 'Description Image';
            }
            reader.readAsDataURL(file);
        }
    });
    descriptionContainer.querySelector('.toggle-description-type').addEventListener('click', function () {
        descriptionImageInput.click();
    });
}


/* product category section */
const categoryContainer = document.getElementById('category-list');
const subcategoryContainer = document.getElementById('subcategory-list');
let currentCategory = null, currentCategoryButton = null;
const categoryNavStack = [];
const categoryTree = [];

async function fetchTopCategories() {
    try {
        const response = await fetch('http://localhost:8080/api/product/category');
        const topCategories = await response.json(); // [{id, name}]
        initializeTopCategories(topCategories);
    } catch (error) {
        console.error('Error fetching top categories:', error);
        categoryContainer.innerHTML = 'Fail to load categories';
    }
}
fetchTopCategories();

async function addTopCategories(topCategories, categoryToToggle = null) {
    removeAllTopCategoryDisplay();
    categoryNavStack.push(topCategories);
    console.log(categoryNavStack);
    topCategories.forEach(topCategory => {
        const categoryItem = categoryContainer.querySelector('.category-item').cloneNode(true);
        categoryItem.classList.remove('hidden');
        categoryContainer.appendChild(categoryItem);
        updateCategoryInfo(categoryItem, topCategory);
        const toggleButton = categoryItem.querySelector('.toggle-subcategories');
        if (topCategory === categoryToToggle) {
            toggleButton.querySelector('svg').classList.toggle('rotate-[-90deg]');
        }
        if (!categoryTree.includes(topCategory)) {
            categoryTree.push(topCategory); // update
        }
        toggleButton.addEventListener('click', async function () {
            await toggleSubcategories(topCategory, this);
        });
    });
}

function initializeTopCategories(topCategories) {
    topCategories.forEach(topCategory => {
        categoryTree.push(topCategory); // update
    });
    addTopCategories(topCategories);
}

function removeAllTopCategoryDisplay() {
    const allCategoryItems = categoryContainer.querySelectorAll('#category-list .category-item');
    Array.from(allCategoryItems).slice(1).forEach(item => item.remove()); // remove all cat item except first one
}

function addSubCategories(subCategories) {
    removeAllSubcategoryDisplay();
    subCategories.forEach(subCategory => {
        const subcategoryItem = subcategoryContainer.querySelector('.subcategory-item').cloneNode(true);
        subcategoryItem.classList.remove('hidden');
        subcategoryContainer.appendChild(subcategoryItem);
        updateCategoryInfo(subcategoryItem, subCategory);
        subcategoryItem.querySelector('.toggle-subcategories').addEventListener('click', async function () {
            addTopCategories(subCategories, subCategory);
            toggleSubcategories(subCategory, this);
        });
    });
}

function removeAllSubcategoryDisplay() {
    const allSubcategoryItems = subcategoryContainer.querySelectorAll('#subcategory-list .subcategory-item');
    Array.from(allSubcategoryItems).slice(1).forEach(item => item.remove()); // remove all sub item except first one
}

async function fetchSubCategories(parentId) {
    try {
        const response = await fetch('http://localhost:8080/api/product/subcategory/' + parentId);
        return await response.json(); // [{id, name}]
    } catch (error) {
        console.error('Error fetching top categories:', error);
        return null;
    }
}

async function toggleSubcategories(topCategory, toggleButton) {
    toggleButton.querySelector('svg').classList.toggle('rotate-[-90deg]');
    if (checkSameCategoryToggle(topCategory)) {
        return;
    }
    const foundCategory = categoryTree.find(item => item === topCategory);
    if (foundCategory && foundCategory.subcategories && foundCategory.subcategories.length !== 0) {
        addSubCategories(foundCategory.subcategories);
    } else {
        const querySubcategories = await fetchSubCategories(topCategory.id);
        if (querySubcategories) {
            foundCategory.subcategories = querySubcategories;
            addSubCategories(querySubcategories);
        }
    }
    currentCategory = topCategory;
    currentCategoryButton = toggleButton;
    console.log(categoryTree);
}

function checkSameCategoryToggle(topCategory) {
    if (currentCategory === topCategory) {
        const allSubcategoryItems = subcategoryContainer.querySelectorAll('#subcategory-list .subcategory-item');
        Array.from(allSubcategoryItems).slice(1).forEach(item => item.remove());
        currentCategory = null;
        currentCategoryButton = null;
        return true;
    } else if (currentCategory != null) {
        currentCategoryButton.querySelector('svg').classList.toggle('rotate-[-90deg]');
        return false;
    }
    return false;
}

function updateCategoryInfo(container, category) {
    const topCategoryInput = container.querySelector('#category-input');
    topCategoryInput.id = `category-${category.id}`;
    topCategoryInput.value = category.name;
    const topCategoryLabel = container.querySelector('label[for="category-input"]');
    topCategoryLabel.htmlFor = `category-${category.id}`;
    topCategoryLabel.name = category.name;
    topCategoryLabel.innerHTML = category.name;
}

document.getElementById('category-back-button').addEventListener('click', function () {
    removeAllSubcategoryDisplay();
    if (categoryNavStack.length > 1) {
        categoryNavStack.pop();
        removeAllTopCategoryDisplay();
        addTopCategories(categoryNavStack[categoryNavStack.length - 1]);
        categoryNavStack.pop();
    } else {
        addTopCategories(categoryNavStack.pop());
    }
    console.log(categoryNavStack);
});



/* Subcategories element is either in a subcategories class or in category-item (id - top),
in sub then is sub of the sub, else direct sub of a top cate,
must be only one sub in either top or another sub
Top-category should be in top-categories class */


/* Product Options Section */
const optionValueContainer = document.getElementById('option-values-container');
const optionHeaderWrapper = document.getElementById('option-header-tr');
const optionBodyContainer = document.getElementById('options-body');
const optionMap = new Map();
let products = [0];

document.getElementById('add-option-btn').addEventListener('click', function () {
    const key = prompt('Enter a new option name:');
    if (key && !optionMap.has(key)) {
        optionMap.set(key, []);
        const optionValueItem = optionValueContainer.querySelector('.option-value-item').cloneNode(true);
        optionValueItem.classList.remove('hidden');
        optionValueItem.querySelector('.option-key').innerHTML = `${key}:`;
        optionValueContainer.appendChild(optionValueItem);
        initializeOptionValueButtons(optionValueItem, key);
        const optionHeaderItem = optionHeaderWrapper.querySelector('.option-header-th').cloneNode(true);
        optionHeaderItem.dataset.optionId = key;
        optionHeaderItem.innerHTML = key;
        optionHeaderWrapper.appendChild(optionHeaderItem);
        addProductOptionSelection(key);
    }
});

function initializeOptionValueButtons(optionValueItem, optionKey) {
    const optionValues = optionValueItem.querySelector('.option-values');
    optionValueItem.querySelector('.add-value-btn').addEventListener('click', function () {
        const value = prompt(`Enter a new value for: ${optionKey}`);
        if (value && !optionMap.get(optionKey).includes(value)) {
            optionMap.get(optionKey).push(value);
            optionValues.innerHTML = optionMap.get(optionKey).join(', ');
            updateProductOptionSelectionValue(optionKey);
        }
        console.log(optionMap);
    });
    optionValueItem.querySelector('.pop-value-btn').addEventListener('click', function () {
        optionMap.get(optionKey).pop();
        optionValues.innerHTML = optionMap.get(optionKey).join(', ');
        updateProductOptionSelectionValue(optionKey);
        console.log(optionMap);
    });
    optionValueItem.querySelector('.remove-option-btn').addEventListener('click', function () {
        optionMap.delete(optionKey);
        optionValueItem.remove();
        optionHeaderWrapper.querySelector(`[data-option-id="${optionKey}"]`).remove();
        optionBodyContainer.querySelectorAll('.option-product-selection').forEach(selectionWrapper => {
            if (selectionWrapper.querySelector(`select[data-option-id="${optionKey}"]`)) {
                selectionWrapper.remove();
            }
        });
        console.log(optionMap);
    });
}

function addProductOptionSelection(optionKey) {
    const allProductOptionItems = optionBodyContainer.querySelectorAll('.option-product-item');
    Array.from(allProductOptionItems).slice(1).forEach(item => {
        if (item.querySelector(`select[data-option-id="${optionKey}"]`) == null) {
            const optionSelectionWrapper = optionBodyContainer.querySelector('.option-product-selection').cloneNode(true);
            const optionSelection = optionSelectionWrapper.querySelector('select');
            optionSelection.dataset.optionId = optionKey;
            optionSelection.innerHTML = "";
            item.appendChild(optionSelectionWrapper);
        }
    });
}

function updateProductOptionSelectionValue(optionKey) {
    const optionValues = optionMap.get(optionKey);
    optionBodyContainer.querySelectorAll(`select[data-option-id="${optionKey}"]`).forEach(select => {
        select.innerHTML = "";
        const selectOptions = getValuesAsSelectOption(optionValues);
        select.append(...selectOptions);
    });
}

document.getElementById('add-product-btn').addEventListener('click', async function () {
    const newProductId = products[products.length - 1] + 1;
    products.push(newProductId);
    addProductForOption(newProductId);
    addProductForSpec(newProductId);
    addNewProductGroupTemplate(newProductId);

    // const [name, images, descriptions] = await getProductLineInfo();
    // await postProductLineInfo(name, images, descriptions);
});

function addProductForOption(productId) {
    const optionProductItem = optionBodyContainer.querySelector('.option-product-item').cloneNode();
    optionProductItem.dataset.productId = productId;
    optionProductItem.classList.remove('hidden');
    const optionProductName = optionBodyContainer.querySelector('.option-product-name').cloneNode();
    optionProductName.innerHTML = 'Product ' + productId.toString();
    optionProductItem.appendChild(optionProductName);
    optionBodyContainer.appendChild(optionProductItem);
    optionMap.forEach((optionValues, optionKey) => {
        const options = getValuesAsSelectOption(optionValues);
        const optionSelectionWrapper = optionBodyContainer.querySelector('.option-product-selection').cloneNode(true);
        const optionSelection = optionSelectionWrapper.querySelector('select');
        optionSelection.innerHTML = "";
        optionSelection.dataset.optionId = optionKey;
        optionSelection.append(...options);
        optionProductItem.appendChild(optionSelectionWrapper);
    });
}

function addProductForSpec(productId) {
    const specProductItem = specBodyContainer.querySelector('.spec-product-item').cloneNode();
    specProductItem.dataset.productId = productId;
    specProductItem.classList.remove('hidden');
    const specProductName = specBodyContainer.querySelector('.spec-product-name').cloneNode();
    specProductName.innerHTML = 'Product ' + productId.toString();
    specProductItem.appendChild(specProductName);
    specBodyContainer.appendChild(specProductItem);
    specMap.forEach((specValues, specKey) => {
        const specOptions = getValuesAsSelectOption(specValues);
        const specSelectionWrapper = specBodyContainer.querySelector('.spec-product-selection').cloneNode(true);
        const specSelection = specSelectionWrapper.querySelector('select');
        specSelection.innerHTML = "";
        specSelection.dataset.specId = specKey;
        specSelection.append(...specOptions);
        specProductItem.appendChild(specSelectionWrapper);
    });
}

function getValuesAsSelectOption(values) {
    return values.map(value => {
        const option = document.createElement('option');
        option.value = value;
        option.textContent = value;
        return option;
    });
}

function removeProductItemFromOption(productId) {
    optionBodyContainer.querySelectorAll(`tr[data-product-id="${productId}"]`).forEach(
        item => item.remove()
    );
}


/* Product Specifications Section */
const specHeaderWrapper = document.getElementById('spec-header-tr');
const specBodyContainer = document.getElementById('spec-body');
const specMap = new Map();

document.getElementById('add-spec-btn').addEventListener('click', function () {
    const key = prompt('Enter a new spec name:');
    if (key && !specMap.has(key)) {
        specMap.set(key, []);
        const specValueContainer = document.getElementById('spec-values-container');
        const specValueItem = specValueContainer.querySelector('.spec-value-item').cloneNode(true);
        specValueItem.classList.remove('hidden');
        specValueItem.querySelector('.spec-key').innerHTML = `${key}:`;
        specValueContainer.appendChild(specValueItem);
        initializeSpecValueButtons(specValueItem, key);
        const specHeaderItem = specHeaderWrapper.querySelector('.spec-header-th').cloneNode(true);
        specHeaderItem.dataset.specId = `spec-id-${key}`;
        specHeaderItem.innerHTML = key;
        specHeaderWrapper.appendChild(specHeaderItem);
        addProductSpecSelection(key);
    }
});

function initializeSpecValueButtons(specValueItem, specKey) {
    const specValues = specValueItem.querySelector('.spec-values');
    specValueItem.querySelector('.add-value-btn').addEventListener('click', function () {
        const value = prompt(`Enter a new value for: ${specKey}`);
        if (value && !specMap.get(specKey).includes(value)) {
            specMap.get(specKey).push(value);
            specValues.innerHTML = specMap.get(specKey).join(', ');
            updateProductSpecSelectionValue(specKey);
        }
        console.log(specMap);
    });
    specValueItem.querySelector('.pop-value-btn').addEventListener('click', function () {
        specMap.get(specKey).pop();
        specValues.innerHTML = specMap.get(specKey).join(', ');
        updateProductSpecSelectionValue(specKey);
        console.log(specMap);
    });
    specValueItem.querySelector('.remove-option-btn').addEventListener('click', function () {
        specMap.delete(specKey);
        specValueItem.remove();
        specHeaderWrapper.querySelector(`[data-spec-id="${specKey}"]`).remove();
        specBodyContainer.querySelectorAll('.spec-product-selection').forEach(selectionWrapper => {
            if (selectionWrapper.querySelector(`select[data-spec-id="${specKey}"]`)) {
                selectionWrapper.remove();
            }
        });
        console.log(specMap);
    });
}

function addProductSpecSelection(specKey) {
    const allProductSpecItems = specBodyContainer.querySelectorAll('.spec-product-item');
    Array.from(allProductSpecItems).slice(1).forEach(item => {
        if (item.querySelector(`select[data-spec-id="${specKey}"]`) == null) {
            const specSelectionWrapper = specBodyContainer.querySelector('.spec-product-selection').cloneNode(true);
            const specSelection = specSelectionWrapper.querySelector('select');
            specSelection.dataset.specId = specKey;
            specSelection.innerHTML = "";
            item.appendChild(specSelectionWrapper);
        }
    });
}

function updateProductSpecSelectionValue(specKey) {
    const specValues = specMap.get(specKey);
    specBodyContainer.querySelectorAll(`select[data-spec-id="${specKey}"]`).forEach(select => {
        select.innerHTML = "";
        const specSelectOptions = getValuesAsSelectOption(specValues);
        select.append(...specSelectOptions);
    });
}

function removeProductItemFromSpec(productId) {
    specBodyContainer.querySelectorAll(`tr[data-product-id="${productId}"]`).forEach(
        item => item.remove()
    );
}


/* Product Group section */
const productGroupContainer = document.getElementById('product-group-container');
const data_allProductImages = new Map();
const data_allProductDescriptionImages = new Map();

function addNewProductGroupTemplate(productId) {
    const productGroupItem = productGroupContainer.querySelector('.product-group-template').cloneNode(true);
    productGroupItem.classList.remove('hidden');
    productGroupItem.dataset.productId = productId;
    productGroupItem.querySelector('.product-number').innerHTML = 'Product ' + productId;
    productGroupItem.querySelector('.feature-entry').remove(); // remove uninitialized one
    productGroupContainer.appendChild(productGroupItem);
    data_allProductImages.set(productId, []);
    data_allProductDescriptionImages.set(productId, []);

    productGroupItem.querySelector('.delete-product-btn').addEventListener('click', function() {
        deleteProductData(productId);
        productGroupItem.remove();
        removeProductItemFromOption(productId);
        removeProductItemFromSpec(productId);
    });
    productGroupItem.querySelector('.toggle-collapse').addEventListener('click', function() {
        productGroupItem.querySelector('.product-details').classList.toggle('hidden');
    });
    productGroupItem.querySelector('.add-feature-btn').addEventListener('click', function () {
        const featureEntry = productGroupContainer.querySelector('.feature-entry').cloneNode(true);
        productGroupItem.querySelector('.product-features').appendChild(featureEntry);
        featureEntry.querySelector('.delete-feature-btn').addEventListener('click', function () {
            featureEntry.remove();
        })
    });
    const addProductImageInput = productGroupItem.querySelector('.add-product-image-input');
    addProductImageInput.addEventListener('change', function () {
        setInputChangeListener(
            addProductImageInput,
            data_allProductImages.get(productId),
            productGroupItem.querySelector('.product-images')
        );
    });
    productGroupItem.querySelector('.add-product-image-btn').addEventListener('click', function () {
        addProductImageInput.click();
    });
    productGroupItem.querySelector('.add-product-description-btn').addEventListener('click', function () {
        const descriptionItem = productGroupContainer.querySelector('.description-entry').cloneNode(true);
        descriptionItem.classList.remove('hidden');
        productGroupItem.querySelector('.product-descriptions').appendChild(descriptionItem);
        initializeDescriptionButtons(descriptionItem, data_allProductDescriptionImages.get(productId));
    });
}

function deleteProductData(productId) {
    products = products.filter(item => item !== productId);
    optionMap.delete(productId);
    specMap.delete(productId);
    data_allProductImages.delete(productId);
    data_allProductDescriptionImages.delete(productId);
    console.log(productId);
    console.log(products);
    console.log(optionMap);
    console.log(specMap);
    console.log(data_allProductImages);
    console.log(data_allProductDescriptionImages);
}


/* POST */
// product line POST
async function getProductLineInfo() {
    const productLineNameInput = document.getElementById('product-line-name-input');
    const productLineName = productLineNameInput.value.trim();
    if (!productLineName)
        return null;
    const productLineImageNames = data_productLineImages.length > 0 ? await uploadImages(data_productLineImages) : [];
    const allDescriptionEntries =
        Array.from(document.getElementById('product-line-descriptions').querySelectorAll('.description-entry')).slice(1);
    const descriptionContent = await getDescriptionContent(data_productLineDescriptionImages, allDescriptionEntries);
    return {
        name: productLineName,
        imageNames: productLineImageNames,
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

function postProductLineInfo(productLineInfoData) {
    const url = 'http://localhost:8080/api/product/newProductLine';
    console.log(productLineInfoData);
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

function getProductOptionContent(productId) {
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
    return optionContent.length === 0 ? null : optionContent;
}

function getProductSpecificationContent(productId) {
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
    return specContent.length === 0 ? null : specContent;
}

// product group POST
async function getProductInfo(productId) {
    const productContainer = productGroupContainer.querySelector(`[data-product-id="${productId}"]`);
    if (!productContainer)
        return null;
    const productFeatures = [];
    productContainer.querySelectorAll('.product-feature-input').forEach(input => {
        const featureContent = input.value.trim();
        if (featureContent)
            productFeatures.push(featureContent);
    });
    const productImageNames = data_allProductImages.get(productId).length > 0 ?
        await uploadImages(data_allProductImages.get(productId)) : [];
    const allDescriptionEntries = Array.from(productContainer.querySelectorAll('.description-entry')).slice(1);
    const productDescriptionContent = await getDescriptionContent(data_allProductDescriptionImages.get(productId), allDescriptionEntries);
    return {
        name: productContainer.querySelector('.product-name-input').value,
        brand: productContainer.querySelector('.product-brand-input').value,
        manufacturerId: productContainer.querySelector('.product-manufacturer-part-number-input').value,
        quantity: productContainer.querySelector('.product-quantity-input').value,
        regularPrice: productContainer.querySelector('.product-regular-price-input').value,
        salePrice: productContainer.querySelector('.product-sale-price-input').value,
        saleEndDate: productContainer.querySelector('.product-sale-date-input').value,
        categoryId: document.querySelector('input[name="category"]:checked')?.id.replace("category-", ""),
        options: getProductOptionContent(productId),
        specs: getProductSpecificationContent(productId),
        features: productFeatures,
        imageNames: productImageNames,
        descriptions: productDescriptionContent
    };
}

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
            if (!response.ok)
                throw new Error('Fail upload images');
            return response.json(); // return list of image names
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

