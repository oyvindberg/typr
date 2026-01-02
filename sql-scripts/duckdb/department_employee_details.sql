-- Department and employee details with composite key handling
-- Tests: composite primary keys, composite foreign keys, multiple table joins

SELECT
    d.dept_code,
    d.dept_region,
    d.dept_name,
    d.budget,
    e.emp_number,
    e.emp_suffix,
    e.emp_name,
    e.salary,
    e.hire_date,
    -- Calculate years of service
    DATE_DIFF('year', e.hire_date, CURRENT_DATE) AS years_of_service
FROM departments d
LEFT JOIN employees e ON d.dept_code = e.dept_code AND d.dept_region = e.dept_region
WHERE
    (:"dept_code?" IS NULL OR d.dept_code = :dept_code)
    AND (:"dept_region?" IS NULL OR d.dept_region = :dept_region)
    AND (:"min_salary?" IS NULL OR e.salary >= :min_salary)
    AND (:"hired_after?" IS NULL OR e.hire_date >= :hired_after)
ORDER BY d.dept_code, d.dept_region, e.emp_number;
