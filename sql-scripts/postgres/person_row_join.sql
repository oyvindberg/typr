SELECT s.businessentityid,
       (select array_agg(ROW(a.emailaddress, a.rowguid)) from person.emailaddress a where a.businessentityid = s.businessentityid) as email,
       (select ARRAY[ROW(a.emailaddress, a.rowguid)] from person.emailaddress a where a.businessentityid = s.businessentityid) as emails
FROM sales.salesperson s
