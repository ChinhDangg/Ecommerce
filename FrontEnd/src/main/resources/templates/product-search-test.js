

async function fetchProductFilterFields(productId) {
    if (!productId) {
        throw new Error('No product id found.');
    }
    const response = await fetch(`localhost:8080/api/product/${productId}/filters`);
    if (!response.ok) {
        throw new Error('Failed to fetch product filters');
    }
    return await response.json();
}

async function searchProduct(productNameSearch) {
    try {
        const page = 0;
        const baseUrl = 'http://localhost:8080/api/product/by-name';
        const queryParams = new URLSearchParams({
            name: productNameSearch,
            page: page.toString(),
            getFeatures: true
        });
        const url = `${baseUrl}?${queryParams.toString()}`;
        const response = await fetch(url);
        if (!response.ok) {
            displayNoSearchResult('Failed searching product');
            return;
        }
        const searchResult = await response.json();
        if (searchResult.page.totalElements) {
            displaySearchResult(searchResult.content);
        } else {
            displayNoSearchResult('No result found with search');
        }
    } catch (error) {
        console.error('Error searching for product:', error);
        displayNoSearchResult('Error searching for product');
    }
}