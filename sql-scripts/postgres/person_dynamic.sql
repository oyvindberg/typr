SELECT p.title, p.firstname, p.middlename, p.lastname
FROM person.person p
WHERE :"first_name?"::text IS NULL OR p.firstname = :first_name
