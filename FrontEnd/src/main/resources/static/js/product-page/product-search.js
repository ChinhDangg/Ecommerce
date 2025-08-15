document.addEventListener("DOMContentLoaded", async () => {
    window.addEventListener("popstate", async (event) => {
        console.log('called');
        await initiate(event.state);
    });
    await initiate();
    // const form = document.getElementById('search-bar-form');
    // const newForm = form.cloneNode(true);
    // form.replaceWith(newForm); // to remove default listener
    // newForm.addEventListener('submit', async (e) => {
    //     e.preventDefault();
    //     const searchInput = document.getElementById('search-input');
    //     if (searchInput.value) {
    //         currentSearchString = searchInput.value;
    //         const url = createTempUrl(currentPage, currentSort, null, null, null, searchInput.value, searchURL);
    //         await searchProduct(url);
    //     }
    //     else {
    //         window.location.href = '/';
    //     }
    // });
});

const mediaURL = document.getElementById('media-url').innerText;
const searchURL = document.getElementById('search-url').innerText;
const searchPageURL = document.getElementById('searchPage-url').innerText;
const cardPageURL = document.getElementById('cardPage-url').innerText;

let currentSearchString = document.getElementById('current-search-string').innerText;
let currentPage = document.getElementById('current-page').innerText;
let currentSort = document.getElementById('current-sort').innerText;
const categoryId = document.getElementById('category-id').innerText;

const selectedSpecialFilters = {};
const selectedSpecFilters = {};
// called at the beginning once only otherwise overwrite
async function initiate(state = null) {
    // document.getElementById('current-page').remove();
    // document.getElementById('current-sort').remove();
    // document.getElementById('current-s-filter').remove();
    // document.getElementById('current-filter').remove();
    // document.getElementById('call-url').remove();
    // document.getElementById('category-id').remove();
    // document.getElementById('redirect-url').remove();
    // document.getElementById('card-url').remove();

    document.getElementById('search-input').value = currentSearchString;

    let currentSpecialFilter;
    let currentFilter;

    if (state) {
        currentPage = state.page;
        currentSort = state.sortBy;
        currentSpecialFilter = state.sFilter;
        currentFilter = state.filter;
        Object.keys(selectedSpecialFilters).forEach(key => delete selectedSpecialFilters[key]);
        Object.keys(selectedSpecFilters).forEach(key => delete selectedSpecFilters[key]);
    } else {
        currentSpecialFilter = document.getElementById('current-s-filter').innerText;
        currentFilter =  document.getElementById('current-filter').innerText;
    }

    const sortSelection = document.getElementById('sort-by-selection');
    sortSelection.value = currentSort;
    if (!sortSelection.value) {
        sortSelection.selectedIndex = 0;
    }

    const queryParams = new URLSearchParams({
        feature: true,
    });
    if (currentPage)
        queryParams.append('page', currentPage.toString());
    if (currentSearchString)
        queryParams.append('q', getValidatedSearchString(currentSearchString)) // max 100 chars only
    if (categoryId)
        queryParams.append('cateId', categoryId);
    if (currentSort)
        queryParams.append('sort', currentSort);
    if (currentSpecialFilter) {
        queryParams.append('s-filters', currentSpecialFilter);
        currentSpecialFilter.split(',').forEach(pair => {
            const [key, valueString] = pair.split(':');
            if (key && valueString) {
                selectedSpecialFilters[key] = valueString.split('|');
            }
        });
    }
    if (currentFilter) {
        queryParams.append('filters', currentFilter);
        currentFilter.split(',').forEach(pair => {
            const [key, valueString] = pair.split(':');
            if (key && valueString) {
                selectedSpecFilters[key] = valueString.split('|');
            }
        });
    }

    const url = `${searchURL}?${queryParams.toString()}`;
    await searchProduct(url);
}

async function searchProduct(url) {
    const response = await fetch(url);
    if (!response.ok) {
        displayNoSearchResult('Failed searching product');
        return;
    }
    const searchResult = await response.json();
    showProduct(searchResult);
}

function showProduct(searchResult) {
    try {
        clearFilterSelection();
        clearProductSelection();
        clearPageSelection();
        if (searchResult.productResults.page.totalElements) {
            displayFilterOptions(searchResult.specialFilters, selectedSpecialFilters);
            displayFilterOptions(searchResult.specFilters, selectedSpecFilters);
            displayResultPageInfo(searchResult.productResults.page);
            displayProductSearchResult(searchResult.productResults.content);
            displayPageInfo(searchResult.productResults.page);
        } else {
            displayNoSearchResult('No result found with search: ' + currentSearchString);
        }
    } catch (error) {
        console.error('Error searching for product:', error);
        displayNoSearchResult('Error searching for product');
    }
}

function pushHistory(pushURL) {
    const urlParams = new URLSearchParams(new URL(pushURL).search);
    history.pushState(
        {
            page: urlParams.get('page'),
            sortBy: urlParams.get('sort'),
            sFilter: urlParams.get('s-filters'),
            filter: urlParams.get('filters'),
        },
        '', pushURL);
}

document.getElementById('sort-by-selection').addEventListener('change', async function (event) {
    currentSort = event.target.value;
    currentSort = currentSort === this.options[0].value ? null : currentSort;
    const pushURL = createTempUrl(currentPage, currentSort, null, null, null, currentSearchString, searchPageURL);
    pushHistory(pushURL);
    await searchProduct(createTempUrl(currentPage, currentSort, null, null, null, currentSearchString, searchURL));
});

function displayFilterOptions(filters, selectedFilters) {
    const filterContainer = document.getElementById('filter-container');
    const filterItemTem = filterContainer.querySelector('.filter-item');
    Object.entries(filters).forEach(([key, values]) => {
        const filterItem = filterItemTem.cloneNode(true);
        filterItem.classList.remove('hidden');
        filterItem.querySelector('.filter-title').textContent = key.charAt(0).toUpperCase() + key.slice(1);
        const filterOptionTem = filterItem.querySelector('.filter-option');
        const filterOptionContainer = filterItem.querySelector('.filter-option-container');
        let containCheckedInput = false;
        values.forEach(value => {
            const filterOption = filterOptionTem.cloneNode(true);
            const optionInput = filterOption.querySelector('#filter-option-input');
            optionInput.id = value.option;
            const filterOptionLabel = filterOption.querySelector('label[for="filter-option-input"]');
            filterOptionLabel.setAttribute('for', value.option);
            filterOption.querySelector('.option-name').innerText = value.option;
            filterOption.querySelector('.option-count').innerText = value.count;
            if (!value.count) {
                optionInput.disabled = true;
                filterOption.classList.remove('hover:bg-gray-200', 'text-gray-700');
                filterOption.classList.add('text-gray-300');
            } else {
                filterOption.querySelector('a').href = createTempUrl(currentPage, currentSort, key, value.option, selectedFilters);
                optionInput.addEventListener('change', async function(event) {
                    await searchProduct(createUrl(key, value.option, event.target.checked, selectedFilters, searchURL));
                    const pushURL = createTempUrl(currentPage, currentSort, null, null, null, currentSearchString, searchPageURL);
                    pushHistory(pushURL);
                });
            }
            filterOptionContainer.appendChild(filterOption);

            optionInput.checked = selectedFilters[key] && selectedFilters[key].includes(value.option);
            if (optionInput.checked)
                containCheckedInput = true;
        });
        filterOptionTem.remove();
        filterItem.querySelector('.filter-btn').addEventListener('click', function() {
            filterOptionContainer.classList.toggle('hidden');
            this.querySelector('.vertical-line').classList.toggle('rotate-90');
        });
        if (containCheckedInput)
            filterItem.querySelector('.filter-btn').click();
        filterContainer.appendChild(filterItem);
    });
}

function displayProductSearchResult(content) {
    document.getElementById('content-container').classList.remove('hidden');
    const productItemContainer = document.getElementById('product-item-container');
    const productItemTem = productItemContainer.querySelector('.product-item');
    content.forEach((item) => {
        const productItem = productItemTem.cloneNode(true);
        productItem.classList.remove('hidden');
        productItem.dataset.productId = item.id;
        productItem.querySelectorAll('.product-link').forEach(link => {
            link.href = `${cardPageURL}/${item.id}`;
        });
        productItem.querySelector('.product-img').src = `${mediaURL}/${item.imageName}`;
        productItem.querySelector('.product-name').innerHTML = item.name;
        productItem.querySelector('.product-id').innerHTML = `CPN # ${item.id} | MFR # ${item.manufacturerId}`;
        const featureList = productItem.querySelector('.product-feature-list');
        for (let i = 0; i < item.features.length; i++) {
            const list = document.createElement('li');
            list.textContent = item.features[i].toString();
            featureList.appendChild(list);
            if (i > 3) {
                list.classList.add('hidden');
            }
        }
        if (item.features.length > 3) {
            productItem.querySelector('.show-more-btn').addEventListener('click', function () {
                for (let i = 4; i < item.features.length; i++) {
                    const list = featureList.children[i];
                    list.classList.remove('hidden');
                }
                this.classList.add('hidden');
                productItem.querySelector('.show-less-btn').classList.remove('hidden');
            });
            productItem.querySelector('.show-less-btn').addEventListener('click', function () {
                for (let i = 4; i < item.features.length; i++) {
                    const list = featureList.children[i];
                    list.classList.add('hidden');
                }
                this.classList.add('hidden');
                productItem.querySelector('.show-more-btn').classList.remove('hidden');
            });
        } else {
            productItem.querySelector('.show-more-btn').remove();
            productItem.querySelector('.show-less-btn').remove();
        }
        if (item.discountedPrice) {
            productItem.querySelector('.sale-price').innerHTML =
                '$' + Number(item.discountedPrice).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
            productItem.querySelector('.price').innerHTML =
                '$' + Number(item.price).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
            const savedPrice = Number(item.price) - Number(item.discountedPrice);
            productItem.querySelector('.saved-price').innerHTML =
                'Save $' + (savedPrice).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
        } else {
            productItem.querySelector('.sale-price').innerHTML =
                '$' + Number(item.price).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
            productItem.querySelector('.price').remove();
            productItem.querySelector('.saved-price').remove();
            productItem.querySelector('.price-end-date').remove();
        }
        if (!item.newRelease) {
            productItem.querySelector('.new-release-label').remove();
        }
        if (item.quantity > 0) {
            productItem.querySelector('.out-stock-label').remove();
            productItem.querySelector('.out-of-stock-btn').remove();
        } else {
            productItem.querySelector('.in-stock-label').remove();
            productItem.querySelector('.add-to-cart-btn').remove();
        }
        productItemContainer.appendChild(productItem);
    });
}

function clearFilterSelection() {
    const items = Array.from(document.getElementById('filter-container')
        .querySelectorAll('.filter-item'));
    items.slice(1).forEach(item => item.remove());
}

function clearProductSelection() {
    const items = Array.from(document.getElementById('product-item-container')
        .querySelectorAll('.product-item'));
    items.slice(1).forEach(item => item.remove());
}

function clearPageSelection() {
    const items = Array.from(document.getElementById('page-link-container')
        .querySelectorAll('.page-link-item'));
    items.slice(1).forEach(item => item.remove());
}

let currentView = document.getElementById('list-view-btn');
currentView.classList.add('bg-gray-200');
document.getElementById('grid-view-btn').addEventListener('click', function () {
    if (currentView === this)
        return;
    currentView.classList.remove('bg-gray-200');
    currentView = this;
    this.classList.add('bg-gray-200');
    const productItemContainer= document.getElementById('product-item-container');
    productItemContainer.classList.add('grid', 'grid-cols-1', 'md:grid-cols-2', 'xl:grid-cols-4', 'gap-4', 'min-w-0');
    productItemContainer.classList.remove('space-y-6');
    productItemContainer.querySelectorAll('.product-item').forEach(item => {
        item.classList.remove('flex-row', 'gap-6');
        item.classList.add('flex-col', 'gap-3');
        const imgContainer = item.querySelector('.img-container');
        imgContainer.classList.remove('w-48', 'h-48');
        imgContainer.classList.add('w-40', 'h-40');
        const productName = item.querySelector('.product-name');
        productName.classList.remove('text-xl');
        productName.classList.add('text-sm');
        const salePrice = item.querySelector('.sale-price');
        salePrice.classList.remove('text-2xl');
        salePrice.classList.add('text-xl');
        const price = item.querySelector('.price');
        if (price) {
            price.classList.remove('text-sm');
            price.classList.add('text-xs');
        }
        item.querySelector('.feature-container').classList.add('hidden');
    });
});

document.getElementById('list-view-btn').addEventListener('click', function () {
    if (currentView === this)
        return;
    currentView.classList.remove('bg-gray-200');
    currentView = this;
    this.classList.add('bg-gray-200');
    const productItemContainer = document.getElementById('product-item-container');
    productItemContainer.classList.remove('grid', 'grid-cols-1', 'md:grid-cols-2', 'xl:grid-cols-4', 'gap-4', 'min-w-0');
    productItemContainer.classList.add('space-y-6');
    productItemContainer.querySelectorAll('.product-item').forEach(item => {
        item.classList.remove('flex-col', 'gap-3');
        item.classList.add('flex-row', 'gap-6');
        const imgContainer = item.querySelector('.img-container');
        imgContainer.classList.remove('w-40', 'h-40');
        imgContainer.classList.add('w-48', 'h-48');
        const productName = item.querySelector('.product-name');
        productName.classList.remove('text-sm');
        productName.classList.add('text-xl');
        const salePrice = item.querySelector('.sale-price');
        salePrice.classList.remove('text-xl');
        salePrice.classList.add('text-2xl');
        const price = item.querySelector('.price');
        if (price) {
            price.classList.remove('text-xs');
            price.classList.add('text-sm');
        }
        item.querySelector('.feature-container').classList.remove('hidden');
    });
});

function displayResultPageInfo(page) {
    if (page.totalElements > 0) {
        const startCount = page.number * page.size + 1;
        document.getElementById('start-result-count').innerText = startCount.toString();
        const endCount = (page.number + 1) * page.size;
        document.getElementById('end-result-count').innerText = (endCount > page.totalElements ? page.totalElements : endCount).toString();
        document.getElementById('total-result').innerText = page.totalElements;
    }
}

function displayPageInfo(page) {
    document.getElementById('pagination-container').classList.remove('hidden');
    if (page.totalElements > 0) {
        const pageLinkContainer = document.getElementById('page-link-container');
        const pageLinkItemTem = pageLinkContainer.querySelector('.page-link-item');
        for (let i = 0  ; i < page.totalPages; i++) {
            const pageLinkItem = pageLinkItemTem.cloneNode(true);
            pageLinkItem.classList.remove('hidden')
            pageLinkItem.innerHTML = i + 1;
            const url = createTempUrl(i, currentSort, null, null, null);
            pageLinkItem.href = url;
            if (i === parseInt(currentPage) || (i === 0 && !currentPage)) {
                pageLinkItem.classList.add('bg-blue-200');
                pageLinkItem.addEventListener('click', (event) => {
                    event.preventDefault();
                });
            } else {
                pageLinkItem.addEventListener('click', async function(event) {
                    event.preventDefault();
                    pushHistory(url);
                    await searchProduct(createTempUrl(i, currentSort, null, null, null, currentSearchString, searchURL));
                });
            }
            pageLinkContainer.appendChild(pageLinkItem);
        }
    } else {
        document.getElementById('pagination-container').remove();
    }
}

function displayNoSearchResult(content) {
    document.getElementById('content-container').classList.add('hidden');
    document.getElementById('pagination-container').classList.add('hidden');
    document.getElementById('search-string-container').innerText = content;
}

function createUrl(key, value, included, selectedFilters, url = searchPageURL) {
    if (included) {
        if (!selectedFilters[key]) {
            selectedFilters[key] = [];
        }
        selectedFilters[key].push(value);
    } else if (included != null) {
        if (selectedFilters[key]) {
            selectedFilters[key] = selectedFilters[key].filter(v => v !== value);

            // remove the category if it's now empty
            if (selectedFilters[key].length === 0) {
                delete selectedFilters[key];
            }
        }
    }

    return createTempUrl(currentPage, currentSort, null, null, null, currentSearchString, url);
}

function createTempUrl(page, sortBy, key, value, selectedFilters, searchString = currentSearchString, url = searchPageURL) {
    let specialFilterString = Object.entries(selectedSpecialFilters)
        .map(([key, values]) => `${key}:${values.join('|')}`)
        .join(',');

    let filterString = Object.entries(selectedSpecFilters)
        .map(([key, values]) => `${key}:${values.join('|')}`)
        .join(',');

    if (key && value) {
        if (!selectedFilters)
            throw new Error('selectedFilters is required');
        if (!(selectedFilters[key] && selectedFilters[key].includes(value)))
            filterString += `,${key}|${value}`;
    }

    const queryParams = new URLSearchParams({
        feature: true,
    });
    if (page)
        queryParams.append('page', page.toString());
    if (searchString)
        queryParams.append('q', getValidatedSearchString(searchString)) // max 100 chars only
    if (categoryId)
        queryParams.append('cateId', categoryId);
    if (currentSort)
        queryParams.append('sort', sortBy);
    if (specialFilterString)
        queryParams.append('s-filters', specialFilterString);
    if (filterString)
        queryParams.append('filters', filterString);
    return `${url}?${queryParams.toString()}`;
}

function getValidatedSearchString(searchString) {
    if (searchString) {
        return searchString.slice(0, 100);
    }
    return false;
}