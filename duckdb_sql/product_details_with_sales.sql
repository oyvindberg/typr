-- Product details with sales statistics and JSON metadata
-- Tests: JSON column handling, complex aggregations, CASE expressions

SELECT
    p.product_id,
    p.sku,
    p.name,
    p.price,
    p.metadata,
    COUNT(DISTINCT oi.order_id) AS times_ordered,
    COALESCE(SUM(oi.quantity), 0) AS total_quantity_sold,
    COALESCE(SUM(oi.quantity * oi.unit_price), 0.0) AS total_revenue,
    CASE
        WHEN COUNT(DISTINCT oi.order_id) = 0 THEN 'never_ordered'
        WHEN COUNT(DISTINCT oi.order_id) < 5 THEN 'low'
        WHEN COUNT(DISTINCT oi.order_id) < 20 THEN 'medium'
        ELSE 'high'
    END AS popularity
FROM products p
LEFT JOIN order_items oi ON p.product_id = oi.product_id
WHERE
    (:"product_ids?" IS NULL OR p.product_id = ANY(CAST(:product_ids AS INTEGER[])))
    AND (:"sku_pattern?" IS NULL OR p.sku LIKE CAST(:sku_pattern AS VARCHAR))
    AND (:"min_price?" IS NULL OR p.price >= CAST(:min_price AS DECIMAL))
    AND (:"max_price?" IS NULL OR p.price <= CAST(:max_price AS DECIMAL))
GROUP BY p.product_id, p.sku, p.name, p.price, p.metadata
ORDER BY total_revenue DESC;
