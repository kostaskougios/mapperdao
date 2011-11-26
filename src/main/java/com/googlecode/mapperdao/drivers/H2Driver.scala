package com.googlecode.mapperdao.drivers
import com.googlecode.mapperdao.jdbc.Jdbc
import com.googlecode.mapperdao.TypeRegistry

/**
 * @author kostantinos.kougios
 *
 * 23 Nov 2011
 */
class H2Driver(override val jdbc: Jdbc, override val typeRegistry: TypeRegistry) extends Driver {

}