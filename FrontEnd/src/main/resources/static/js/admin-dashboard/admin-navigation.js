const mainContentArea = document.getElementById('main-content-area');

document.getElementById('admin-dashboard-top-header-link').addEventListener('click', async function(e) {
    e.preventDefault();
    const newUrl = `/admin/dashboard`;
    this.href = newUrl;
    updatePageUrl(newUrl, 'admin-dashboard');
    await updateTemplateOnQueryPath();
});

const addNewQuery = 'addProduct'
const addProductLink = document.getElementById('add-product-link');
addProductLink.addEventListener('click', async function(e) {
    e.preventDefault();
    const newUrl = `/admin/dashboard?query=${addNewQuery}`;
    this.href = newUrl;
    updatePageUrl(newUrl, addNewQuery);
    await getAddNewProductTemplate();
});

export const updateProductQuery = 'updateProduct';
const updateProductLink = document.getElementById('update-product-link');
updateProductLink.addEventListener('click', async function(e) {
    e.preventDefault();
    const newUrl = `/admin/dashboard?query=${updateProductQuery}`;
    this.href = newUrl;
    updatePageUrl(newUrl, updateProductQuery);
    await getUpdateProductTemplate()
});

const resetClickedLinks = () => {
    addProductLink.classList.remove('bg-blue-300');
    updateProductLink.classList.remove('bg-blue-300');
};

async function getAddNewProductTemplate() {
    resetClickedLinks();
    addProductLink.classList.add('bg-blue-300');
    await getAdminProductTemplate('/admin/dashboard/addNewProduct', mainContentArea);
    import('./add-new-product.js')
        .then((module) => {
            module.initializeAdd();
        });
    import('./post-new-product.js')
        .then((module) => {
            module.initializePost();
        });
}

async function getUpdateProductTemplate() {
    resetClickedLinks();
    updateProductLink.classList.add('bg-blue-300');
    await getAdminProductTemplate('/admin/dashboard/updateProduct', mainContentArea);
    import('./retrieve-product.js')
        .then((module) => {
            // You can use `module` here
            module.initializeUpdate();
            const product = new URLSearchParams(window.location.search).get('product');
            const productLine = new URLSearchParams(window.location.search).get('line');
            if (product)
                module.handleProductResult(product, productLine);
        });
}

document.addEventListener("DOMContentLoaded", async () => {
    window.addEventListener("popstate", async () => {
        updateTemplateOnQueryPath();
    });
    updateTemplateOnQueryPath();
});

async function updateTemplateOnQueryPath() {
    const query = new URLSearchParams(window.location.search).get('query');
    console.log('query: ', query);
    if (query === addNewQuery) {
        await getAddNewProductTemplate();
    } else if (query === updateProductQuery) {
        await getUpdateProductTemplate();
    } else {
        mainContentArea.innerHTML = '';
    }
}

export function updatePageUrl(newUrl, query) {
    const currentUrl = window.location.pathname + window.location.search;
    if (currentUrl !== newUrl) {
        history.pushState({ query }, "", newUrl);
        console.log('newUrl', newUrl);
    }
}

async function getAdminProductTemplate(endPoint, container) {
    const content = await fetch(endPoint, {
        method: 'GET',
        cache: 'no-store'
    })
        .then(response => {
            if (response.ok) // created
                return response.text();
            throw new Error('Fail to get product content template');
        })
        .then(data => {
            console.log('Success getting product content template');
            return data;
        })
        .catch(error => {
            console.error('Error getting add product content template');
            return null;
        });
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = content;
    const innerScripts = tempDiv.querySelectorAll('script');
    const newScripts = [];
    innerScripts.forEach(innerScript => {
        const existingScript = Array.from(document.body.querySelectorAll('script')).find(
            s => s.src && innerScript.src && s.src.includes(innerScript.src)
        ) || null;
        innerScript.remove();
        if (existingScript)
            existingScript.remove();
        const script = document.createElement('script');
        script.type = 'module';
        script.src = innerScript.src;
        newScripts.push(script);
    });
    container.innerHTML = tempDiv.innerHTML;
    newScripts.forEach(script => {
        document.body.appendChild(script);
    });
}
