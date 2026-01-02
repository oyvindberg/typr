-- Update customer priority based on spending and return updated rows
-- Tests: UPDATE with RETURNING, enum parameters

UPDATE customers
SET priority = :"new_priority:testdb.Priority!"
WHERE customer_id = :"customer_id:testdb.customers.CustomersId!"
RETURNING
    customer_id,
    name,
    email,
    created_at,
    priority;
