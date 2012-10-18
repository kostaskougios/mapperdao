package com.googlecode.mapperdao

/**
 * @author kostantinos.kougios
 *
 * 18 Oct 2012
 */
trait SqlWhereMixins[M] { m: M =>
	var clauses: OpBase = null

	def apply(op: OpBase) =
		{
			clauses = op
			m
		}

	def and(op: OpBase) = {
		clauses = AndOp(clauses, op)
		m
	}
	def or(op: OpBase) = {
		clauses = OrOp(clauses, op)
		m
	}
}