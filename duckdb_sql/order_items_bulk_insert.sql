-- Insert order items with composite key handling
-- Tests: composite primary key in INSERT, foreign key types, RETURNING with composite key

INSERT INTO order_items (order_id, product_id, quantity, unit_price)
VALUES (
    CAST(:"order_id:testdb.orders.OrdersId!" AS INTEGER),
    CAST(:"product_id:testdb.products.ProductsId!" AS INTEGER),
    CAST(:"quantity!" AS INTEGER),
    CAST(:"unit_price!" AS DECIMAL)
)
RETURNING
    order_id,
    product_id,
    quantity,
    unit_price;
