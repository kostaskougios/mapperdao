package com.googlecode.mapperdao

abstract class SimpleEntity[T](table: String, clz: Class[T]) extends Entity[AnyRef, T](table, clz) {
	def this()(implicit m: ClassManifest[T]) = this(m.erasure.getSimpleName, m.erasure.asInstanceOf[Class[T]])
	def this(table: String)(implicit m: ClassManifest[T]) = this(table, m.erasure.asInstanceOf[Class[T]])
}
