package com.rits.orm.drivers
import com.rits.jdbc.Jdbc
import com.rits.orm.TypeRegistry

/**
 * @author kostantinos.kougios
 *
 * 2 Sep 2011
 */
class Mysql(override val jdbc: Jdbc, override val typeRegistry: TypeRegistry) extends Driver {
}