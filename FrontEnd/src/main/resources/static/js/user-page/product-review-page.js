
window.onload = function() {
    initialize();
}

async function initialize() {
    const pid = new URLSearchParams(window.location.search).get("pid");
    if (!pid) {
        alert("Bad url");
        window.location.href = '/user/account/order';
    }
    const reviewInfo = await getProductReviewInfo(pid);
    displayProductInfo(reviewInfo);
    displayPreviousProductReview(reviewInfo);
    initializeRatingStar();
    initializeImageInput();
    document.getElementById('submit-review-btn').addEventListener('click', async function() {
        await postUserProductReview(reviewInfo);
    });
}

async function getProductReviewInfo(productId) {
    const response = await fetch(`/api/user/review/${productId}`);
    if (response.status === 400) { // bad request
        console.error('Bad request with getting review for product')
        window.location.href = '/user/account/order'
    } else if (!response.ok) {
        console.error('Failed to get review for product')
        return;
    }

    return response.json();
}

function displayProductInfo(reviewInfo) {
    if (reviewInfo.thumbnail)
        document.getElementById('product-image').src = '/media/' + reviewInfo.thumbnail;
    document.getElementById('product-name').innerText = reviewInfo.productName;
}

const reviewTitleInput = document.getElementById('title');
const reviewCommentTextInput = document.getElementById('comment');

function displayPreviousProductReview(review) {
    if (!review.rating && !review.reviewTitle)
        return;
    selected = review.rating;
    for (let i = 0; i < selected; i++) {
        stars[i].classList.add('text-yellow-400');
    }

    reviewTitleInput.value = review.reviewTitle;
    reviewCommentTextInput.value = review.comment;
    if (review.reviewMediaURL) {
        previewImg.src = '/media/' + review.reviewMediaURL;
        preview.classList.remove('hidden');
    }
}

let selected = 0;
const stars = document.querySelectorAll('#stars span');
function initializeRatingStar() {

    stars.forEach((star, idx) => {
        star.addEventListener('mouseover', () => {
            stars.forEach((s, i) => {
                s.classList.toggle('text-yellow-400', i <= idx);
                s.classList.toggle('text-gray-300', i > idx);
            });
        });

        star.addEventListener('click', () => {
            selected = idx + 1;
        });

        star.addEventListener('mouseout', () => {
            stars.forEach((s, i) => {
                s.classList.toggle('text-yellow-400', i < selected);
                s.classList.toggle('text-gray-300', i >= selected);
            });
        });
    });
}

const input = document.getElementById('reviewImage');
const preview = document.getElementById('preview');
const previewImg = document.getElementById('previewImg');
function initializeImageInput() {
    // Handle image preview
    input.addEventListener('change', () => {
        const file = input.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = e => {
                previewImg.src = e.target.result;
                preview.classList.remove('hidden');
            };
            reader.readAsDataURL(file);
        } else {
            preview.classList.add('hidden');
            previewImg.src = "";
        }
    });
}

async function postUserProductReview(reviewInfo) {
    if (!reviewTitleInput.value || selected === 0) {
        alert("Review Title and Rating are required");
        return;
    }

    const formData = new FormData();
    const jsonData = new Blob([JSON.stringify({
        'productId': reviewInfo.productId,
        'reviewTitle' : reviewTitleInput.value,
        'comment' : reviewCommentTextInput.value,
        'reviewMediaURL' : reviewInfo.reviewMediaURL,
        'rating': selected
    })], { type: 'application/json' });
    formData.append('review', jsonData);
    formData.append('image', input.files[0]);

    console.log(jsonData);

    const postResponse = await fetch('/api/user/review', {
        method: 'POST',
        body: formData,
    });
    if (!postResponse.ok) {
        console.error(postResponse.error);
        alert('Failed to post a review');
    }
}