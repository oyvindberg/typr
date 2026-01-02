SELECT n.nspname                                     as "schema?",
       c.relname                                     as name,
       pg_catalog.obj_description(c.oid, 'pg_class') as description
FROM pg_catalog.pg_class c
         JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
WHERE c.relkind = 'r';
