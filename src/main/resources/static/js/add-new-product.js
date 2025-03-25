const productLineImageBtn = document.getElementById('add-line-image-btn');
const productLineImageContainer = document.getElementById('product-line-images');
const productLineImageInput = document.getElementById('add-line-image-input');
let productLineImages = []

productLineImageBtn.addEventListener('click', function() {
    productLineImageInput.click();
});

productLineImageInput.addEventListener('change', function() {
    productLineImages = Array.from(productLineImageInput.files);
    const imageEntryTemplate = document.querySelector('.image-entry-template').cloneNode(true);
    imageEntryTemplate.classList.remove('hidden');
    let max = 5;
    productLineImages.some(file => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const newImageEntry = imageEntryTemplate.cloneNode(true);
            newImageEntry.querySelector('.image-entry-img').src = e.target.result;
            initializeImageButtons(newImageEntry);
            productLineImageContainer.appendChild(newImageEntry);
        }
        reader.readAsDataURL(file); // start the reading process which trigger the onload
        max--;
        if (max <= 0)
            return true;
    });
})

function initializeImageButtons(imageContainer) {
    imageContainer.querySelector('.delete-image-btn').addEventListener('click', function() {
        let index = getChildIndex(imageContainer);
        productLineImages.splice(index, 1);
        imageContainer.remove();
    });
    imageContainer.querySelector('.move-left-btn').addEventListener('click', function() {
        const previousSib = imageContainer.previousElementSibling;
        if (previousSib) {
            const currentIndex = getChildIndex(imageContainer);
            [productLineImages[currentIndex], productLineImages[currentIndex-1]] = [productLineImages[currentIndex-1], productLineImages[currentIndex]]
            console.log(productLineImages);
            productLineImageContainer.insertBefore(imageContainer, previousSib);
        }
    });
}

function getChildIndex(child) {
    let index = 0;
    while (child = child.previousElementSibling)
        index++;
    return index;
}

