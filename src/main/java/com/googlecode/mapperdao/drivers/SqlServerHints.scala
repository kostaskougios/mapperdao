package com.googlecode.mapperdao.drivers

/**
 * @author kostantinos.kougios
 *
 * May 11, 2012
 */
object SqlServerHints {
	object NoLock extends AfterTableNameSelectHint {
		override def hint = "with(nolock)"
	}
}