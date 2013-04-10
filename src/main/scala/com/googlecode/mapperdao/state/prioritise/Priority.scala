package com.googlecode.mapperdao.state.prioritise

/**
 * @author: kostas.kougios
 *          Date: 28/12/12
 */
trait Priority

object Priority
{

	object High extends Priority

	object Low extends Priority

	object Lowest extends Priority

	object Related extends Priority

	object Dependant extends Priority

}
