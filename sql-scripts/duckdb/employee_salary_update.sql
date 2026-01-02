-- Update employee salary using composite primary key
-- Tests: UPDATE with composite primary key, RETURNING, decimal arithmetic

UPDATE employees
SET salary = CASE
    WHEN :"raise_percentage?" IS NOT NULL THEN salary * (1 + CAST(:raise_percentage AS DECIMAL) / 100.0)
    ELSE CAST(:"new_salary!" AS DECIMAL)
END
WHERE emp_number = CAST(:"emp_number!" AS INTEGER)
  AND emp_suffix = CAST(:"emp_suffix!" AS VARCHAR)
RETURNING
    emp_number,
    emp_suffix,
    dept_code,
    dept_region,
    emp_name,
    salary,
    hire_date;
