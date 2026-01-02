-- Search customers by name pattern
SELECT
    c.customer_id,
    c.name,
    c.billing_address,
    c.credit_limit,
    c.created_at
FROM customers c
WHERE c.name LIKE :"name_pattern:String!"
ORDER BY c.name
