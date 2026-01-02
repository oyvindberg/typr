select n.nspname as "enum_schema?",
       t.typname as enum_name,
       e.enumsortorder as enum_sort_order,
       e.enumlabel as enum_value
from pg_type t
         join pg_enum e on t.oid = e.enumtypid
         join pg_catalog.pg_namespace n ON n.oid = t.typnamespace