-- Insert a new order and return the generated data
-- Tests: INSERT with RETURNING, multiple columns, foreign keys, DEFAULT values

INSERT INTO orders (order_id, customer_id, order_date, total_amount, status)
VALUES (
    CAST(:"order_id!" AS INTEGER),
    CAST(:"customer_id:testdb.customers.CustomersId!" AS INTEGER),
    CAST(:"order_date?" AS DATE),
    CAST(:"total_amount?" AS DECIMAL),
    CAST(:"status?" AS VARCHAR)
)
RETURNING
    order_id,
    customer_id,
    order_date,
    total_amount,
    status;
