# Product Service

## Service Events

### Happy case

In happy path, if an event is published on topic `OrderPlaced`, product-service will try to avail the products of the order.

After a successful avail of **all** the produts of order, this service will raise `ProductAvailed` event. If for some reason, products can't be availed, `ProductAvailFailed` event will be raised.

### Error case

error scenarios after an order is placed:
  - enough products are not available in inventory
    - products will not be availed and `ProductAvailFailed` event will be raised
  - mongo is down/not accessible
    - products will not be availed and `ProductAvailFailed` event will be raised
  - kafka is down/not accessible after availing all products in database
    - ???
  - kafka is down/not accessible after availing fails due to lack of quantity in database
    - ???