package com.googlecode.classgenerator.runtime

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

	private val m = scala.collection.mutable.Map[Int, Array[Class[_]]]()
	private var c = 0

	// called reflectively
	def get(cnt: Int) = m.synchronized {
		m.remove(cnt).get
	}

	protected[classgenerator] def register[T, RT](argTypes: Array[Class[_]]) =
		m.synchronized {
			c += 1
			m += c -> argTypes
			c
		}
}
