-- Complex query testing all scalar types with various comparisons
-- Tests: all DuckDB scalar types, optional parameters, range queries, UUID, JSON

SELECT
    id,
    col_tinyint,
    col_smallint,
    col_integer,
    col_bigint,
    col_hugeint,
    col_utinyint,
    col_usmallint,
    col_uinteger,
    col_ubigint,
    col_float,
    col_double,
    col_decimal,
    col_boolean,
    col_varchar,
    col_text,
    col_blob,
    col_date,
    col_time,
    col_timestamp,
    col_timestamptz,
    col_interval,
    col_uuid,
    col_json,
    col_mood,
    col_not_null
FROM all_scalar_types
WHERE
    (:"id?" IS NULL OR id = :id)
    AND (:"min_integer?" IS NULL OR col_integer >= :min_integer)
    AND (:"max_bigint?" IS NULL OR col_bigint <= :max_bigint)
    AND (:"boolean_value?" IS NULL OR col_boolean = :boolean_value)
    AND (:"varchar_pattern?" IS NULL OR col_varchar LIKE :varchar_pattern)
    AND (:"min_date?" IS NULL OR col_date >= :min_date)
    AND (:"max_date?" IS NULL OR col_date <= :max_date)
    AND (:"after_timestamp?" IS NULL OR col_timestamp >= :after_timestamp)
    AND (:"uuid_value?" IS NULL OR col_uuid = :uuid_value)
    AND (:"mood_value:testdb.Mood?" IS NULL OR col_mood = :mood_value)
    AND (:"min_decimal?" IS NULL OR col_decimal >= :min_decimal)
ORDER BY id;
