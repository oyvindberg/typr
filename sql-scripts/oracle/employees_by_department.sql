-- Get employees in a department
SELECT
    e.emp_number,
    e.emp_suffix,
    e.emp_name,
    e.salary,
    e.hire_date,
    d.dept_name,
    d.budget
FROM employees e
INNER JOIN departments d ON e.dept_code = d.dept_code AND e.dept_region = d.dept_region
WHERE e.dept_code = :"dept_code:String!"
  AND e.dept_region = :"dept_region:String!"
ORDER BY e.emp_name
