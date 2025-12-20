-- Customer search with multiple optional filters and enum handling
-- Tests: optional parameters, enum types, LIKE patterns, complex WHERE

SELECT
    customer_id,
    name,
    email,
    created_at,
    priority
FROM customers
WHERE
    (:"name_pattern?" IS NULL OR name LIKE :name_pattern)
    AND (:"email_pattern?" IS NULL OR email LIKE :email_pattern)
    AND (:"min_priority:testdb.Priority?" IS NULL OR priority >= :min_priority)
    AND (:"created_after?" IS NULL OR created_at >= :created_after)
ORDER BY created_at DESC, customer_id
LIMIT :"max_results!";
