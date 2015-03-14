| **Type** | **Database Type** | **java.sql.Types** |
|:---------|:------------------|:-------------------|
| String | VARCHAR / NVARCHAR / TEXT | Types.VARCHAR |
| Int / java.lang.Integer | INTEGER | Types.INTEGER |
| Long / java.lang.Long | BIGINT | Types.BIGINT |
| Float / java.lang.Float | FLOAT | Types.FLOAT |
| Double / java.lang.Double | DOUBLE | Types.DOUBLE |
| java.util.Date / java.util.Calendar / org.joda.time.DateTime | DATETIME/TIMESTAMP | Types.TIMESTAMP |
| BigDecimal | NUMERIC | Types.NUMERIC |
| Boolean | BIT | Types.BIT |
| Byte | BYTE/SMALLINT | Types.SMALLINT |
| Short | SMALLINT | Types.SMALLINT |
| org.joda.time.LocalDate | TIMESTAMP | Types.TIMESTAMP |
| org.joda.time.LocalTime | TIME | Types.TIME |
| org.joda.time.Duration | Interval | PGInterval (postgresql only) |
| org.joda.time.Period | Interval | PGInterval (postgresql only) |
| `Array[Byte]` | BLOB | Types.BLOB |

see [TypesSuite.scala](https://code.google.com/p/mapperdao/source/browse/src/test/scala/com/googlecode/mapperdao/TypesSuite.scala)