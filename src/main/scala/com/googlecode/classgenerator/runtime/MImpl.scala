package com.googlecode.classgenerator.runtime

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
	* temporarily stores class implementation functions and information
	* until the class is loaded for the first time.
	*
	* @author kostantinos.kougios
	*
	*         10 Apr 2012
	*/
object MImpl
{
	type ImplementationFunction[T, RT] = Args[T, RT] => RT

	private val m = new ConcurrentHashMap[Int, Array[Class[_]]]()
	private val c = new AtomicInteger(0)

	def get(cnt: Int) = Option(m.remove(cnt)).get

	protected[classgenerator] def register[T, RT](argTypes: Array[Class[_]]) = {
		val next = c.incrementAndGet()
		val prevous = Option(m.putIfAbsent(next, argTypes))
		if (prevous.isDefined) throw new RuntimeException("Counter already taken. This should never happen!")

		next
	}
}
