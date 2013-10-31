package com.googlecode.mapperdao.queries

import org.joda.time._
import com.googlecode.mapperdao.schema._
import com.googlecode.mapperdao._
import com.googlecode.mapperdao.schema.ColumnInfo
import com.googlecode.mapperdao.OneToOneReverseOperation
import com.googlecode.mapperdao.schema.ColumnInfoTraversableManyToMany
import com.googlecode.mapperdao.ManyToManyOperation
import com.googlecode.mapperdao.OneToManyOperation
import com.googlecode.mapperdao.schema.ColumnInfoManyToOne
import com.googlecode.mapperdao.schema.ColumnInfoOneToOneReverse
import com.googlecode.mapperdao.Operation
import com.googlecode.mapperdao.schema.ColumnInfoTraversableOneToMany
import com.googlecode.mapperdao.schema.ColumnInfoOneToOne
import com.googlecode.mapperdao.OneToManyDeclaredPrimaryKeyOperation
import com.googlecode.mapperdao.OneToOneOperation
import com.googlecode.mapperdao.queries.v2.{AliasRelationshipColumn, AliasColumn}

/**
 * @author kostantinos.kougios
 *
 *         17 Oct 2012
 */
trait SqlImplicitConvertions
{

	/**
	 * manages simple type expressions
	 */
	protected class Convertor[T, V](t: ColumnInfo[T, V])
	{
		def >(v: V) = new Operation(AliasColumn(t.column), GT, v)

		def >(v: ColumnInfo[_, V]) = new Operation(AliasColumn(t.column), GT, v.column)

		def >=(v: V) = new Operation(AliasColumn(t.column), GE, v)

		def >=(v: ColumnInfo[_, V]) = new ColumnOperation(AliasColumn(t.column), GE, AliasColumn(v.column))

		def <(v: V) = new Operation(AliasColumn(t.column), LT, v)

		def <(v: ColumnInfo[_, V]) = new ColumnOperation(AliasColumn(t.column), LT, AliasColumn(v.column))

		def <>(v: V) = new Operation(AliasColumn(t.column), NE, v)

		def <>(v: ColumnInfo[_, V]) = new ColumnOperation(AliasColumn(t.column), NE, AliasColumn(v.column))

		def <=(v: V) = new Operation(AliasColumn(t.column), LE, v)

		def <=(v: ColumnInfo[_, V]) = new Operation(AliasColumn(t.column), LE, v.column)

		def ===(v: V) = new Operation(AliasColumn(t.column), EQ, v) with EqualityOperation

		def ===(v: ColumnInfo[_, V]) = new Operation(AliasColumn(t.column), EQ, v.column) with EqualityOperation

		def like(v: V) = new Operation(AliasColumn(t.column), LIKE, v)

		def like(v: ColumnInfo[_, V]) = new ColumnOperation(AliasColumn(t.column), LIKE, AliasColumn(v.column))
	}

	implicit def columnInfoToOperableString[T](ci: ColumnInfo[T, String]) = new Convertor(ci)

	implicit def columnInfoToOperableByte[T](ci: ColumnInfo[T, Byte]) = new Convertor(ci)

	implicit def columnInfoToOperableShort[T](ci: ColumnInfo[T, Short]) = new Convertor(ci)

	implicit def columnInfoToOperableInt[T](ci: ColumnInfo[T, Int]) = new Convertor(ci)

	implicit def columnInfoToOperableLong[T](ci: ColumnInfo[T, Long]) = new Convertor(ci)

	implicit def columnInfoToOperableFloat[T](ci: ColumnInfo[T, Float]) = new Convertor(ci)

	implicit def columnInfoToOperableDouble[T](ci: ColumnInfo[T, Double]) = new Convertor(ci)

	implicit def columnInfoToOperableBoolean[T](ci: ColumnInfo[T, Boolean]) = new Convertor(ci)

	implicit def columnInfoToOperableDateTime[T](ci: ColumnInfo[T, DateTime]) = new Convertor(ci)

	implicit def columnInfoToOperableLocalTime[T](ci: ColumnInfo[T, LocalTime]) = new Convertor(ci)

	implicit def columnInfoToOperableLocalDate[T](ci: ColumnInfo[T, LocalDate]) = new Convertor(ci)

	implicit def columnInfoToOperablePeriod[T](ci: ColumnInfo[T, Period]) = new Convertor(ci)

	implicit def columnInfoToOperableDuration[T](ci: ColumnInfo[T, Duration]) = new Convertor(ci)

	implicit def columnInfoToOperableBigInt[T](ci: ColumnInfo[T, BigInt]) = new Convertor(ci)

	implicit def columnInfoToOperableBigDecimal[T](ci: ColumnInfo[T, BigDecimal]) = new Convertor(ci)

	// java
	implicit def columnInfoToOperableJShort[T](ci: ColumnInfo[T, java.lang.Short]) = new Convertor(ci)

	implicit def columnInfoToOperableJInteger[T](ci: ColumnInfo[T, java.lang.Integer]) = new Convertor(ci)

	implicit def columnInfoToOperableJLong[T](ci: ColumnInfo[T, java.lang.Long]) = new Convertor(ci)

	implicit def columnInfoToOperableJFloat[T](ci: ColumnInfo[T, java.lang.Float]) = new Convertor(ci)

	implicit def columnInfoToOperableJDouble[T](ci: ColumnInfo[T, java.lang.Double]) = new Convertor(ci)

	implicit def columnInfoToOperableJBoolean[T](ci: ColumnInfo[T, java.lang.Boolean]) = new Convertor(ci)
}

trait SqlManyToOneImplicitConvertions
{

	/**
	 * manages many-to-one expressions
	 */
	implicit def columnInfoManyToOneOperation[T, FID, F](ci: ColumnInfoManyToOne[T, FID, F]) =
		AliasRelationshipColumn[T, FID, F](ci, None)

	implicit def columnInfoManyToOneOperation[T, FID, F](alias: (Symbol, ColumnInfoManyToOne[T, FID, F])) =
		AliasRelationshipColumn(alias._2, Some(alias._1))
}

trait SqlOneToOneImplicitConvertions
{

	/**
	 * manages one-to-one expressions
	 */
	protected class ConvertorOneToOne[T, FID, F](ci: ColumnInfoOneToOne[T, FID, F])
	{
		def ===(v: F) = new OneToOneOperation(ci.column, EQ, v) with EqualityOperation

		def <>(v: F) = new OneToOneOperation(ci.column, NE, v)
	}

	implicit def columnInfoOneToOneOperation[T, FID, F](ci: ColumnInfoOneToOne[T, FID, F]) = new ConvertorOneToOne[T, FID, F](ci)
}

trait SqlRelatedImplicitConvertions
{

	/**
	 * manages one-to-many expressions
	 */
	protected class ConvertorOneToMany[ID, T, FID, F](
		ci: ColumnInfoTraversableOneToMany[ID, T, FID, F]
		)
	{
		def ===(v: F) = new OneToManyOperation(ci.column, EQ, v)

		def <>(v: F) = new OneToManyOperation(ci.column, NE, v)
	}

	implicit def columnInfoOneToManyOperation[ID, T, FID, F](
		ci: ColumnInfoTraversableOneToMany[ID, T, FID, F]
		) = new ConvertorOneToMany(ci)

	protected class ConvertorOneToManyDeclaredPrimaryKey[FID, F, TID, T](
		ci: ColumnInfoTraversableOneToManyDeclaredPrimaryKey[FID, F, TID, T]
		)
	{
		def ===(v: F) = new OneToManyDeclaredPrimaryKeyOperation(ci.declaredColumnInfo.column, EQ, v, ci.declaredColumnInfo.entityOfT)

		def <>(v: F) = new OneToManyDeclaredPrimaryKeyOperation(ci.declaredColumnInfo.column, NE, v, ci.declaredColumnInfo.entityOfT)
	}

	implicit def columnInfoOneToManyForDeclaredPrimaryKeyOperation[FID, F, TID, T](
		ci: ColumnInfoTraversableOneToManyDeclaredPrimaryKey[FID, F, TID, T]
		) =
		new ConvertorOneToManyDeclaredPrimaryKey[FID, F, TID, T](ci)

	/**
	 * manages many-to-many expressions
	 */
	protected class ConvertorManyToMany[T, FID, F](ci: ColumnInfoTraversableManyToMany[T, FID, F])
	{
		def ===(v: F) = new ManyToManyOperation(ci.column, EQ, v)

		def <>(v: F) = new ManyToManyOperation(ci.column, NE, v)
	}

	implicit def columnInfoManyToManyOperation[T, FID, F](ci: ColumnInfoTraversableManyToMany[T, FID, F]) = new ConvertorManyToMany[T, FID, F](ci)

	/**
	 * manages one-to-one reverse expressions
	 */
	protected class ConvertorOneToOneReverse[T, FID, F](ci: ColumnInfoOneToOneReverse[T, FID, F])
	{
		def ===(v: F) = new OneToOneReverseOperation(ci.column, EQ, v)

		def <>(v: F) = new OneToOneReverseOperation(ci.column, NE, v)
	}

	implicit def columnInfoOneToOneReverseOperation[T, FID, F](ci: ColumnInfoOneToOneReverse[T, FID, F]) = new ConvertorOneToOneReverse[T, FID, F](ci)
}