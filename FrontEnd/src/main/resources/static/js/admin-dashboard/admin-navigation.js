const mainContentArea = document.getElementById('main-content-area');

const addNewQuery = 'addNew'
document.getElementById('add-product-link').addEventListener('click', async function(e) {
    e.preventDefault();
    const newUrl = `/admin/dashboard?query=${addNewQuery}`;
    this.href = newUrl;

    const currentUrl = window.location.pathname + window.location.search;
    if (currentUrl !== newUrl) {
        history.pushState({ addNewQuery }, "", newUrl);
    }

    await getAddNewProductTemplate();
});

async function getAddNewProductTemplate() {
    const content = await fetch('/admin/dashboard/addNewProduct', {
        method: 'GET',
    })
        .then(response => {
            if (response.ok) // created
                return response.text();
            throw new Error('Fail to get add new product content');
        })
        .then(data => {
            console.log('Success getting add new product content');
            return data;
        })
        .catch(error => {
            console.error('Error getting add new product content');
            return null;
        });
    const tempDiv = document.createElement('div');
    tempDiv.innerHTML = content;
    const innerScripts = tempDiv.querySelectorAll('script');
    innerScripts.forEach(innerScript => {
        const existingScript = Array.from(document.body.querySelectorAll('script')).find(script => {
            if (!script.src) return false;
            return script.src.includes(innerScript.src);
        });
        if (existingScript)
            return;
        const script = document.createElement('script');
        script.type = 'module';
        script.src = innerScript.src;
        document.body.appendChild(script);
        innerScript.remove();
    })
    mainContentArea.innerHTML = content;
}

document.addEventListener("DOMContentLoaded", () => {

    window.addEventListener("popstate", (event) => {
        const query = new URLSearchParams(window.location.search).get("query");
        if (query === addNewQuery) {
            getAddNewProductTemplate();
        } else {
            mainContentArea.innerHTML = '';
        }
    });

    const initQuery = new URLSearchParams(window.location.search).get("query");
    if (initQuery === addNewQuery) {
        getAddNewProductTemplate();
    }
});
