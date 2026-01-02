-- Order summary with aggregations and complex joins
-- Tests: aggregations, GROUP BY, HAVING, multiple joins, type-safe IDs in WHERE

SELECT
    c.customer_id,
    c.name AS customer_name,
    c.email,
    c.priority,
    COUNT(DISTINCT o.order_id) AS order_count,
    COALESCE(SUM(o.total_amount), 0.0) AS total_spent,
    MAX(o.order_date) AS last_order_date,
    MIN(o.order_date) AS first_order_date,
    COALESCE(AVG(o.total_amount), 0.0) AS avg_order_amount
FROM customers c
LEFT JOIN orders o ON c.customer_id = o.customer_id
WHERE
    (:"customer_ids?" IS NULL OR c.customer_id = ANY(CAST(:customer_ids AS INTEGER[])))
    AND (:"min_total?" IS NULL OR c.customer_id IN (
        SELECT customer_id
        FROM orders
        GROUP BY customer_id
        HAVING SUM(total_amount) >= CAST(:min_total AS DECIMAL)
    ))
GROUP BY c.customer_id, c.name, c.email, c.priority
HAVING
    (:"min_order_count?" IS NULL OR COUNT(DISTINCT o.order_id) >= CAST(:min_order_count AS INTEGER))
ORDER BY total_spent DESC, customer_name;
