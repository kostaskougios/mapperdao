package com.googlecode.classgenerator

import java.lang.reflect.Method

import com.googlecode.classgenerator.runtime.MImpl
import javassist._

/**
  * @author kostantinos.kougios
  *
  *         8 Apr 2012
  */
class ClassBuilder[T](val pool: ClassPool, val ctClass: CtClass, val reflectionManager: ReflectionManager)
{

	def name(name: String): this.type = {
		ctClass.setName(name)
		this
	}

	def interface[I1](clz: String): ClassBuilder[T with I1] = {
		ctClass.addInterface(pool.get(clz))
		this.asInstanceOf[ClassBuilder[T with I1]]
	}

	def interface[I1](implicit m1: ClassManifest[I1]): ClassBuilder[T with I1] = interface[I1](m1.erasure.getName)

	def field(javaSrc: String): this.type = {
		val field = CtField.make(javaSrc, ctClass)
		ctClass.addField(field)
		this
	}

	def methodWithSrc(javaSrc: String): this.type = {
		val m = CtMethod.make(javaSrc, ctClass)
		ctClass.addMethod(m)
		this
	}

	private var methodGlue = false

	class MethodBuilder[RT](name: String)
	{
		private var ret: Class[_] = null
		private var art: List[Class[_]] = Nil
		private var superEnabled = false

		def returnType[R](implicit m: ClassManifest[R]): MethodBuilder[R] = {
			ret = m.erasure
			this.asInstanceOf[MethodBuilder[R]]
		}

		def returnType[R](tpe: Class[R]) = {
			ret = tpe
			this.asInstanceOf[MethodBuilder[R]]
		}

		def argTypes(args: List[Class[_]]): this.type = {
			art = args
			this
		}

		def argTypes[A1](implicit m1: ClassManifest[A1]): this.type = argTypes(List(m1.erasure))

		def argTypes[A1, A2](implicit m1: ClassManifest[A1], m2: ClassManifest[A2]): this.type = argTypes(List(m1.erasure, m2.erasure))

		def argTypes[A1, A2, A3](implicit m1: ClassManifest[A1], m2: ClassManifest[A2], m3: ClassManifest[A3]): this.type = argTypes(List(m1.erasure, m2.erasure, m3.erasure))

		/**
		  * enables args.proceed
		  */
		def enableSuperMethodInvocation = {
			superEnabled = true
			this
		}

		def implementation: ClassBuilder[T with MethodImplementation[T]] = {
			val rt = toType(ret)
			val a = art.toArray
			val at = a.map(toType)
			val m = new CtMethod(rt, name, at, ctClass)

			val methodCounter = MImpl.register(a)

			if (!methodGlue) {
				interface[MethodImplementation[T]]

				field(
					"""
					private scala.Function1 _m$_impl;
					""")
				var implMethod = new CtMethod(
					CtClass.voidType,
					"methodImplementation",
					Array(pool.get("scala.Function1")),
					ctClass)
				ctClass.addMethod(implMethod)
				implMethod.setBody(
					"""{
					_m$_impl=$1;
			}""")
				methodGlue = true
			}
			// a lot of rubbish here in order to provide the required functionality
			field(
				"""
					private static Class[] __M$_ARGS%d = MImpl.get(%d);
				""".format(methodCounter, methodCounter))

			field(
				"""
					private static java.lang.reflect.Method __M$_METHOD%d = %s.class.getMethod("%s",__M$_ARGS%d);
				""".format(methodCounter, ctClass.getName, name, methodCounter, methodCounter))

			val sp = if (superEnabled) {
				field(
					"""
					private static java.lang.reflect.Method __M$_SUPER_METHOD%d = %s.class.getMethod("%s_$%d",(Class[]) __M$_ARGS%d);
					""".format(methodCounter, ctClass.getName, name, methodCounter, methodCounter))

				val superMethod = new CtMethod(rt, name + "_$" + methodCounter, at, ctClass)
				ctClass.addMethod(superMethod)
				val body = if (ret == null) {
					"""{
						super.%s($$);
				}""".format(name)
				} else {
					"""{
						return ($r) super.%s($$);
				}""".format(name)
				}
				superMethod.setBody(body)
				"__M$_SUPER_METHOD%d".format(methodCounter)
			} else "null"

			ctClass.addMethod(m)
			m.setBody(
				"""{
				Args args=new Args((java.lang.reflect.Method)__M$_METHOD%d,%s,$0,$args);
				return ($r) ((scala.Function1) _m$_impl).apply(args);
				}""".format(methodCounter, sp, methodCounter))
			ctClass.setModifiers(ctClass.getModifiers & ~Modifier.ABSTRACT)
			ClassBuilder.this.asInstanceOf[ClassBuilder[T with MethodImplementation[T]]]
		}
	}

	def method(name: String) = new MethodBuilder[Unit](name)

	private def toType(clz: Class[_]) = if (clz == null) CtClass.voidType else pool.get(clz.getName)

	def superClass[S](clz: String): ClassBuilder[T with S] = {
		ctClass.setSuperclass(pool.get(clz))
		this.asInstanceOf[ClassBuilder[T with S]]
	}

	def superClass[S](clz: Class[S]): ClassBuilder[T with S] = superClass(clz.getName)

	def superClass[S](implicit m: ClassManifest[S]): ClassBuilder[T with S] = superClass(m.erasure.getName)

	def get: Class[T] = {
		ctClass.toClass.asInstanceOf[Class[T]]
	}

	def implementFromTrait(template: Class[_], enableSuperMethod: Boolean): this.type = {
		template.getMethods.foreach { m =>
			val newMethod = method(m.getName)
				.argTypes(m.getParameterTypes.toList)
				.returnType(m.getReturnType.asInstanceOf[Class[Any]])
			if (enableSuperMethod) newMethod.enableSuperMethodInvocation
			newMethod.implementation
		}
		this
	}

	def implementFromTrait[TR](enableSuperMethod: Boolean)(implicit m: ClassManifest[TR]): this.type = implementFromTrait(m.erasure, enableSuperMethod)

	def overrideMethods(methods: Set[Method]): ClassBuilder[T with MethodImplementation[T]] = {
		methods.foreach { m =>
			method(m.getName)
				.argTypes(m.getParameterTypes.toList)
				.returnType(m.getReturnType.asInstanceOf[Class[Any]])
				.enableSuperMethodInvocation
				.implementation
		}
		this.asInstanceOf[ClassBuilder[T with MethodImplementation[T]]]
	}

	def createMethods(clz: Class[_], methods: Set[Method]): ClassBuilder[T with MethodImplementation[T]] = {
		methods.foreach { m =>
			method(m.getName)
				.returnType(m.getReturnType.asInstanceOf[Class[Any]])
				.implementation
		}
		this.asInstanceOf[ClassBuilder[T with MethodImplementation[T]]]
	}

	def implementSettersIfExistFromTrait(clz: Class[_], template: Class[_]): ClassBuilder[T with MethodImplementation[T]] = {
		template.getMethods
			.map(m => reflectionManager.method(clz, setter(m.getName)))
			.filter(_.isDefined)
			.map(_.get)
			.foreach { m =>
				method(m.getName)
					.argTypes(m.getParameterTypes.toList)
					.enableSuperMethodInvocation
					.implementation
			}
		this.asInstanceOf[ClassBuilder[T with MethodImplementation[T]]]
	}

	def overrideSettersIfExist(clz: Class[_], methods: Set[Method]): this.type = {
		methods
			.map(m => reflectionManager.method(clz, setter(m.getName)))
			.filter(_.isDefined)
			.map(_.get)
			.foreach { m =>
				method(m.getName)
					.argTypes(m.getParameterTypes.toList)
					.enableSuperMethodInvocation
					.implementation
			}
		this
	}
}