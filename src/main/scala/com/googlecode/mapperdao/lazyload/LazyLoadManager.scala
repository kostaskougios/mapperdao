package com.googlecode.mapperdao.lazyload

import java.lang.reflect.Method

import scala.collection.mutable.ListMap

import org.objenesis.ObjenesisStd

import com.googlecode.classgenerator.ClassManager
import com.googlecode.classgenerator.MethodImplementation
import com.googlecode.classgenerator.ReflectionManager
import javassist._
import com.googlecode.mapperdao.schema.ColumnInfoRelationshipBase
import com.googlecode.mapperdao._

/**
 * manages lazy loading of classes
 *
 * @author kostantinos.kougios
 *
 *         18 Apr 2012
 */
private[mapperdao] class LazyLoadManager
{

	import LazyLoadManager._

	type CacheKey = (Class[_], LazyLoad)

	private val classCache = new scala.collection.mutable.HashMap[CacheKey, (Class[_], Map[String, ColumnInfoRelationshipBase[_, Any, Any, Any]])]

	def proxyFor[ID, T](constructed: T with Persisted, entity: EntityBase[ID, T], lazyLoad: LazyLoad, vm: ValuesMap): T with Persisted = {
		if (constructed == null) throw new NullPointerException("constructed can't be null")

		val clz = entity.clz
		val constructedClz = constructed.getClass
		// find all relationships that should be proxied
		val relationships = entity.tpe.table.allRelationshipColumnInfos

		val key = (clz, lazyLoad)
		val lazyRelationships = relationships.filter(lazyLoad.isLazyLoaded(_))
		// get cached proxy class or generate it
		val (proxyClz, methodToCI) = classCache.synchronized {
			classCache.get(key).getOrElse {
				val methods = lazyRelationships.map(ci =>
					ci.getterMethod.getOrElse(
						throw new IllegalStateException("please define getter method on entity %s . %s".format(entity.getClass.getName, ci.column))
					).getterMethod
				).toSet
				if (methods.isEmpty)
					throw new IllegalStateException("can't lazy load class that doesn't declare any getters for relationships. Entity: %s".format(clz))
				val proxyClz = createProxyClz(constructedClz, clz, methods)

				val methodToCI = lazyRelationships.map {
					ci =>
						(ci.getterMethod.get.getterMethod.getName, ci.asInstanceOf[ColumnInfoRelationshipBase[T, Any, Any, Any]])
				}.toMap
				val r = (proxyClz, methodToCI)
				classCache.put(key, r)
				r
			}
		}

		val instantiator = objenesis.getInstantiatorOf(proxyClz)
		val instance = instantiator.newInstance.asInstanceOf[DeclaredIds[ID] with T with MethodImplementation[T with Persisted]]

		// copy data from constructed to instance
		reflectionManager.copy(clz, constructed, instance)
		if (hasIntId(constructedClz) || hasLongId(constructedClz)) {
			reflectionManager.copy("id", constructed, instance)
		}

		// prepare the dynamic function

		// memory optimization for unlinked entities
		val toLazyLoad = ListMap.empty ++ lazyRelationships.map {
			ci =>
				(ci.asInstanceOf[ColumnInfoRelationshipBase[T, Any, Any, Any]], vm.columnValue[() => Any](ci))
		}.toMap

		val llpm = new LazyLoadProxyMethod[T](toLazyLoad, methodToCI.asInstanceOf[Map[String, ColumnInfoRelationshipBase[T, Any, Any, Any]]])
		llpm.mapperDaoValuesMap = vm
		instance.methodImplementation(llpm)
		instance
	}

	private def createProxyClz(constructedClz: Class[_], originalClz: Class[_], methods: Set[Method]) = {
		val b = classManager.buildNewSubclass(originalClz)
			.interface[DeclaredIds[_]]
			.interface[LazyLoaded]
			.implementFromTrait[LazyLoaded](false)
			.implementFromTrait[DeclaredIds[_]](false)

		if (hasIntId(constructedClz)) {
			guardIdField(originalClz)
			b.interface[SurrogateIntId]
			b.field("private int id;")
			b.methodWithSrc( """
					public int id() {
						return id;
					}""")
		} else if (hasLongId(constructedClz)) {
			guardIdField(originalClz)
			b.interface[SurrogateLongId]
			b.field("private long id;")
			b.methodWithSrc( """
					public long id() {
						return id;
					}""")
		}

		b.overrideMethods(methods)
			.overrideSettersIfExist(originalClz, methods)

		b.get
	}

	private def guardIdField(constructedClz: Class[_]) {
		if (reflectionManager.fields(constructedClz).find(_.getName == "id").isDefined)
			throw new IllegalStateException(constructedClz.getName + " already contains a field 'id'")
	}

	private def hasIntId(clz: Class[_]) = classOf[SurrogateIntId].isAssignableFrom(clz)

	private def hasLongId(clz: Class[_]) = classOf[SurrogateLongId].isAssignableFrom(clz)

	def isLazyLoaded(lazyLoad: LazyLoad, entity: EntityBase[_, _]) =
		(lazyLoad.all ||
			lazyLoad.isAnyColumnLazyLoaded(
				entity.tpe.table.allRelationshipColumnInfosSet.asInstanceOf[Set[ColumnInfoRelationshipBase[_, _, _, _]]]
			)
			) && !entity.tpe.table.allRelationshipColumnInfos.isEmpty
}

object LazyLoadManager
{
	private val classManager = new ClassManager(pool = {
		val cp = new ClassPool(null)
		cp.appendClassPath(new LoaderClassPath(getClass.getClassLoader))
		cp
	})
	private val objenesis = new ObjenesisStd
	private[mapperdao] val reflectionManager = new ReflectionManager
	private[mapperdao] val persistedMethods = reflectionManager.methods(classOf[Persisted]).toSet
	private[mapperdao] val persistedMethodNamesToMethod = persistedMethods.map {
		m =>
			(m.getName, m)
	}.toMap

}