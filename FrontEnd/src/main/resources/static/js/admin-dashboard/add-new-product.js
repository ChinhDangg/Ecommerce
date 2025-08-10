export function initializeAdd() {

    data_productLineImages.length = 0;
    data_productLineDescriptionImages.length = 0;
    data_allProductImages.clear();
    data_allProductDescriptionImages.clear();
    products.splice(1);

    initializeProductLineSection();
    initializeCategorySection();
    initializeProductOptionSection();
    initializeProductSpecificationSection();
}

const mediaURL = document.getElementById('media-url').innerText;
const categoryTopURL = document.getElementById('categoryTop-url').innerText;
const categorySubURL = document.getElementById('categorySubcategory-url').innerText;

/* Product line section */
const productLineImageInput = document.getElementById('add-line-image-input');
export const data_productLineImages = []

function initializeProductLineSection() {
    initializeProductDeleteButton();
    document.getElementById('add-line-image-btn').addEventListener('click', function () {
        productLineImageInput.click();
    });
    document.getElementById('add-line-description-btn').addEventListener('click', function () {
        addProductLineDescription();
    });
}

function initializeProductDeleteButton() {
    const btn = document.getElementById('product-line-section').querySelector('.delete-product-btn');
    btn.innerText = 'Clear';
    btn.onclick = function () {
        const confirmClear = confirm('Are you sure you want to clear the product line?');
        if (confirmClear)
            clearProductLineSection();
    };
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

productLineImageInput.addEventListener('change', function () {
    const productLineImageContainer = document.getElementById('product-line-images');
    setInputImageChangeListener(productLineImageInput, data_productLineImages, productLineImageContainer);
});

function setInputImageChangeListener(input, dataImageArray, allImageContainer) {
    const inputImageFiles = Array.from(input.files);
    // const imageEntryTemplate = document.querySelector('#image-entry-template').cloneNode(true);
    // imageEntryTemplate.classList.remove('hidden');
    let max = 5 - dataImageArray.length;
    inputImageFiles.some(file => {
        if (max <= 0)
            return true;
        const reader = new FileReader();
        reader.onload = function (e) {
            // dataImageArray.push(file);
            // const newImageEntry = imageEntryTemplate.cloneNode(true);
            // newImageEntry.querySelector('.image-entry-img').src = e.target.result;
            // initializeImageButtons(allImageContainer, newImageEntry, dataImageArray);
            // allImageContainer.appendChild(newImageEntry);
            addImageEntry(dataImageArray, allImageContainer, file, e.target.result);
        }
        reader.readAsDataURL(file); // start the reading process which trigger the onload
        max--;
    });
    input.value = null;
}

export function addImageEntry(dataImageArray, allImageContainer, imageFile, imageSrc) {
    const imageEntryTemplate = document.querySelector('#image-entry-template').cloneNode(true);
    imageEntryTemplate.classList.remove('hidden');
    dataImageArray.push(imageFile == null ? imageSrc : imageFile);
    const newImageEntry = imageEntryTemplate.querySelector('.image-entry');
    newImageEntry.querySelector('.image-entry-img').src = imageFile ? imageSrc : `${mediaURL}${imageSrc}`;
    initializeImageButtons(allImageContainer, newImageEntry, dataImageArray);
    allImageContainer.appendChild(newImageEntry);
    console.log(dataImageArray);
    return newImageEntry;
}

export function initializeImageButtons(allImageContainer, imageContainer, dataImageArray) {
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

export const data_productLineDescriptionImages = []

export function addProductLineDescription() {
    const productLineDescriptionContainer = document.getElementById('product-line-descriptions');
    return addDescription(productLineDescriptionContainer, data_productLineDescriptionImages);
}

function addDescription(descriptionContainer, dataDescriptionImageArray) {
    if (descriptionContainer.children.length > 10) {
        alert('Max of 10 descriptions only');
        return;
    }
    const newDescription = descriptionContainer.querySelector('.description-entry').cloneNode(true);
    newDescription.classList.remove('hidden');
    initializeDescriptionButtons(newDescription, dataDescriptionImageArray);
    descriptionContainer.appendChild(newDescription);
    return newDescription;
}

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
            const reader = new FileReader();
            reader.onload = function () {
                updateDescriptionImage(descriptionContainer, dataImageArray, file);
            }
            reader.readAsDataURL(file);
        }
    });
    descriptionContainer.querySelector('.toggle-description-type').addEventListener('click', function () {
        descriptionImageInput.click();
    });
}

export function updateDescriptionImage(descriptionContainer, dataImageArray, imageContent) {
    const index = getChildIndex(descriptionContainer);
    dataImageArray[index] = imageContent;
    console.log(dataImageArray);
    descriptionContainer.querySelector('.description-textarea-entry').classList.add('hidden');
    const descriptionImageContainer = descriptionContainer.querySelector('.description-image-entry');
    descriptionImageContainer.classList.remove('hidden');
    const imgTag = descriptionImageContainer.querySelector('img');
    imgTag.src = imageContent instanceof File ? URL.createObjectURL(imageContent) : `${mediaURL}${imageContent}`;
    imgTag.alt = 'Description Image';
}


/* Product category section */
/* Subcategories element is either in a subcategories class or in category-item (id - top),
in sub then is sub of the sub, else direct sub of a top cate,
must be only one sub in either top or another sub
Top-category should be in top-categories class */
let currentCategory = null, currentCategoryButton = null;
const categoryNavStack = [];
export const categoryTree = [];

function initializeCategorySection() {
    currentCategory = null;
    currentCategoryButton = null;
    categoryNavStack.length = 0;
    categoryTree.length = 0;

    document.getElementById('product-category-section').querySelector('.toggle-collapse').addEventListener('click', async function(){
        if (this.classList.contains('hidden')) {
            console.log('is hidden');
            return;
        }
        expandCategorySection(this);
        if (!categoryTree.length) {
            await fetchTopCategories();
        }
    });
    document.getElementById('category-back-button').addEventListener('click',async function () {
        removeAllSubcategoryDisplay();
        if (categoryNavStack.length > 1) {
            categoryNavStack.pop();
            removeAllTopCategoryDisplay();
            await addTopCategories(categoryNavStack[categoryNavStack.length - 1]);
            categoryNavStack.pop();
        } else if (categoryNavStack.length) {
            await addTopCategories(categoryNavStack.pop());
            categoryNavStack.pop();
        } else {
            await fetchTopCategories();
        }
        console.log(categoryNavStack);
    });
}

export function expandCategorySection(categoryToggleButton) {
    categoryToggleButton.classList.add('hidden');
    const categorySection = document.getElementById('product-category-section');
    categorySection.querySelector('.category-description').classList.remove('hidden');
    categorySection.querySelector('#category-back-button').classList.remove('hidden');
}

async function fetchTopCategories() {
    try {
        const response = await fetch(categoryTopURL);
        const topCategories = await response.json(); // [{id, name}]
        await addTopCategories(topCategories);
    } catch (error) {
        console.error('Error fetching top categories:', error);
        const categoryContainer = document.getElementById('category-list');
        categoryContainer.innerHTML = 'Fail to load categories';
    }
}

export async function addTopCategories(topCategories, categoryToToggle = null, categoryToCheck = null, isChecked = false) {
    removeAllTopCategoryDisplay();
    categoryNavStack.push(topCategories);
    console.log(categoryNavStack);
    topCategories.forEach(topCategory => {
        const categoryContainer = document.getElementById('category-list');
        const categoryItem = categoryContainer.querySelector('.category-item').cloneNode(true);
        categoryItem.classList.remove('hidden');
        categoryContainer.appendChild(categoryItem);
        updateCategoryInfo(categoryItem, topCategory, categoryToCheck, isChecked);
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

function removeAllTopCategoryDisplay() {
    const categoryContainer = document.getElementById('category-list');
    const allCategoryItems = categoryContainer.querySelectorAll('#category-list .category-item');
    Array.from(allCategoryItems).slice(1).forEach(item => item.remove()); // remove all cat item except first one
}

function addSubCategories(subCategories) {
    removeAllSubcategoryDisplay();
    subCategories.forEach(subCategory => {
        const subcategoryContainer = document.getElementById('subcategory-list');
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
    const subcategoryContainer = document.getElementById('subcategory-list');
    const allSubcategoryItems = subcategoryContainer.querySelectorAll('#subcategory-list .subcategory-item');
    Array.from(allSubcategoryItems).slice(1).forEach(item => item.remove()); // remove all sub item except first one
}

async function fetchSubCategories(parentId) {
    try {
        const response = await fetch(`${categorySubURL}/${parentId}`);
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
        } else {
            const subcategoryContainer = document.getElementById('subcategory-list');
            subcategoryContainer.innerHTML = 'Fail to load sub-categories';
        }
    }
    currentCategory = topCategory;
    currentCategoryButton = toggleButton;
    console.log(categoryTree);
}

function checkSameCategoryToggle(topCategory) {
    if (currentCategory === topCategory) {
        const subcategoryContainer = document.getElementById('subcategory-list');
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

function updateCategoryInfo(container, category, categoryToCheck = null, isChecked = false) {
    const topCategoryInput = container.querySelector('.category-input');
    topCategoryInput.id = `category-${category.id}`;
    topCategoryInput.value = category.name;
    if (category === categoryToCheck)
        topCategoryInput.checked = isChecked;
    const topCategoryLabel = container.querySelector('.category-input-label');
    topCategoryLabel.htmlFor = `category-${category.id}`;
    topCategoryLabel.name = category.name;
    topCategoryLabel.innerHTML = category.name;
}



/* Product Options Section */
const optionMap = new Map();
export let products = [0];

function initializeProductOptionSection() {
    document.getElementById('add-option-btn').addEventListener('click', function () {
        const key = prompt('Enter a new option name:');
        addOptionKey(key);
    });
    document.getElementById('add-product-btn').addEventListener('click', async function () {
        addNewProductEntry();
    });
}

export function addOptionKey(key) {
    const optionValueContainer = document.getElementById('option-values-container');
    if (key && !optionMap.has(key)) {
        optionMap.set(key, []);
        const optionValueItem = optionValueContainer.querySelector('.option-value-item').cloneNode(true);
        optionValueItem.classList.remove('hidden');
        optionValueItem.dataset.optionItemId = key;
        optionValueItem.querySelector('.option-key').innerHTML = `${key}:`;
        optionValueContainer.appendChild(optionValueItem);
        initializeOptionValueButtons(optionValueItem, key);
        const optionHeaderWrapper = document.getElementById('option-header-tr');
        const optionHeaderItem = optionHeaderWrapper.querySelector('.option-header-th').cloneNode(true);
        optionHeaderItem.dataset.optionId = key;
        optionHeaderItem.innerHTML = key;
        optionHeaderWrapper.appendChild(optionHeaderItem);
        addProductOptionSelection(key);
        return optionValueItem;
    } else if (optionMap.has(key)) {
        return optionValueContainer.querySelector(`.option-value-item[data-option-item-id="${key}"]`);
    }
    return null;
}

export function addOptionValue(optionValueItem, optionKey, value) {
    if (value && !optionMap.get(optionKey).includes(value)) {
        optionMap.get(optionKey).push(value);
        optionValueItem.querySelector('.option-values').innerHTML = optionMap.get(optionKey).join(', ');
        updateProductOptionSelectionValue(optionKey);
    }
    console.log(optionMap);
}

function initializeOptionValueButtons(optionValueItem, optionKey) {
    //const optionValues = optionValueItem.querySelector('.option-values');
    optionValueItem.querySelector('.add-value-btn').addEventListener('click', function () {
        const value = prompt(`Enter a new value for: ${optionKey}`);
        addOptionValue(optionValueItem, optionKey, value);
        console.log(optionMap);
    });
    optionValueItem.querySelector('.pop-value-btn').addEventListener('click', function () {
        optionMap.get(optionKey).pop();
        optionValueItem.querySelector('.option-values').innerHTML = optionMap.get(optionKey).join(', ');
        updateProductOptionSelectionValue(optionKey);
        console.log(optionMap);
    });
    optionValueItem.querySelector('.remove-option-btn').addEventListener('click', function () {
        optionMap.delete(optionKey);
        optionValueItem.remove();
        const optionHeaderWrapper = document.getElementById('option-header-tr');
        optionHeaderWrapper.querySelector(`[data-option-id="${optionKey}"]`).remove();
        const optionBodyContainer = document.getElementById('options-body');
        optionBodyContainer.querySelectorAll('.option-product-selection').forEach(selectionWrapper => {
            if (selectionWrapper.querySelector(`select[data-option-id="${optionKey}"]`)) {
                selectionWrapper.remove();
            }
        });
        console.log(optionMap);
    });
}

function addProductOptionSelection(optionKey) {
    const optionBodyContainer = document.getElementById('options-body');
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
    const optionBodyContainer = document.getElementById('options-body');
    optionBodyContainer.querySelectorAll(`select[data-option-id="${optionKey}"]`).forEach(select => {
        const previouslySelectedValue = select.value;
        select.innerHTML = "";
        const selectOptions = getValuesAsSelectOption(optionValues);
        select.append(...selectOptions);
        const matchingOption = Array.from(select.options).find(option => option.value === previouslySelectedValue);
        if (matchingOption) {
            select.value = previouslySelectedValue;
        }
    });
}

export function addNewProductEntry(productId = null, collapsed = false) {
    const newProductId = productId == null ? products[products.length - 1] + 1 : productId;
    products.push(newProductId);
    console.log(products);
    return [
        addProductForOption(newProductId),
        addProductForSpec(newProductId),
        addNewProductGroupTemplate(newProductId, collapsed)
    ];
}

function addProductForOption(productId) {
    const optionBodyContainer = document.getElementById('options-body');
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
    return optionProductItem;
}

function addProductForSpec(productId) {
    const specBodyContainer = document.getElementById('spec-body');
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
    return specProductItem;
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
    const optionBodyContainer = document.getElementById('options-body');
    optionBodyContainer.querySelectorAll(`tr[data-product-id="${productId}"]`).forEach(
        item => item.remove()
    );
}


/* Product Specifications Section */
const specMap = new Map();

function initializeProductSpecificationSection() {
    document.getElementById('add-spec-btn').addEventListener('click', function () {
        const key = prompt('Enter a new spec name:');
        addSpecificationKey(key);
    });
}

export function addSpecificationKey(key) {
    const specValueContainer = document.getElementById('spec-values-container');
    if (key && !specMap.has(key)) {
        specMap.set(key, []);
        const specValueItem = specValueContainer.querySelector('.spec-value-item').cloneNode(true);
        specValueItem.classList.remove('hidden');
        specValueItem.dataset.specItemId = key;
        specValueItem.querySelector('.spec-key').innerHTML = `${key}:`;
        specValueContainer.appendChild(specValueItem);
        initializeSpecValueButtons(specValueItem, key);
        const specHeaderWrapper = document.getElementById('spec-header-tr');
        const specHeaderItem = specHeaderWrapper.querySelector('.spec-header-th').cloneNode(true);
        specHeaderItem.dataset.specId = key;
        specHeaderItem.innerHTML = key;
        specHeaderWrapper.appendChild(specHeaderItem);
        addProductSpecSelection(key);
        return specValueItem;
    } else if (specMap.has(key)) {
        return specValueContainer.querySelector(`.spec-value-item[data-spec-item-id="${key}"]`);
    }
    return null;
}

export function addSpecificationValue(specValueItem, specKey, value) {
    if (value && !specMap.get(specKey).includes(value)) {
        specMap.get(specKey).push(value);
        specValueItem.querySelector('.spec-values').innerHTML = specMap.get(specKey).join(', ');
        updateProductSpecSelectionValue(specKey);
    }
    console.log(specMap);
}

function initializeSpecValueButtons(specValueItem, specKey) {
    specValueItem.querySelector('.add-value-btn').addEventListener('click', function () {
        const value = prompt(`Enter a new value for: ${specKey}`);
        addSpecificationValue(specValueItem, specKey, value);
    });
    specValueItem.querySelector('.pop-value-btn').addEventListener('click', function () {
        specMap.get(specKey).pop();
        specValueItem.querySelector('.spec-values').innerHTML = specMap.get(specKey).join(', ');
        updateProductSpecSelectionValue(specKey);
        console.log(specMap);
    });
    specValueItem.querySelector('.remove-option-btn').addEventListener('click', function () {
        specMap.delete(specKey);
        specValueItem.remove();
        const specHeaderWrapper = document.getElementById('spec-header-tr');
        specHeaderWrapper.querySelector(`[data-spec-id="${specKey}"]`).remove();
        const specBodyContainer = document.getElementById('spec-body');
        specBodyContainer.querySelectorAll('.spec-product-selection').forEach(selectionWrapper => {
            if (selectionWrapper.querySelector(`select[data-spec-id="${specKey}"]`)) {
                selectionWrapper.remove();
            }
        });
        console.log(specMap);
    });
}

function addProductSpecSelection(specKey) {
    const specBodyContainer = document.getElementById('spec-body');
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
    const specBodyContainer = document.getElementById('spec-body');
    specBodyContainer.querySelectorAll(`select[data-spec-id="${specKey}"]`).forEach(select => {
        const previouslySelectedValue = select.value;
        select.innerHTML = "";
        const specSelectOptions = getValuesAsSelectOption(specValues);
        select.append(...specSelectOptions);
        const matchingOption = Array.from(select.options).find(option => option.value === previouslySelectedValue);
        if (matchingOption) {
            select.value = previouslySelectedValue;
        }
    });
}

function removeProductItemFromSpec(productId) {
    const specBodyContainer = document.getElementById('spec-body');
    specBodyContainer.querySelectorAll(`tr[data-product-id="${productId}"]`).forEach(
        item => item.remove()
    );
}


/* Product Group section */
export const data_allProductImages = new Map();
export const data_allProductDescriptionImages = new Map();

function addNewProductGroupTemplate(productId, collapsed) {
    const productGroupContainer = document.getElementById('product-group-container');
    const productGroupItem = productGroupContainer.querySelector('.product-group-template').cloneNode(true);
    productGroupItem.classList.remove('hidden');
    productGroupItem.dataset.productId = productId;
    productGroupItem.querySelector('.product-number').innerHTML = 'Product ' + productId;
    productGroupItem.querySelector('.feature-entry').remove(); // remove uninitialized one
    productGroupContainer.appendChild(productGroupItem);
    data_allProductImages.set(productId, []);
    data_allProductDescriptionImages.set(productId, []);

    productGroupItem.querySelector('.delete-product-btn').onclick = function() {
        const confirmDelete = confirm('Are you sure you want to delete this product?');
        if (confirmDelete)
            removeProductInfo(productId);
    };
    productGroupItem.querySelector('.toggle-collapse').addEventListener('click', function() {
        productGroupItem.querySelector('.product-details').classList.toggle('hidden');
    });
    if (collapsed)
        productGroupItem.querySelector('.toggle-collapse').click();
    productGroupItem.querySelector('.add-feature-btn').addEventListener('click', function () {
        addProductFeature(productGroupItem);
    });
    const addProductImageInput = productGroupItem.querySelector('.add-product-image-input');
    addProductImageInput.addEventListener('change', function () {
        setInputImageChangeListener(
            addProductImageInput,
            data_allProductImages.get(productId),
            productGroupItem.querySelector('.product-images')
        );
    });
    productGroupItem.querySelector('.add-product-image-btn').addEventListener('click', function () {
        addProductImageInput.click();
    });
    productGroupItem.querySelector('.add-product-description-btn').addEventListener('click', function () {
        addProductDescription(productGroupItem, productId);
    });
    return productGroupItem;
}

export function removeProductInfo(productId) {
    deleteProductData(productId);
    const productGroupContainer = document.getElementById('product-group-container');
    productGroupContainer.querySelector(`[data-product-id="${productId}"]`).remove();
    removeProductItemFromOption(productId);
    removeProductItemFromSpec(productId);
}

export function addProductFeature(productGroupItem) {
    const productFeatureContainer = productGroupItem.querySelector('.product-features');
    if (productFeatureContainer.children.length > 10) {
        alert('Max of 10 features only');
        return;
    }
    const productGroupContainer = document.getElementById('product-group-container');
    const featureEntry = productGroupContainer.querySelector('.feature-entry').cloneNode(true);
    productFeatureContainer.appendChild(featureEntry);
    featureEntry.querySelector('.delete-feature-btn').addEventListener('click', function () {
        featureEntry.remove();
    });
    return featureEntry;
}

export function addProductDescription(productGroupItem, productId) {
    const productDescriptionContainer = productGroupItem.querySelector('.product-descriptions');
    return addDescription(productDescriptionContainer, data_allProductDescriptionImages.get(productId));
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