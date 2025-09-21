
let currentStartTimeRange = null;

async function fetchUserOrderHistory(start = null, page = 0) {
    // const queryParams = new URLSearchParams({
    //     page: page,
    // });
    // if (start) {
    //     queryParams.append('start', start);
    // }
    //
    // const response = await fetch(`http://localhost:8080/api/user/order/history?${queryParams.toString()}`);
    // if (!response.ok) {
    //     console.error('Failed to get user order history')
    //     return;
    // }
    //
    // return response.json();
    currentStartTimeRange = start;
    return {
        "timeFilterOptions": [
            {
                "key": "DAYS_30",
                "label": "Last 30 days",
            },
            {
                "key": "DAYS_90",
                "label": "Last 90 days",
            },
            {
                "key": "2025",
                "label": "Year 2025",
            }
        ],
        "orders": {
            "content": [
                {
                    "orderPlaced": "2025-09-19",
                    "total": 318.75,
                    "orderId": 1,
                    "orderStatus": "PROCESSING",
                    "statusDate": "2025-09-19",
                    "items": [
                        {
                            "productId": 1,
                            "thumbnail": null,
                            "productName": "Product Name 1",
                            "quantity": 1,
                            "price": 100.00,
                            "status": null,
                            "statusDate": null
                        },
                        {
                            "productId": 2,
                            "thumbnail": null,
                            "productName": "Product Name 2",
                            "quantity": 1,
                            "price": 100.00,
                            "status": 'CANCELLED',
                            "statusDate": '2025-09-19'
                        }
                    ]
                }
            ],
            "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 10
            }
        }
    };
}

async function initializeUserOrderHistory() {
    const orderHistory = await fetchUserOrderHistory();
    displaySelectPlaceInTimeRange(orderHistory.timeFilterOptions);
    displayTotalOrder(orderHistory.orders.page.totalElements);
    displayOrderHistory(orderHistory.orders.content);
    displayPaging(orderHistory.orders.page.number, orderHistory.orders.page.totalPages);
}

function displayTotalOrder(total) {
    document.getElementById('total-orders').innerText = total;
}

function displaySelectPlaceInTimeRange(timeFilterOptions) {
    const select = document.getElementById('select-place-in');
    select.innerHTML = "";
    timeFilterOptions.forEach(filterOption => {
        const option = document.createElement("option");
        option.value = filterOption.key;
        option.textContent = filterOption.label;
        select.appendChild(option);
    });
    select.addEventListener('change',async function(e) {
        const start = e.target.value;
        const orderHistory = await fetchUserOrderHistory(start);
        displayTotalOrder(orderHistory.orders.page.totalElements);
        displayOrderHistory(orderHistory.orders.content);
        displayPaging(orderHistory.orders.page.number, orderHistory.orders.page.totalPages);
    });
}

function displayOrderHistory(orders) {

    const orderContainer = document.getElementById('orders-container');
    const orderNodeTem = orderContainer.querySelector('.order-node');
    const orderItemNodeTem = orderContainer.querySelector('.order-item-node');
    const greenStatusItemTem = orderContainer.querySelector('.green-status-item');
    const redStatusItemTem = orderContainer.querySelector('.red-status-item');

    const orderPlacedContainer = document.getElementById('order-placed-container');
    orderPlacedContainer.innerHTML = '';

    orders.forEach(order => {
        const orderNode = orderNodeTem.cloneNode(true);
        orderNode.classList.remove('hidden');
        orderNode.querySelector('.order-placed-date').innerText = convertNumericDateToStringDate(order.orderPlaced);
        orderNode.querySelector('.order-id').innerText = order.orderId;
        orderNode.querySelector('.order-total').innerText = order.total;
        orderPlacedContainer.appendChild(orderNode);

        const orderItemNodeContainer = orderNode.querySelector('.order-item-node-container');
        const sameAsParentOrderStatusNode = [];
        const notSameAsParentOrderStatusNode = [];
        const notSameAsParentOrderStatusItem = [];
        order.items.forEach(orderItem => {
            const orderItemNode = orderItemNodeTem.cloneNode(true);
            orderItemNode.classList.remove('hidden');
            orderItemNode.querySelector('.item-node-thumbnail').src = orderItem.thumbnail;
            orderItemNode.querySelector('.item-node-title').innerText = orderItem.productName;
            orderItemNode.querySelector('.item-node-quantity').innerText = orderItem.quantity;
            orderItemNode.querySelector('.item-node-price').innerText = orderItem.price;

            if (orderItem.status) {
                notSameAsParentOrderStatusNode.push(orderItemNode);
                notSameAsParentOrderStatusItem.push(orderItem);
            } else {
                sameAsParentOrderStatusNode.push(orderItemNode);
            }
        });

        if (sameAsParentOrderStatusNode.length) {
            const greenStatus = greenStatusItemTem.cloneNode(true);
            greenStatus.querySelector('.green-status-name').innerText = order.orderStatus;
            greenStatus.querySelector('.green-status-date').innerText = convertNumericDateToShortStringDate(order.statusDate);
            greenStatus.classList.remove('hidden');
            orderItemNodeContainer.appendChild(greenStatus);

            sameAsParentOrderStatusNode.forEach((orderItemNode) => {
                orderItemNodeContainer.appendChild(orderItemNode);
            });
        }
        if (notSameAsParentOrderStatusNode.length) {
            notSameAsParentOrderStatusNode.forEach((orderItemNode, index) => {
                const redStatus = redStatusItemTem.cloneNode(true);
                redStatus.querySelector('.red-status-name').innerText =
                    notSameAsParentOrderStatusItem[index].status;
                redStatus.querySelector('.red-status-date').innerText =
                    convertNumericDateToShortStringDate(notSameAsParentOrderStatusItem[index].statusDate);
                redStatus.classList.remove('hidden');
                orderItemNodeContainer.appendChild(redStatus);

                orderItemNodeContainer.appendChild(orderItemNode);
            });
        }
    });
}

function convertNumericDateToStringDate(date) {
    return new Date(date)
        .toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" });
}

function convertNumericDateToShortStringDate(date) {
    return new Date(date)
        .toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

function displayPaging(currentPage = 0, totalPage) {
    const pageWrapper = document.getElementById('pagination-wrapper');
    const pageContainer = document.getElementById('page-container');
    pageContainer.innerHTML = '';
    const pageItemTem = pageWrapper.querySelector('.page-item').cloneNode(true);
    pageItemTem.classList.remove('hidden');
    const threeDotsTem = pageWrapper.querySelector('.three-dots');

    currentPage += 1; // since page index starts at 0

    if (currentPage > 1) {
        pageContainer.appendChild(createPageNode(pageItemTem, 1));
    }

    const prev2 = currentPage - 2;

    if (prev2 > 1 + 2) {
        const threeDotsNode = threeDotsTem.cloneNode(true);
        threeDotsNode.classList.remove('hidden');
        pageContainer.appendChild(threeDotsNode);
    } else if (currentPage > 2 ) {
        pageContainer.appendChild(createPageNode(pageItemTem, 2));
    }

    const prevNodes = [];
    for (let i = currentPage - 1; i > 0 && i >= prev2; i--) {
        prevNodes.push(createPageNode(pageItemTem, i));
    }
    for (let i = prevNodes.length - 1; i >= 0; i--) {
        pageContainer.appendChild(prevNodes[i]);
    }

    const currentPageNode = createPageNode(pageItemTem, currentPage)
    currentPageNode.classList.add('bg-amber-600', 'text-white', 'hover:bg-amber-700');
    currentPageNode.classList.remove('hover:bg-gray-50');
    pageContainer.appendChild(currentPageNode);

    const next2 = currentPage + 2;
    for (let i = currentPage + 1; i <= next2 && i < totalPage; i++) {
        pageContainer.appendChild(createPageNode(pageItemTem, i));
    }

    if (next2 < totalPage - 2) {
        const threeDotsNode = threeDotsTem.cloneNode(true);
        threeDotsNode.classList.remove('hidden');
        pageContainer.appendChild(threeDotsNode);
    } else if (currentPage < totalPage - 1) {
        pageContainer.appendChild(createPageNode(pageItemTem, totalPage - 1));
    }

    if (currentPage < totalPage) {
        pageContainer.appendChild(createPageNode(pageItemTem, totalPage));
    }

    const prevPageBtn = document.getElementById('prev-page-btn');
    const nextPageBtn = document.getElementById('next-page-btn');
    if (totalPage < 2) {
        prevPageBtn.classList.add('hidden');
        nextPageBtn.classList.add('hidden');
    } else if (currentPage === 1) {
        prevPageBtn.classList.add('hidden');
    } else if (currentPage === totalPage) {
        nextPageBtn.classList.add('hidden');
    }

    if (currentPage !== 1) {
        prevPageBtn.addEventListener('click',async function(){
            console.log(currentPage - 1)
            await getOrderHistoryOnPage(currentPage - 1);
        });
    }
    if (currentPage !== totalPage) {
        nextPageBtn.addEventListener('click',async function() {
            console.log(currentPage + 1)
            await getOrderHistoryOnPage(currentPage + 1);
        });
    }
}

function createPageNode(nodeToClone, index) {
    const pageNode = nodeToClone.cloneNode(true);
    pageNode.classList.remove('hidden');
    pageNode.innerText = index
    pageNode.addEventListener('click',async function() {
        await getOrderHistoryOnPage(index);
    });
    return pageNode;
}

async function getOrderHistoryOnPage(index) {
    const orderHistory = await fetchUserOrderHistory(currentStartTimeRange, index);
    displayOrderHistory(orderHistory.orders.content);
    displayPaging(orderHistory.orders.page.number, orderHistory.orders.page.totalPages);
}

window.onload = initializeUserOrderHistory();























