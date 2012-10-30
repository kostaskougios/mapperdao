package com.googlecode.mapperdao

trait SqlClauses[M] { m: M =>
	var clauses: OpBase = _

	def apply(op: OpBase) =
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
trait SqlWhereMixins[M] extends SqlClauses[M] { m: M =>

	def and(op: OpBase) = {
		clauses = AndOp(clauses, op)
		m
	}
	def or(op: OpBase) = {
		clauses = OrOp(clauses, op)
		m
	}
}