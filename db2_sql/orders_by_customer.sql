-- Orders by customer with order items
SELECT o.order_id, o.order_date, o.total_amount, o.status,
       i.item_number, i.product_name, i.quantity, i.unit_price
FROM orders o
INNER JOIN order_items i ON o.order_id = i.order_id
WHERE o.customer_id = :"customer_id:Int!"
ORDER BY o.order_date DESC, i.item_number
