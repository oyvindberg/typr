---
title: Arrays
---

Arrays are super useful in PostgreSQL, but almost impossible to work with through JDBC, unfortunately.

In Typo, a lot of care has been taken to support `Array`s of all relevant types, 
so you can freely use them in your relations and in your queries.

## Note
PostgreSQL arrays can be multidimensional, and can have `NULL` values.
No attempt has been made at handling that yet.

- To handle multidimensionality Typo may adopt `skunk`s `Arr` data type, which looks like it handles it well.
- To handle `NULL` we could (optionally) always generate `Arr[Option[T]]` types for all array columns.