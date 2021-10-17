# Order Service
=> Sample cURL request for creating a new order

curl --location --request POST 'http://localhost:9000/order/submit' \
--header 'Content-Type: application/json' \
--data-raw '{
    "customerId": "customer-id",
    "items": [
        {
            "itemId": "itemId",
            "quantity": 1,
            "pricePerUnit": 10.10
        }
    ],
    "totalAmount": 10.50,
    "shippingAddress": {
        "addressLineOne": "address-line-one",
        "addressLineTwo": "address-line-two",
        "addressLineThree": "address-line-three",
        "city": "Mumbai",
        "pincode": 400104,
        "country": "India",
        "type": "HOME"
    }
}'


=> Sample cURL request to get totalAmount for an orderId

curl --location --request GET 'http://localhost:9000/order/totalAmount/a8c8e906-c1c2-43b5-9421-b4dd75e5aa7a' \
--header 'Content-Type: application/json' \
--data-raw ''


=> Sample cURL request to get shippingAddress for an orderId

curl --location --request GET 'http://localhost:9000/order/shippingAddress/a8c8e906-c1c2-43b5-9421-b4dd75e5aa7a' \
--header 'Content-Type: application/json' \
--data-raw ''

