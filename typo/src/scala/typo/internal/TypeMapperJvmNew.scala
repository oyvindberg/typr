package typo
package internal

case class TypeMapperJvmNew(
    lang: Lang,
    typeOverride: TypeOverride,
    nullabilityOverride: NullabilityOverride,
    naming: Naming
) extends TypeMapperJvm(lang, typeOverride, nullabilityOverride) {

  override def needsTimestampCasts: Boolean = false

  override def baseType(tpe: db.Type): jvm.Type = {
    tpe match {
      case x: db.PgType =>
        x match {
          case db.PgType.Array(_) => sys.error("no idea what to do with nested array types")
          case db.PgType.Boolean  => lang.Boolean
          case db.PgType.Bytea    => lang.ByteArray
          case db.PgType.Bpchar(maybeN) =>
            maybeN match {
              case Some(n) if n != 2147483647 => lang.String.withComment(s"bpchar, max $n chars")
              case _                          => lang.String.withComment(s"bpchar")
            }
          case db.PgType.Char                  => lang.String
          case db.PgType.Date                  => TypesJava.LocalDate
          case db.PgType.DomainRef(name, _, _) => jvm.Type.Qualified(naming.domainName(name))
          case db.PgType.Float4                => lang.Float
          case db.PgType.Float8                => lang.Double
          case db.PgType.Hstore                => lang.MapOps.tpe.of(lang.String, lang.String)
          case db.PgType.Inet                  => TypesJava.runtime.Inet
          case db.PgType.Int2                  => lang.Short
          case db.PgType.Int4                  => lang.Int
          case db.PgType.Int8                  => lang.Long
          case db.PgType.Json                  => TypesJava.runtime.Json
          case db.PgType.Jsonb                 => TypesJava.runtime.Jsonb
          case db.PgType.Name                  => lang.String
          case db.PgType.Numeric               => lang.BigDecimal
          case db.PgType.Oid                   => lang.Long.withComment("oid")
          case db.PgType.PGInterval            => TypesJava.PGInterval
          case db.PgType.PGbox                 => TypesJava.PGbox
          case db.PgType.PGcircle              => TypesJava.PGcircle
          case db.PgType.PGline                => TypesJava.PGline
          case db.PgType.PGlseg                => TypesJava.PGlseg
          case db.PgType.PGlsn                 => lang.Long.withComment("pg_lsn")
          case db.PgType.PGmoney               => TypesJava.runtime.Money
          case db.PgType.PGpath                => TypesJava.PGpath
          case db.PgType.PGpoint               => TypesJava.PGpoint
          case db.PgType.PGpolygon             => TypesJava.PGpolygon
          case db.PgType.aclitem               => TypesJava.runtime.AclItem
          case db.PgType.anyarray              => TypesJava.runtime.AnyArray
          case db.PgType.int2vector            => TypesJava.runtime.Int2Vector
          case db.PgType.oidvector             => TypesJava.runtime.OidVector
          case db.PgType.pg_node_tree          => TypesJava.runtime.PgNodeTree
          case db.PgType.record                => TypesJava.runtime.Record
          case db.PgType.regclass              => TypesJava.runtime.Regclass
          case db.PgType.regconfig             => TypesJava.runtime.Regconfig
          case db.PgType.regdictionary         => TypesJava.runtime.Regdictionary
          case db.PgType.regnamespace          => TypesJava.runtime.Regnamespace
          case db.PgType.regoper               => TypesJava.runtime.Regoper
          case db.PgType.regoperator           => TypesJava.runtime.Regoperator
          case db.PgType.regproc               => TypesJava.runtime.Regproc
          case db.PgType.regprocedure          => TypesJava.runtime.Regprocedure
          case db.PgType.regrole               => TypesJava.runtime.Regrole
          case db.PgType.regtype               => TypesJava.runtime.Regtype
          case db.PgType.xid                   => TypesJava.runtime.Xid
          case db.PgType.EnumRef(enm)          => jvm.Type.Qualified(naming.enumName(enm.name))
          case db.PgType.Text                  => lang.String
          case db.PgType.Time                  => TypesJava.LocalTime
          case db.PgType.TimeTz                => TypesJava.OffsetTime
          case db.PgType.Timestamp             => TypesJava.LocalDateTime
          case db.PgType.TimestampTz           => TypesJava.Instant
          case db.PgType.UUID                  => TypesJava.UUID
          case db.PgType.Xml                   => TypesJava.runtime.Xml
          case db.PgType.VarChar(maybeN) =>
            maybeN match {
              case Some(n) if n != 2147483647 => lang.String.withComment(s"max $n chars")
              case _                          => lang.String
            }
          case db.PgType.Vector => TypesJava.runtime.Vector
          case db.Unknown(_)    => TypesJava.runtime.Unknown
        }
      case x: db.MariaType =>
        x match {
          case db.MariaType.TinyInt            => lang.Byte
          case db.MariaType.SmallInt           => lang.Short
          case db.MariaType.MediumInt          => lang.Int
          case db.MariaType.Int                => lang.Int
          case db.MariaType.BigInt             => lang.Long
          case db.MariaType.TinyIntUnsigned    => lang.Short
          case db.MariaType.SmallIntUnsigned   => lang.Int
          case db.MariaType.MediumIntUnsigned  => lang.Int
          case db.MariaType.IntUnsigned        => lang.Long
          case db.MariaType.BigIntUnsigned     => TypesJava.BigInteger
          case db.MariaType.Decimal(_, _)      => lang.BigDecimal
          case db.MariaType.Float              => lang.Float
          case db.MariaType.Double             => lang.Double
          case db.MariaType.Boolean            => lang.Boolean
          case db.MariaType.Bit(Some(1))       => lang.Boolean
          case db.MariaType.Bit(_)             => lang.ByteArrayType
          case db.MariaType.Char(_)            => lang.String
          case db.MariaType.VarChar(_)         => lang.String
          case db.MariaType.TinyText           => lang.String
          case db.MariaType.Text               => lang.String
          case db.MariaType.MediumText         => lang.String
          case db.MariaType.LongText           => lang.String
          case db.MariaType.Binary(_)          => lang.ByteArrayType
          case db.MariaType.VarBinary(_)       => lang.ByteArrayType
          case db.MariaType.TinyBlob           => lang.ByteArrayType
          case db.MariaType.Blob               => lang.ByteArrayType
          case db.MariaType.MediumBlob         => lang.ByteArrayType
          case db.MariaType.LongBlob           => lang.ByteArrayType
          case db.MariaType.Date               => TypesJava.LocalDate
          case db.MariaType.Time(_)            => TypesJava.LocalTime
          case db.MariaType.DateTime(_)        => TypesJava.LocalDateTime
          case db.MariaType.Timestamp(_)       => TypesJava.LocalDateTime
          case db.MariaType.Year               => TypesJava.Year
          case db.MariaType.Enum(_)            => lang.String // MariaDB inline ENUMs are stored as strings
          case db.MariaType.Set(_)             => TypesJava.maria.MariaSet
          case db.MariaType.Json               => TypesJava.runtime.Json
          case db.MariaType.Inet4              => TypesJava.maria.Inet4
          case db.MariaType.Inet6              => TypesJava.maria.Inet6
          case db.MariaType.Geometry           => TypesJava.maria.Geometry
          case db.MariaType.Point              => TypesJava.maria.Point
          case db.MariaType.LineString         => TypesJava.maria.LineString
          case db.MariaType.Polygon            => TypesJava.maria.Polygon
          case db.MariaType.MultiPoint         => TypesJava.maria.MultiPoint
          case db.MariaType.MultiLineString    => TypesJava.maria.MultiLineString
          case db.MariaType.MultiPolygon       => TypesJava.maria.MultiPolygon
          case db.MariaType.GeometryCollection => TypesJava.maria.GeometryCollection
          case db.Unknown(_)                   => TypesJava.runtime.Unknown
        }
    }
  }
}
