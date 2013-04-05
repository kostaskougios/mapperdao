package com.googlecode.mapperdao

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import com.googlecode.mapperdao.schema._
import com.googlecode.mapperdao.schema.ColumnInfo
import com.googlecode.mapperdao.schema.ColumnInfoManyToOne
import com.googlecode.mapperdao.schema.ColumnInfoOneToOne
import com.googlecode.mapperdao.utils.EntityRelationshipVisitor

/**
 * @author kostantinos.kougios
 *
 *         22 May 2012
 */
@RunWith(classOf[JUnitRunner])
class EntityRelationshipVisitorSuite extends FunSuite with ShouldMatchers
{

	import CommonEntities._

	class Visitor extends EntityRelationshipVisitor[Any](visitLazyLoaded = true, visitUnlinked = true)
	{
		override def manyToMany[T, F](ci: ColumnInfoTraversableManyToMany[T, _, F], traversable: Traversable[F], collected: Traversable[Any]) =
			collected

		override def oneToMany[T, F](ci: ColumnInfoTraversableOneToMany[_, T, _, F], traversable: Traversable[F], collected: Traversable[Any]) =
			collected

		override def manyToOne[T, F](ci: ColumnInfoManyToOne[T, _, F], foreign: F) =
			foreign

		override def oneToOne[T, F](ci: ColumnInfoOneToOne[T, _, _], foreign: F) =
			foreign

		override def oneToOneReverse[T, F](ci: ColumnInfoOneToOneReverse[T, _, _], foreign: F) =
			foreign

		override def simple[T](ci: ColumnInfo[T, _], v: Any): Any = v

		override def createR(collected: List[(ColumnInfoBase[Any, _], Any)], entity: Entity[_, _, _], o: Any) = {
			val m = collected.map {
				case (ci, v) =>
					(ci.column.alias, v)
			}.toMap
			val vm = ValuesMap.fromMap(System.identityHashCode(o), m)
			entity.constructor(vm)
		}
	}

	test("visit manyToMany") {
		val visitor = new Visitor
		val p = Product("jeans", Set(Attribute("colour", "blue"), Attribute("colour", "green")))
		visitor.visit(ProductEntity, p) match {
			case vp: Product =>
				vp should not be eq(p)
				vp should be === (p)
		}
	}

	test("visit many to one") {
		val visitor = new Visitor
		val p = Person("kostas", Company("r-it"))
		visitor.visit(PersonEntity, p) match {
			case vp: Person =>
				vp should not be eq(p)
				vp should be === (p)
		}
	}

	test("visit one to many") {
		val visitor = new Visitor
		val o = Owner("kostas", Set(House("London"), House("Rhodes")))
		visitor.visit(OwnerEntity, o) match {
			case vo: Owner =>
				vo should not be eq(o)
				vo should be === (o)
		}
	}

	test("visit one to one") {
		val visitor = new Visitor
		val o = Husband("k", 40, new Wife("t", 39))
		visitor.visit(HusbandEntity, o) match {
			case vo: Husband =>
				vo should not be eq(o)
				vo should be === (o)
		}
	}
}