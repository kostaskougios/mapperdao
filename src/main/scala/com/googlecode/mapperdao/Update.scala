package com.googlecode.mapperdao

import com.googlecode.mapperdao.queries.{SqlWhereMixins, SqlOneToOneImplicitConvertions, SqlRelationshipImplicitConvertions, SqlImplicitConvertions}


/**
 * @author kostantinos.kougios
 *
 *         29 Oct 2012
 */
object Update extends SqlImplicitConvertions
with SqlRelationshipImplicitConvertions
with SqlOneToOneImplicitConvertions
{

	def update[ID, PC <: Persisted, T](entity: Entity[ID, PC, T]) =
		new UpdateStart(entity)

	protected class UpdateStart[ID, PC <: Persisted, T](entity: Entity[ID, PC, T])
	{
		def set = new UpdateSet

		class UpdateSet extends Updatable[ID, T]
		{
			private[mapperdao] var clauses: OpBase with EqualityOperation = _

			override private[mapperdao] def entity = UpdateStart.this.entity

			override private[mapperdao] def setClauses = clauses

			override private[mapperdao] def whereClauses = None

			// 1 setter
			def apply(
				op1: OpBase with EqualityOperation
				) = {
				clauses = CommaOp(op1 :: Nil)
				WhereKeyword
			}

			// 2 setters
			def apply(
				op1: OpBase with EqualityOperation,
				op2: OpBase with EqualityOperation
				) = {
				clauses = CommaOp(op1 :: op2 :: Nil)
				WhereKeyword
			}

			// 3 setters
			def apply(
				op1: OpBase with EqualityOperation,
				op2: OpBase with EqualityOperation,
				op3: OpBase with EqualityOperation
				) = {
				clauses = CommaOp(op1 :: op2 :: op3 :: Nil)
				WhereKeyword
			}

			// 4 setters
			def apply(
				op1: OpBase with EqualityOperation,
				op2: OpBase with EqualityOperation,
				op3: OpBase with EqualityOperation,
				op4: OpBase with EqualityOperation
				) = {
				clauses = CommaOp(op1 :: op2 :: op3 :: op4 :: Nil)
				WhereKeyword
			}

			// 5 setters
			def apply(
				op1: OpBase with EqualityOperation,
				op2: OpBase with EqualityOperation,
				op3: OpBase with EqualityOperation,
				op4: OpBase with EqualityOperation,
				op5: OpBase with EqualityOperation
				) = {
				clauses = CommaOp(op1 :: op2 :: op3 :: op4 :: op5 :: Nil)
				WhereKeyword
			}

			// 6 setters
			def apply(
				op1: OpBase with EqualityOperation,
				op2: OpBase with EqualityOperation,
				op3: OpBase with EqualityOperation,
				op4: OpBase with EqualityOperation,
				op5: OpBase with EqualityOperation,
				op6: OpBase with EqualityOperation
				) = {
				clauses = CommaOp(op1 :: op2 :: op3 :: op4 :: op5 :: op6 :: Nil)
				WhereKeyword
			}

			// any number of setters via a list
			def apply(ops: List[OpBase with EqualityOperation]) = {
				clauses = CommaOp(ops)
				WhereKeyword
			}

			object WhereKeyword
			{
				def where = new Where

				/**
				 * runs the update
				 */
				def run(queryDao: QueryDao) = queryDao.update(UpdateSet.this)
			}

			class Where extends SqlWhereMixins[Where] with Updatable[ID, T]
			{
				/**
				 * runs the update
				 */
				def run(queryDao: QueryDao) = queryDao.update(this)

				override private[mapperdao] def entity = UpdateStart.this.entity

				override private[mapperdao] def setClauses = UpdateSet.this.setClauses

				override private[mapperdao] def whereClauses = Some(clauses)
			}

		}

	}

	trait Updatable[ID, T]
	{
		private[mapperdao] def entity: Entity[ID, Persisted, T]

		private[mapperdao] def setClauses: OpBase with EqualityOperation

		private[mapperdao] def whereClauses: Option[OpBase]
	}

}