document.addEventListener("DOMContentLoaded", async () => {
    window.addEventListener("popstate", async () => {
        await initiate();
    });
    await initiate();
});

const currentSearchString = document.getElementById('current-search-string').innerText;
const currentPage = document.getElementById('current-page').innerText;

const selectedFilters = {};
// called at beginning only otherwise overwrite
async function initiate() {
    const getFeature = document.getElementById('get-feature').innerText;
    const currentFilter = document.getElementById('current-filter').innerText;

    // document.getElementById('current-page').remove();
    // document.getElementById('get-feature').remove();
    // document.getElementById('current-filter').remove();

    currentFilter.split(',').forEach(pair => {
        const [key, valueString] = pair.split(':');
        if (key && valueString) {
            selectedFilters[key] = valueString.split('|');
        }
    });
    const queryParams = new URLSearchParams({
        name: currentSearchString.slice(0, 100), // max 100 chars only
        page: currentPage.toString(),
        feature: getFeature,
        filters: currentFilter,
    });
    await searchProduct(queryParams);
}

async function searchProduct(queryParams) {
    try {
        // const baseUrl = 'http://localhost:8080/api/product/by-name';
        // const url = `${baseUrl}?${queryParams.toString()}`;
        // const response = await fetch(url);
        // if (!response.ok) {
        //     displayNoSearchResult('Failed searching product');
        //     return;
        // }
        // const searchResult = await response.json();
        const searchResult = {
            "filterSpecs": {
                "price": [
                    {
                        "count": 1,
                        "option": "100.00"
                    }
                ],
                "CPU": [
                    {
                        "count": 1,
                        "option": "Core i9"
                    },
                    {
                        "count": 1,
                        "option": "Core i7"
                    }
                ],
                "category": [
                    {
                        "count": 1,
                        "option": "Electronics"
                    }
                ],
                "brand": [
                    {
                        "count": 1,
                        "option": "Brand 1"
                    }
                ],
                "RAM": [
                    {
                        "count": 1,
                        "option": "16GB"
                    }
                ]
            },
            "productResults": {
                "content": [
                    {
                        "productLineId": 1,
                        "id": 1,
                        "manufacturerId": "Man part 1",
                        "name": "Product Name 1",
                        "quantity": 5,
                        "price": 100.00,
                        "features": [
                            "Product 1 feature 1",
                            "Product 1 feature 2",
                            "Product 1 feature 3",
                            "Product 1 feature 4",
                            "Product 1 feature 5"
                        ],
                        "imageName": "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg",
                        "discountedPrice": null,
                        "newRelease": true
                    },
                    {
                        "productLineId": 1,
                        "id": 2,
                        "manufacturerId": "Man part 1",
                        "name": "Product Name 2",
                        "quantity": 0,
                        "price": 100.00,
                        "features": [
                            "Product 1 feature 1",
                            "Product 1 feature 2",
                            "Product 1 feature 3",
                            "Product 1 feature 4",
                            "Product 1 feature 5"
                        ],
                        "imageName": "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg",
                        "discountedPrice": 50,
                        "newRelease": true
                    }
                ],
                "page": {
                    "size": 10,
                    "number": 0,
                    "totalElements": 2,
                    "totalPages": 2
                }
            }
        }
        if (searchResult.productResults.page.totalElements) {
            displayFilterOptions(searchResult.filterSpecs);
            displayResultPageInfo(searchResult.productResults.page);
            displaySearchResult(searchResult.productResults.content);
            displayPageInfo(searchResult.productResults.page);
        } else {
            displayNoSearchResult('No result found with search');
        }
    } catch (error) {
        console.error('Error searching for product:', error);
        displayNoSearchResult('Error searching for product');
    }
}

function displayFilterOptions(filters) {
    const filterContainer = document.getElementById('filter-container');
    const filterItemTem = filterContainer.querySelector('.filter-item');
    Object.entries(filters).forEach(([key, values]) => {
        const filterItem = filterItemTem.cloneNode(true);
        filterItem.querySelector('.filter-title').textContent = key.charAt(0).toUpperCase() + key.slice(1);
        const filterOptionTem = filterItem.querySelector('.filter-option');
        const filterOptionContainer = filterItem.querySelector('.filter-option-container');
        values.forEach(value => {
            const filterOption = filterOptionTem.cloneNode(true);
            const optionInput = filterOption.querySelector('#filter-option-input');
            optionInput.id = value.option;
            const filterOptionLabel = filterOption.querySelector('label[for="filter-option-input"]');
            filterOptionLabel.setAttribute('for', value.option);
            filterOption.querySelector('.option-name').innerText = value.option;
            filterOption.querySelector('.option-count').innerText = value.count;
            filterOptionContainer.appendChild(filterOption);

            optionInput.checked = selectedFilters[key] && selectedFilters[key].includes(value.option);
            filterOption.querySelector('a').href = createTempUrl(currentSearchString, currentPage, key, value.option);
            if (selectedFilters[key] && selectedFilters[key].includes(value.option)) {
                console.log(key);
                console.log(value);
            }
            optionInput.addEventListener('change', function(event) {
                const url = createUrl(currentSearchString, currentPage, key, value.option, event.target.checked);
                console.log(url);
                window.location.href = url;
            });
        });
        filterItem.querySelector('.filter-btn').addEventListener('click', function() {
            filterOptionContainer.classList.toggle('hidden');
            this.querySelector('.vertical-line').classList.toggle('rotate-90');
        });
        filterOptionTem.remove();
        filterContainer.appendChild(filterItem);
    });
    filterItemTem.remove();
}

function displaySearchResult(content) {
    const productItemContainer = document.getElementById('product-item-container');
    const productItemTem = productItemContainer.querySelector('.product-item');
    content.forEach((item) => {
        const productItem = productItemTem.cloneNode(true);
        productItem.classList.remove('hidden');
        productItem.dataset.productId = item.id;
        productItem.querySelectorAll('.product-link').forEach(link => {
            link.href = `http://localhost:8081/product/card/${item.id}`;
        });
        productItem.querySelector('.product-img').src = item.imageName;
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
    productItemTem.remove();
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
    if (page.totalElements > 0) {
        const pageLinkContainer = document.getElementById('page-link-container');
        const pageLinkItemTem = pageLinkContainer.querySelector('.page-link-item');
        for (let i = 0  ; i < page.totalPages; i++) {
            const pageLinkItem = pageLinkItemTem.cloneNode(true);
            pageLinkItem.classList.remove('hidden');
            pageLinkItem.innerHTML = i + 1;
            pageLinkItem.href = createTempUrl(currentSearchString, i, null, null);
            if (i === parseInt(currentPage)) {
                pageLinkItem.classList.add('bg-blue-200');
                pageLinkItem.addEventListener('click', (event) => {
                    event.preventDefault();
                });
            }
            pageLinkContainer.appendChild(pageLinkItem);
        }
        pageLinkItemTem.remove();
    } else {
        document.getElementById('pagination-container').remove();
    }
}

function displayNoSearchResult(content) {

}

function createUrl(searchString, page, key, value, included) {
    if (included) {
        if (!selectedFilters[key]) {
            selectedFilters[key] = [];
        }
        selectedFilters[key].push(value);
    } else {
        if (selectedFilters[key]) {
            selectedFilters[key] = selectedFilters[key].filter(v => v !== value);

            // remove the category if it's now empty
            if (selectedFilters[key].length === 0) {
                delete selectedFilters[key];
            }
        }
    }

    const filterString = Object.entries(selectedFilters)
        .map(([key, values]) => `${key}:${values.join('|')}`)
        .join(',');

    const baseUrl = 'http://localhost:8081/product/search';
    const queryParams = new URLSearchParams({
        q: searchString.slice(0, 100), // max 100 chars only
        page: page.toString(),
        feature: true,
        filters: filterString
    });
    return `${baseUrl}?${queryParams.toString()}`;
}

function createTempUrl(searchString, page, key, value) {
    let filterString = Object.entries(selectedFilters)
        .map(([key, values]) => `${key}:${values.join('|')}`)
        .join(',');
    if (key && value && !(selectedFilters[key] && selectedFilters[key].includes(value)))
        filterString += `,${key}|${value}`;
    const baseUrl = 'http://localhost:8081/product/search';
    const queryParams = new URLSearchParams({
        q: searchString.slice(0, 100), // max 100 chars only
        page: page.toString(),
        feature: true,
        filters: filterString
    });
    return `${baseUrl}?${queryParams.toString()}`;
}