
async function fetchUserOrderHistory(start, page = 0) {
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

    return {
        "timeFilterOptions": [
            {
                "key": "last_30_days",
                "label": "Last 30 days",
                "startInclusive": "2025-08-20T20:48:21.176954Z",
                "endExclusive": "2025-09-19T20:48:21.176954Z"
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
                            "price": 100.00,
                            "status": null,
                            "statusDate": null
                        }
                    ]
                }
            ],
            "page": {
                "size": 10,
                "number": 0,
                "totalElements": 1,
                "totalPages": 1
            }
        }
    };
}

function displaySelectPlaceIn(timeFilterOptions) {
    const select = document.getElementById('select-place-in');
    select.innerHTML = "";
    timeFilterOptions.forEach(filterOption => {
        const option = document.createElement("option");

    });
}

function displayOrderHistory(orderHistory) {

}























