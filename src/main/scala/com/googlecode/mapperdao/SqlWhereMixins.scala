package com.googlecode.mapperdao

trait SqlClauses[M, OP <: OpBase] { m: M =>
	private[mapperdao] var clauses: OpBase = _

	def apply(op: OP) =
		{
			clauses = op
			m
		}
}
/**
 * @author kostantinos.kougios
 *
 * 18 Oct 2012
 */
trait SqlWhereMixins[M, OP <: OpBase] extends SqlClauses[M, OP] { m: M =>

	def and(op: OpBase) = {
		clauses = AndOp(clauses, op)
		m
	}
	def or(op: OpBase) = {
		clauses = OrOp(clauses, op)
		m
	}
}