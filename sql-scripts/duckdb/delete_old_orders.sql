-- Delete old orders and return what was deleted
-- Tests: DELETE with RETURNING, date comparisons, status filtering

DELETE FROM orders
WHERE
    order_date < CAST(:"cutoff_date!" AS DATE)
    AND (:"status?" IS NULL OR status = CAST(:status AS VARCHAR))
RETURNING
    order_id,
    customer_id,
    order_date,
    total_amount,
    status;
