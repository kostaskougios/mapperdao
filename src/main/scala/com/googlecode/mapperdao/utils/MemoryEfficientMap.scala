package com.googlecode.mapperdao.utils

/**
 * a "map" for maps with few items that is memory efficient and can
 * be mixen in to avoid having 1 more instance
 *
 * @author kostantinos.kougios
 *
 * 28 May 2012
 */
private[mapperdao] trait MemoryEfficientMap[K, V] {

	private var keys: Array[Any] = _
	private var values: Array[Any] = _

	def toMap: Map[K, V] = {
		val t = (keys.zip(values)).toList.asInstanceOf[Traversable[(K, V)]]
		Map.empty ++ t
	}

	def initializeMEM(m: scala.collection.Map[K, V]) {
		var i = 0
		val (ks, vs) = m.map { case (k, v) => (k, v) }.unzip
		keys = ks.toArray
		values = vs.toArray
	}

	private def findKeyIndex(k: K) = {
		keys.indexWhere(kk => k == kk)
	}
	private def findKeyIndexSafe(k: K) = {
		val idx = findKeyIndex(k)
		if (idx == -1) throw new IllegalStateException("cant find key %s".format(k))
		idx
	}

	def containsMEM(k: K) = findKeyIndexSafe(k) > -1

	def getMEM(k: K): V = {
		val idx = findKeyIndexSafe(k)
		values(idx).asInstanceOf[V]
	}

	def getMEMOption(k: K): Option[V] = {
		val idx = findKeyIndex(k)
		if (idx == -1) None
		else
			Some(values(idx).asInstanceOf[V])
	}

	def getMEMOrElse(k: K, orV: V) = {
		val idx = findKeyIndex(k)
		if (idx == -1)
			orV
		else
			values(idx).asInstanceOf[V]
	}

	def putMEM(k: K, v: V) {
		val idx = findKeyIndex(k)
		if (idx == -1) {
			keys ++= List(k)
			values ++= List(v)

		} else
			values(idx) = v
	}

	def memToString = {
		val b = new StringBuilder("MEMMap(")
		for (i <- 0 until keys.size) {
			if (i > 0) b.append(", ")
			b append (keys(i)) append (" -> ") append (values(i))
		}

		b append (")") toString
	}
}

abstract trait SynchronizedMemoryEfficientMap[K, V] extends MemoryEfficientMap[K, V] {
	override def getMEM(k: K): V = synchronized {
		super.getMEM(k)
	}
	override def putMEM(k: K, v: V) =
		synchronized {
			super.putMEM(k, v)
		}
}