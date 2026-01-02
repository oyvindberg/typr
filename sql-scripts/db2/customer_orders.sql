-- Get customer order details
SELECT
    c.customer_id,
    c.name as customer_name,
    o.order_id,
    o.order_date,
    o.total_amount,
    o.status
FROM customers c
INNER JOIN orders o ON c.customer_id = o.customer_id
WHERE c.customer_id = :"customer_id:Int!"
ORDER BY o.order_date DESC
