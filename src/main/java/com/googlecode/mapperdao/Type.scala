package com.googlecode.mapperdao

case class Type[PC, T](val clz: Class[T], val constructor: (Option[Any], ValuesMap) => T with PC with Persisted, table: Table[PC, T])
