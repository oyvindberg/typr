with cols as (SELECT nc.nspname AS table_schema,
                     c.relname  AS table_name,
                     a.attname  AS column_name,
                     a.attnum  AS ordinal_position
              FROM pg_attribute a
                       LEFT JOIN pg_attrdef ad ON a.attrelid = ad.adrelid AND a.attnum = ad.adnum
                       JOIN (pg_class c JOIN pg_namespace nc ON c.relnamespace = nc.oid) ON a.attrelid = c.oid

              WHERE a.attnum > 0
                AND NOT a.attisdropped)

select tc.table_schema,
       tc.table_name,
       array_agg(col.column_name) as columns,
       tc.constraint_name,
       cc.check_clause
from information_schema.table_constraints tc
         join information_schema.check_constraints cc
              on tc.constraint_schema = cc.constraint_schema and tc.constraint_name = cc.constraint_name
         join pg_namespace nsp on nsp.nspname = cc.constraint_schema
         join pg_constraint pgc on pgc.conname = cc.constraint_name and pgc.connamespace = nsp.oid and pgc.contype = 'c'
         join cols col on col.table_schema = tc.table_schema and col.table_name = tc.table_name and
                          col.ordinal_position = ANY (pgc.conkey)
group by tc.table_schema,
         tc.table_name,
         tc.constraint_name,
         cc.check_clause
order by tc.table_schema,
         tc.table_name