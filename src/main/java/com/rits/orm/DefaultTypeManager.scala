package com.rits.orm
import java.util.Calendar
import org.joda.time.DateTime

/**
 * @author kostantinos.kougios
 *
 * 1 Aug 2011
 */
class DefaultTypeManager extends TypeManager {

	override def deepClone[T](o: T): T = o match {
		case t: scala.collection.mutable.Traversable[_] => t.map(e => e).asInstanceOf[T] // copy mutable traversables
		case cal: Calendar => cal.clone().asInstanceOf[T]
		case _ => o
	}

	override def convert(o: Any): Any = o match {
		case t: java.util.Date => new DateTime(t)
		case _ => o
	}

	override def reverseConvert(o: Any): Any = o match {
		case t: DateTime => t.toCalendar(null)
		case _ => o
	}
}