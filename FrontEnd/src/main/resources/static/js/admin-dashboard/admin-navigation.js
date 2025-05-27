const mainContentArea = document.getElementById('main-content-area');

const addNewQuery = 'addProduct'
document.getElementById('add-product-link').addEventListener('click', async function(e) {
    e.preventDefault();
    const newUrl = `/admin/dashboard?query=${addNewQuery}`;
    this.href = newUrl;
    updatePageUrl(newUrl, addNewQuery);
    await getAddNewProductTemplate();
});

export const updateProductQuery = 'updateProduct';
document.getElementById('update-product-link').addEventListener('click', async function(e) {
    e.preventDefault();
    const newUrl = `/admin/dashboard?query=${updateProductQuery}`;
    this.href = newUrl;
    updatePageUrl(newUrl, updateProductQuery);
    await getUpdateProductTemplate()
});

async function getAddNewProductTemplate() {
    await getAdminProductTemplate('/admin/dashboard/addNewProduct', mainContentArea);
    import('./add-new-product.js')
        .then((module) => {
            // You can use `module` here
            module.initializeAdd();
        });
}

async function getUpdateProductTemplate() {
    await getAdminProductTemplate('/admin/dashboard/updateProduct', mainContentArea);
    import('./retrieve-product.js')
        .then((module) => {
            // You can use `module` here
            module.initializeUpdate();
        });
}

document.addEventListener("DOMContentLoaded", async () => {
    window.addEventListener("popstate", async (event) => {
        updateTemplateOnQueryPath();
    });
    updateTemplateOnQueryPath();
});

async function updateTemplateOnQueryPath() {
    const query = new URLSearchParams(window.location.search).get("query");
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

function removeDynamicScript() {
    const scripts = document.getElementsByTagName('script');
    Array.from(scripts).forEach(script => {
        if (script.src && script.src.includes('?_ts=')) {
            console.log(script.src);
            script.remove();
        }
    });
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
