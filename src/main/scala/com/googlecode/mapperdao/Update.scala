package com.googlecode.mapperdao

import com.googlecode.mapperdao.jdbc.UpdateResult

/**
 * @author kostantinos.kougios
 *
 * 29 Oct 2012
 */
object Update extends SqlImplicitConvertions
		with SqlManyToOneImplicitConvertions
		with SqlOneToOneImplicitConvertions {

	def update[ID, PC <: DeclaredIds[ID], T](entity: Entity[ID, PC, T]) =
		new UpdateStart(entity)

	protected class UpdateStart[ID, PC <: DeclaredIds[ID], T](entity: Entity[ID, PC, T]) {
		def set = new UpdateSet

		class UpdateSet extends SqlClauses[UpdateSet, OpBase with EqualityOperation]
				with Updatable[ID, PC, T] {
			def where = new Where
			/**
			 * runs the update
			 */
			def run(queryDao: QueryDao) = queryDao.update(this)

			override private[mapperdao] def entity = UpdateStart.this.entity
			override private[mapperdao] def setClauses = clauses
			override private[mapperdao] def whereClauses = None

			// 2 setters
			def apply(
				op1: OpBase with EqualityOperation,
				op2: OpBase with EqualityOperation) = {
				clauses = CommaOp(op1 :: op2 :: Nil)
				this
			}

			// 3 setters
			def apply(
				op1: OpBase with EqualityOperation,
				op2: OpBase with EqualityOperation,
				op3: OpBase with EqualityOperation) = {
				clauses = CommaOp(op1 :: op2 :: op3 :: Nil)
				this
			}
			// 4 setters
			def apply(
				op1: OpBase with EqualityOperation,
				op2: OpBase with EqualityOperation,
				op3: OpBase with EqualityOperation,
				op4: OpBase with EqualityOperation) = {
				clauses = CommaOp(op1 :: op2 :: op3 :: op4 :: Nil)
				this
			}
			// 5 setters
			def apply(
				op1: OpBase with EqualityOperation,
				op2: OpBase with EqualityOperation,
				op3: OpBase with EqualityOperation,
				op4: OpBase with EqualityOperation,
				op5: OpBase with EqualityOperation) = {
				clauses = CommaOp(op1 :: op2 :: op3 :: op4 :: op5 :: Nil)
				this
			}
			// 6 setters
			def apply(
				op1: OpBase with EqualityOperation,
				op2: OpBase with EqualityOperation,
				op3: OpBase with EqualityOperation,
				op4: OpBase with EqualityOperation,
				op5: OpBase with EqualityOperation,
				op6: OpBase with EqualityOperation) = {
				clauses = CommaOp(op1 :: op2 :: op3 :: op4 :: op5 :: op6 :: Nil)
				this
			}

			// any number of setters via a list
			def apply(ops: List[OpBase with EqualityOperation]) = {
				clauses = CommaOp(ops)
				this
			}

			class Where extends SqlWhereMixins[Where] with Updatable[ID, PC, T] {

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

	trait Updatable[ID, PC <: DeclaredIds[ID], T] {
		private[mapperdao] def entity: Entity[ID, PC, T]
		private[mapperdao] def setClauses: OpBase with EqualityOperation
		private[mapperdao] def whereClauses: Option[OpBase]

		def run(queroDao: QueryDao): UpdateResult
	}
}