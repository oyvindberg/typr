-- Product Summary View
-- Shows product details with aggregated information
-- This tests DuckDB SQL file processing and view generation

SELECT
  p.product_id,
  p.name as product_name,
  p.sku,
  p.price,
  COUNT(*) as order_count,
  COALESCE(SUM(oi.quantity), 0) as total_quantity,
  COALESCE(SUM(oi.quantity * oi.unit_price), 0) as total_revenue
FROM products p
LEFT JOIN order_items oi ON p.product_id = oi.product_id
GROUP BY p.product_id, p.name, p.sku, p.price
