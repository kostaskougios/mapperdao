package com.googlecode.mapperdao.queries

import com.googlecode.mapperdao.{OrOp, AndOp, OpBase}

/**
 * @author kostantinos.kougios
 *
 *         18 Oct 2012
 */

trait SqlClauses[M, OP <: OpBase]
{
	m: M =>
	private[mapperdao] var clauses: OP = _

	def apply(op: OP) = {
		clauses = op
		m
	}
}

trait SqlWhereMixins[M] extends SqlClauses[M, OpBase]
{
	m: M =>

	def and(op: OpBase) = {
		clauses = AndOp(clauses, op)
		m
	}

	def or(op: OpBase) = {
		clauses = OrOp(clauses, op)
		m
	}
}