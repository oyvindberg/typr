package typo
package internal
package codegen

import typo.jvm.Code

object DuckDbAdapter extends DbAdapter {
  val dbType: DbType = DbType.DuckDB

  // ═══════════════════════════════════════════════════════════════════════════
  // LAYER 1: SQL Syntax
  // ═══════════════════════════════════════════════════════════════════════════

  def quoteIdent(name: String): String = s""""$name""""

  def typeCast(value: Code, typeName: String): Code =
    if (typeName.isEmpty) value else code"$value::$typeName"

  // DuckDB uses PostgreSQL-style casting
  def columnReadCast(col: ComputedColumn): Code = Code.Empty
  def columnWriteCast(col: ComputedColumn): Code = Code.Empty

  // ═══════════════════════════════════════════════════════════════════════════
  // LAYER 2: Runtime Type System
  // ═══════════════════════════════════════════════════════════════════════════

  val Types: jvm.Type.Qualified = jvm.Type.Qualified("typo.runtime.DuckDbTypes")
  val TypeClass: jvm.Type.Qualified = jvm.Type.Qualified("typo.runtime.DuckDbType")
  val TextClass: jvm.Type.Qualified = jvm.Type.Qualified("typo.runtime.DuckDbText")
  val typeFieldName: jvm.Ident = jvm.Ident("duckDbType")
  val textFieldName: jvm.Ident = jvm.Ident("duckDbText")
  val dialectRef: Code = code"${jvm.Type.dsl.Dialect}.DUCKDB"

  def lookupType(tpe: jvm.Type, pkg: jvm.QIdent, lang: Lang): Code =
    jvm.Type.base(tpe) match {
      case TypesJava.BigDecimal                    => code"$Types.numeric"
      case TypesJava.BigInteger                    => code"$Types.hugeint"
      case TypesJava.Boolean | TypesKotlin.Boolean => code"$Types.boolean_"
      case TypesJava.Double | TypesKotlin.Double   => code"$Types.double_"
      case TypesJava.Float | TypesKotlin.Float     => code"$Types.float_"
      case TypesJava.Byte | TypesKotlin.Byte       => code"$Types.tinyint"
      case TypesJava.Short | TypesKotlin.Short     => code"$Types.smallint"
      case TypesJava.Integer | TypesKotlin.Int     => code"$Types.integer"
      case TypesJava.Long | TypesKotlin.Long       => code"$Types.bigint"
      case TypesJava.String | TypesKotlin.String   => code"$Types.varchar"
      case TypesJava.UUID                          => code"$Types.uuid"
      case TypesJava.LocalDate                     => code"$Types.date"
      case TypesJava.LocalTime                     => code"$Types.time"
      case TypesJava.LocalDateTime                 => code"$Types.timestamp"
      case TypesJava.OffsetDateTime                => code"$Types.timestamptz"
      case TypesJava.Duration                      => code"$Types.interval"
      case TypesJava.runtime.Json                  => code"$Types.json"
      case lang.Optional(targ)                     => code"${lookupType(targ, pkg, lang)}.opt()"
      case lang.ByteArrayType                      => code"$Types.blob"
      // generated type
      case x: jvm.Type.Qualified if x.value.idents.startsWith(pkg.idents) =>
        code"$tpe.$typeFieldName"
      // list type
      case jvm.Type.TApply(TypesJava.List, List(targ)) =>
        code"${lookupType(targ, pkg, lang)}.list()"
      // map type
      case jvm.Type.TApply(TypesJava.Map, List(keyTarg, valueTarg)) =>
        code"${lookupType(keyTarg, pkg, lang)}.mapTo(${lookupType(valueTarg, pkg, lang)})"
      case other => sys.error(s"DuckDbAdapter.lookupType: Unsupported type: $other")
    }

  def lookupTypeByDbType(dbType: db.Type, Types: jvm.Type.Qualified, naming: Naming): Code =
    dbType match {
      // Integer types
      case db.DuckDbType.TinyInt   => code"$Types.tinyint"
      case db.DuckDbType.SmallInt  => code"$Types.smallint"
      case db.DuckDbType.Integer   => code"$Types.integer"
      case db.DuckDbType.BigInt    => code"$Types.bigint"
      case db.DuckDbType.HugeInt   => code"$Types.hugeint"
      case db.DuckDbType.UTinyInt  => code"$Types.utinyint"
      case db.DuckDbType.USmallInt => code"$Types.usmallint"
      case db.DuckDbType.UInteger  => code"$Types.uinteger"
      case db.DuckDbType.UBigInt   => code"$Types.ubigint"
      case db.DuckDbType.UHugeInt  => code"$Types.uhugeint"

      // Floating-point types
      case db.DuckDbType.Float  => code"$Types.float_"
      case db.DuckDbType.Double => code"$Types.double_"

      // Fixed-point types
      case db.DuckDbType.Decimal(_, _) => code"$Types.numeric"

      // Boolean
      case db.DuckDbType.Boolean => code"$Types.boolean_"

      // String types
      case db.DuckDbType.VarChar(_) => code"$Types.varchar"
      case db.DuckDbType.Char(_)    => code"$Types.char_"
      case db.DuckDbType.Text       => code"$Types.text"

      // Binary types
      case db.DuckDbType.Blob => code"$Types.blob"

      // Bit string
      case db.DuckDbType.Bit(_) => code"$Types.bit"

      // Date/Time types
      case db.DuckDbType.Date        => code"$Types.date"
      case db.DuckDbType.Time        => code"$Types.time"
      case db.DuckDbType.Timestamp   => code"$Types.timestamp"
      case db.DuckDbType.TimestampTz => code"$Types.timestamptz"
      case db.DuckDbType.TimestampS  => code"$Types.timestamp"
      case db.DuckDbType.TimestampMS => code"$Types.timestamp"
      case db.DuckDbType.TimestampNS => code"$Types.timestamp"
      case db.DuckDbType.TimeTz      => code"$Types.timetz"
      case db.DuckDbType.Interval    => code"$Types.interval"

      // UUID
      case db.DuckDbType.UUID => code"$Types.uuid"

      // JSON
      case db.DuckDbType.Json => code"$Types.json"

      // Enum
      case db.DuckDbType.Enum(name, _) =>
        code"${jvm.Type.Qualified(naming.enumName(db.RelationName(None, name)))}.$typeFieldName"

      // Composite types - these need element type lookup
      case db.DuckDbType.ListType(elementType) =>
        code"${lookupTypeByDbType(elementType, Types, naming)}.list()"
      case db.DuckDbType.ArrayType(elementType, _) =>
        code"${lookupTypeByDbType(elementType, Types, naming)}.list()"
      case db.DuckDbType.MapType(keyType, valueType) =>
        code"${lookupTypeByDbType(keyType, Types, naming)}.mapTo(${lookupTypeByDbType(valueType, Types, naming)})"
      case db.DuckDbType.StructType(_) =>
        sys.error(s"DuckDbAdapter.lookupTypeByDbType: STRUCT type not yet supported")
      case db.DuckDbType.UnionType(_) =>
        sys.error(s"DuckDbAdapter.lookupTypeByDbType: UNION type not yet supported")

      case db.Unknown(sqlType) =>
        sys.error(s"DuckDbAdapter.lookupTypeByDbType: Cannot lookup for unknown type: $sqlType")
      case _: db.PgType =>
        sys.error(s"DuckDbAdapter.lookupTypeByDbType: Cannot lookup PostgreSQL type in DuckDB adapter")
      case _: db.MariaType =>
        sys.error(s"DuckDbAdapter.lookupTypeByDbType: Cannot lookup MariaDB type in DuckDB adapter")
    }

  // ═══════════════════════════════════════════════════════════════════════════
  // LAYER 3: Capabilities
  // ═══════════════════════════════════════════════════════════════════════════

  val supportsArrays: Boolean = true // DuckDB has LIST type
  val supportsReturning: Boolean = true
  val supportsCopyStreaming: Boolean = false // DuckDB uses different COPY mechanism
  val supportsDefaultInCopy: Boolean = false

  // ═══════════════════════════════════════════════════════════════════════════
  // LAYER 4: SQL Templates
  // ═══════════════════════════════════════════════════════════════════════════

  def upsertSql(
      tableName: Code,
      columns: Code,
      idColumns: Code,
      values: Code,
      conflictUpdate: Code,
      returning: Option[Code]
  ): Code =
    returning match {
      case Some(cols) =>
        code"""|INSERT INTO $tableName($columns)
               |VALUES ($values)
               |ON CONFLICT ($idColumns)
               |$conflictUpdate
               |RETURNING $cols""".stripMargin
      case None =>
        code"""|INSERT INTO $tableName($columns)
               |VALUES ($values)
               |ON CONFLICT ($idColumns)
               |$conflictUpdate""".stripMargin
    }

  def conflictUpdateClause(cols: List[ComputedColumn], quotedColName: ComputedColumn => Code): Code =
    code"""|DO UPDATE SET
           |  ${cols.map(c => code"${quotedColName(c)} = EXCLUDED.${quotedColName(c)}").mkCode(",\n")}""".stripMargin

  def conflictNoOpClause(firstPkCol: ComputedColumn, quotedColName: ComputedColumn => Code): Code =
    code"DO UPDATE SET ${quotedColName(firstPkCol)} = EXCLUDED.${quotedColName(firstPkCol)}"

  def streamingInsertSql(tableName: Code, columns: Code): Code =
    // DuckDB doesn't support streaming COPY in the same way as PostgreSQL
    code"/* DuckDB batch insert */"

  def createTempTableLike(tempName: String, sourceTable: Code): Code =
    code"CREATE TEMPORARY TABLE $tempName AS SELECT * FROM $sourceTable WHERE FALSE"
}
