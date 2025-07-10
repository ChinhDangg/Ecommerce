

async function searchProduct(searchString, selectedFilters) {
    try {
        // const page = 0;
        // const baseUrl = 'http://localhost:8080/api/product/by-name';
        // const queryParams = new URLSearchParams({
        //     name: searchString.slice(0, 100), // max 100 chars only
        //     page: page.toString(),
        //     feature: true
        // });
        // for (const [key, value] of Object.entries(selectedFilters)) {
        //     queryParams.append(key, value);
        // }
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
                        "value": "100.00"
                    }
                ],
                "CPU": [
                    {
                        "count": 1,
                        "option": "Core i9"
                    }
                ],
                "category": [
                    {
                        "count": 1,
                        "value": "Electronics"
                    }
                ],
                "brand": [
                    {
                        "count": 1,
                        "value": "Brand 1"
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
                            "Product 1 feature 3"
                        ],
                        "imageName": "/images/水淼Aqua cosplay Tsukatsuki Rio - Blue Archive (5).jpg",
                        "discountedPrice": null,
                        "newRelease": true
                    }
                ],
                "page": {
                    "size": 10,
                    "number": 0,
                    "totalElements": 1,
                    "totalPages": 1
                }
            }
        }
        if (searchResult.productResults.page.totalElements) {
            displaySearchResult(searchResult.productResults.content);
        } else {
            displayNoSearchResult('No result found with search');
        }
    } catch (error) {
        console.error('Error searching for product:', error);
        displayNoSearchResult('Error searching for product');
    }
}

function displaySearchResult(content) {
    const productItemContainer = document.getElementById('product-item-container');
    const productItemTem = productItemContainer.querySelector('.product-item');
    content.forEach((item) => {
        const productItem = productItemTem.cloneNode(true);
        productItem.classList.remove('hidden');
        productItem.querySelector('.product-img').src = item.imageName;
        productItem.querySelector('.product-name').innerHTML = item.name;
        productItem.querySelector('.product-id').innerHTML = `CPN # ${item.id} | MFR # ${item.manufacturerId}`;
        const featureList = productItem.querySelector('.product-feature-list');
        item.features.forEach(feature => {
            const list = document.createElement('li');
            list.textContent = feature.toString();
            featureList.appendChild(list);
        });
        if (item.discountedPrice) {
            productItem.querySelector('.sale-price').innerHTML =
                Number(item.discountedPrice).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
            productItem.querySelector('.price').innerHTML =
                Number(item.price).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
            const savedPrice = Number(item.price) - Number(item.discountedPrice);
            productItem.querySelector('.saved-price').innerHTML =
                Number(savedPrice).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
        } else {
            productItem.querySelector('.sale-price').innerHTML =
                Number(item.price).toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});
            productItem.querySelector('.price').remove();
            productItem.querySelector('.saved-price').remove();
        }

        productItemContainer.appendChild(productItem);
    });
}