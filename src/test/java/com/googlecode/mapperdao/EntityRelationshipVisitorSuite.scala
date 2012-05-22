package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite

/**
 * @author kostantinos.kougios
 *
 * 22 May 2012
 */
@RunWith(classOf[JUnitRunner])
class EntityRelationshipVisitorSuite extends FunSuite with ShouldMatchers {

	import CommonEntities._

	val typeManager = new DefaultTypeManager

	class Visitor extends EntityRelationshipVisitor[Any](visitLazyLoaded = true) {
		override def manyToMany[T, F](ci: ColumnInfoTraversableManyToMany[T, _, F], traversable: Traversable[F], collected: Traversable[Any]) =
			collected
		override def oneToMany[T, F](ci: ColumnInfoTraversableOneToMany[T, _, F], traversable: Traversable[F], collected: Traversable[Any]) =
			collected
		override def manyToOne[T, F](ci: ColumnInfoManyToOne[T, _, F], foreign: F) =
			foreign
		override def oneToOne[T, F](ci: ColumnInfoOneToOne[T, _, _], foreign: F) =
			foreign
		override def oneToOneReverse[T, F](ci: ColumnInfoOneToOneReverse[T, _, _], foreign: F) =
			foreign

		override def simple(ci: ColumnInfo[Any, _], v: Any): Any = v

		override def createR(collected: List[(ColumnInfoBase[Any, _], Any)], entity: Entity[_, _], o: Any) = {
			val m = collected.map {
				case (ci, v) =>
					(ci.column.alias, v)
			}.toMap
			val vm = ValuesMap.fromMap(typeManager, m)
			entity.constructor(vm)
		}
	}

	test("visit manyToMany") {
		val visitor = new Visitor
		val p = new Product("jeans", Set(Attribute("colour", "blue"), Attribute("colour", "green")))
		visitor.visit(ProductEntity, p) match {
			case vp: Product =>
				vp should not be eq(p)
				vp should be === (p)
		}

	}
}