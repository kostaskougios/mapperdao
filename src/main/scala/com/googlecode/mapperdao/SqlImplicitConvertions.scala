package com.googlecode.mapperdao

import org.joda.time.DateTime

/**
 * @author kostantinos.kougios
 *
 * 17 Oct 2012
 */
trait SqlImplicitConvertions {
	/**
	 * manages simple type expressions
	 */
	protected class Convertor[T, V](t: ColumnInfo[T, V]) {
		def >(v: V) = new Operation(t.column, GT(), v)
		def >(v: ColumnInfo[_, V]) = new Operation(t.column, GT(), v.column)

		def >=(v: V) = new Operation(t.column, GE(), v)
		def >=(v: ColumnInfo[_, V]) = new Operation(t.column, GE(), v.column)

		def <(v: V) = new Operation(t.column, LT(), v)
		def <(v: ColumnInfo[_, V]) = new Operation(t.column, LT(), v.column)

		def <>(v: V) = new Operation(t.column, NE(), v)
		def <>(v: ColumnInfo[_, V]) = new Operation(t.column, NE(), v.column)

		def <=(v: V) = new Operation(t.column, LE(), v)
		def <=(v: ColumnInfo[_, V]) = new Operation(t.column, LE(), v.column)

		def ===(v: V) = new Operation(t.column, EQ(), v)
		def ===(v: ColumnInfo[_, V]) = new Operation(t.column, EQ(), v.column)

		def like(v: V) = new Operation(t.column, LIKE(), v)
		def like(v: ColumnInfo[_, V]) = new Operation(t.column, LIKE(), v.column)
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

trait SqlManyToOneImplicitConvertions {
	/**
	 * manages many-to-one expressions
	 */
	protected class ConvertorManyToOne[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoManyToOne[T, FID, FPC, F]) {
		def ===(v: F) = new ManyToOneOperation(ci.column, EQ(), v)
		def <>(v: F) = new ManyToOneOperation(ci.column, NE(), v)
	}
	implicit def columnInfoManyToOneOperation[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoManyToOne[T, FID, FPC, F]) =
		new ConvertorManyToOne(ci)
}

trait SqlRelatedImplicitConvertions {
	/**
	 * manages one-to-many expressions
	 */
	protected class ConvertorOneToMany[ID, PC <: DeclaredIds[ID], T, FID, FPC <: DeclaredIds[FID], F](
			ci: ColumnInfoTraversableOneToMany[ID, PC, T, FID, FPC, F]) {
		def ===(v: F) = new OneToManyOperation(ci.column, EQ(), v)
		def <>(v: F) = new OneToManyOperation(ci.column, NE(), v)
	}
	implicit def columnInfoOneToManyOperation[ID, PC <: DeclaredIds[ID], T, FID, FPC <: DeclaredIds[FID], F](
		ci: ColumnInfoTraversableOneToMany[ID, PC, T, FID, FPC, F]) = new ConvertorOneToMany(ci)

	protected class ConvertorOneToManyDeclaredPrimaryKey[FID, FPC <: DeclaredIds[FID], F, TID, TPC <: DeclaredIds[TID], T](
			ci: ColumnInfoTraversableOneToManyDeclaredPrimaryKey[FID, FPC, F, TID, TPC, T]) {
		def ===(v: F) = new OneToManyDeclaredPrimaryKeyOperation(ci.declaredColumnInfo.column, EQ(), v, ci.declaredColumnInfo.entityOfT)
		def <>(v: F) = new OneToManyDeclaredPrimaryKeyOperation(ci.declaredColumnInfo.column, NE(), v, ci.declaredColumnInfo.entityOfT)
	}
	implicit def columnInfoOneToManyForDeclaredPrimaryKeyOperation[FID, FPC <: DeclaredIds[FID], F, TID, TPC <: DeclaredIds[TID], T](
		ci: ColumnInfoTraversableOneToManyDeclaredPrimaryKey[FID, FPC, F, TID, TPC, T]) =
		new ConvertorOneToManyDeclaredPrimaryKey[FID, FPC, F, TID, TPC, T](ci)

	/**
	 * manages many-to-many expressions
	 */
	protected class ConvertorManyToMany[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoTraversableManyToMany[T, FID, FPC, F]) {
		def ===(v: F) = new ManyToManyOperation(ci.column, EQ(), v)
		def <>(v: F) = new ManyToManyOperation(ci.column, NE(), v)
	}
	implicit def columnInfoManyToManyOperation[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoTraversableManyToMany[T, FID, FPC, F]) = new ConvertorManyToMany[T, FID, FPC, F](ci)

	/**
	 * manages one-to-one expressions
	 */
	protected class ConvertorOneToOne[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoOneToOne[T, FID, FPC, F]) {
		def ===(v: F) = new OneToOneOperation(ci.column, EQ(), v)
		def <>(v: F) = new OneToOneOperation(ci.column, NE(), v)
	}
	implicit def columnInfoOneToOneOperation[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoOneToOne[T, FID, FPC, F]) = new ConvertorOneToOne[T, FID, FPC, F](ci)

	/**
	 * manages one-to-one reverse expressions
	 */
	protected class ConvertorOneToOneReverse[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoOneToOneReverse[T, FID, FPC, F]) {
		def ===(v: F) = new OneToOneReverseOperation(ci.column, EQ(), v)
		def <>(v: F) = new OneToOneReverseOperation(ci.column, NE(), v)
	}
	implicit def columnInfoOneToOneReverseOperation[T, FID, FPC <: DeclaredIds[FID], F](ci: ColumnInfoOneToOneReverse[T, FID, FPC, F]) = new ConvertorOneToOneReverse[T, FID, FPC, F](ci)
}