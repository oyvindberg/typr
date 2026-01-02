-- Find product by SKU
SELECT
    p.product_id,
    p.sku,
    p.name,
    p.price,
    p.tags
FROM products p
WHERE p.sku = :"sku:String!"
