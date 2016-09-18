package com.googlecode.classgenerator

import java.lang.reflect.Field
import java.util.IdentityHashMap

/**
  * @author kostantinos.kougios
  *
  *         4 Jun 2012
  */
class DeepFieldVisitor(
	reflectionManager: ReflectionManager,
	dontVisit: Any => Boolean = DeepFieldVisitor.dontVisit
)
{

	def visit[T, R](t: T)(visitor: (Any, Field, Any) => R): List[R] = visitInner(t, new IdentityHashMap, visitor)

	private def visitInner[R](o: Any, m: IdentityHashMap[Any, Any], visitor: (Any, Field, Any) => R): List[R] =
		if (!m.containsKey(o)) {
			m.put(o, o)
			val fields = reflectionManager.fields(o.getClass).filterNot(_.getName == "$outer")
			fields.map { field =>
				val v = reflectionManager.get[Any, Any](field, o)
				val r = visitor(o, field, v)
				r :: (if (v == null) Nil
				else {
					if (dontVisit(v))
						Nil
					else
						visitInner(v, m, visitor)
				})
			}.toList.flatten
		} else Nil

	def visitTwo[T, R](t1: T, t2: T)(visitor: (Any, Any, Field, Any, Any) => R): List[R] = visitTwoInner(t1, t2, new IdentityHashMap, visitor)

	private def visitTwoInner[R](o1: Any, o2: Any, m: IdentityHashMap[Any, Any], visitor: (Any, Any, Field, Any, Any) => R): List[R] =
		if (!m.containsKey(o1)) {
			m.put(o1, o1)
			val fields1 = reflectionManager.fields(o1.getClass).filterNot(_.getName == "$outer")
			val fields2 = reflectionManager.fields(o2.getClass)
			fields1.filter(fields2(_)).map { field =>
				val v1 = reflectionManager.get[Any, AnyRef](field, o1)
				val v2 = reflectionManager.get[Any, AnyRef](field, o2)
				val r = visitor(o1, o2, field, v1, v2)
				r :: (if (v1 == null || v2 == null) Nil
				else {
					if (dontVisit(v1))
						Nil
					else
						visitTwoInner(v1, v2, m, visitor)
				})
			}.toList.flatten
		} else Nil
}

object DeepFieldVisitor
{
	val dontVisit = (v: Any) => v.getClass.getName.startsWith("java.lang.")
}