package com.googlecode.mapperdao

import com.googlecode.classgenerator.ClassManager
import com.googlecode.classgenerator.runtime.Args
import com.googlecode.classgenerator.LazyLoadInstanceFactory
import org.objenesis.ObjenesisStd
import com.googlecode.classgenerator.ReflectionManager
import com.googlecode.classgenerator.MethodImplementation
import java.lang.reflect.Method

/**
 * manages lazy loading of classes
 *
 * @author kostantinos.kougios
 *
 * 18 Apr 2012
 */
private[mapperdao] class LazyLoadManager {

	import LazyLoadManager._

	type CacheKey = (Class[_], LazyLoad)

	private val classCache = new scala.collection.mutable.HashMap[CacheKey, Class[_]]

	private val persistedMethods = reflectionManager.methods(classOf[Persisted]).toSet
	private val persistedMethodNamesToMethod = persistedMethods.map { m =>
		(m.getName, m)
	}.toMap

	// convert collections returned by mapperdao to actual collections
	// required by entities
	private val converters = Map[Class[_], Any => Any](
		classOf[Set[_]] -> { _.asInstanceOf[List[_]].toSet },
		classOf[List[_]] -> { _.asInstanceOf[List[_]] },
		classOf[IndexedSeq[_]] -> { _.asInstanceOf[List[_]].toIndexedSeq },
		classOf[Traversable[_]] -> { _.asInstanceOf[List[_]] }
	)

	def proxyFor[PC, T](constructed: T with PC, entity: Entity[PC, T], lazyLoad: LazyLoad, vm: ValuesMap): T with PC = {
		if (constructed == null) throw new NullPointerException("constructed can't be null")

		val clz = entity.clz
		val constructedClz = constructed.getClass
		// find all relationships that should be proxied
		val relationships = entity.tpe.table.relationshipColumnInfos

		val key = (clz, lazyLoad)
		val lazyRelationships = relationships.filter(lazyLoad.isLazyLoaded(_))
		// get cached proxy class or generate it
		val proxyClz = classCache.synchronized {
			classCache.get(key).getOrElse {
				val methods = lazyRelationships.map(ci =>
					ci.getterMethod.getOrElse(throw new IllegalStateException("please define getter method on entity for %s".format(ci.column))).getterMethod
				).toSet
				if (methods.isEmpty)
					throw new IllegalStateException("can't lazy load class that doesn't declare any getters for relationships. Entity: %s".format(clz))
				val proxyClz = createProxyClz(constructedClz, clz, methods)
				classCache.put(key, proxyClz)
				proxyClz
			}
		}

		val instantiator = objenesis.getInstantiatorOf(proxyClz)
		val instance = instantiator.newInstance.asInstanceOf[PC with T with MethodImplementation[T]]

		// copy data from constructed to instance
		reflectionManager.copy(clz, constructed, instance)
		if (hasIntId(constructedClz) || hasLongId(constructedClz)) {
			reflectionManager.copy("id", constructed, instance)
		}

		// provide an implementation for the proxied methods
		val alreadyCalled = new scala.collection.mutable.HashSet[String]

		// prepare the dynamic function
		val methodToCI = lazyRelationships.map { ci =>
			(ci.getterMethod.get.getterMethod.getName, ci.asInstanceOf[ColumnInfoRelationshipBase[T, Any, Any, Any]])
		}.toMap

		import com.googlecode.classgenerator._
		val persisted = new Persisted {
		}
		persisted.mapperDaoValuesMap = vm

		instance.methodImplementation { args: Args[T, Any] =>
			val methodName = args.methodName
			val persistedMethodOption = persistedMethodNamesToMethod.get(methodName)
			if (persistedMethodOption.isDefined) {
				// method from Persisted trait
				val method = persistedMethodOption.get
				reflectionManager.callMethod(method, persisted, args.args)
			} else if (isSetter(methodName)) {
				// setter
				alreadyCalled += getterFromSetter(args.methodName)
				args.callSuper
			} else {
				// getter
				if (!alreadyCalled(args.methodName)) {
					alreadyCalled += args.methodName

					val ci = methodToCI(args.methodName)
					val gm = ci.getterMethod.get
					val alias = ci.column.alias
					val v = vm.valueOf[Any](alias)
					val r = v match {
						case _: Traversable[_] =>
							val returnType = args.method.getReturnType
							if (returnType.isArray) {
								val ct = returnType.getComponentType
								val am = ClassManifest.fromClass(ct.asInstanceOf[Class[Any]])
								v.asInstanceOf[List[_]].toArray(am)
							} else {
								val con = converters.getOrElse(returnType, gm.converter.getOrElse(throw new IllegalStateException("type %s not supported for getter. Please define a converter function".format(returnType))))
								con(v)
							}
						case _ => v
					}
					reflectionManager.set(gm.fieldName, args.self, r)
					r
				} else {
					args.callSuper
				}
			}
		}
		instance
	}

	private def createProxyClz(constructedClz: Class[_], originalClz: Class[_], methods: Set[Method]) = {
		val b = classManager.buildNewSubclass(originalClz)
			.interface[Persisted]
			.implementFromTrait[Persisted](false)
		if (hasIntId(constructedClz)) {
			b.interface[IntId]
			b.field("private int id;")
			b.methodWithSrc("""
			public int id() {
				return id;
			}""")
		} else if (hasLongId(constructedClz)) {
			b.interface[LongId]
			b.field("private long id;")
			b.methodWithSrc("""
			public long id() {
				return id;
			}""")
		}
		b.overrideMethods(originalClz, methods)
			.overrideSettersIfExist(originalClz, methods)

		b.get
	}

	private def hasIntId(clz: Class[_]) = classOf[IntId].isAssignableFrom(clz)
	private def hasLongId(clz: Class[_]) = classOf[LongId].isAssignableFrom(clz)

	def isLazyLoaded[PC, T](lazyLoad: LazyLoad, entity: Entity[PC, T]) =
		(lazyLoad.all || lazyLoad.isAnyColumnLazyLoaded(entity.tpe.table.relationshipColumnInfos.toSet)) && !entity.tpe.table.relationshipColumnInfos.isEmpty
}

object LazyLoadManager {
	private val classManager = new ClassManager
	private val objenesis = new ObjenesisStd
	private val reflectionManager = new ReflectionManager
}