package com.googlecode.mapperdao.jdbc

import scala.io.Source

/**
 * load & execute queries from text files
 *
 * @author kostantinos.kougios
 *
 *         30 Sep 2011
 */
class Queries private(jdbc: Jdbc, source: Source) {

	if (jdbc == null) throw new NullPointerException("jdbc parameter is null")
	if (source == null) throw new NullPointerException("source parameter is null")

	private val m: Map[String, List[String]] = {
		val lines = source.getLines().toList.reverse.iterator

		def parseSql(list: List[String]): List[String] = {
			val line = lines.next
			if (line.trim == ";") {
				val s = list.mkString("\n").trim()
				if (s.isEmpty) parseSql(Nil) else parseSql(Nil) ::: List(s)
			} else if (line.startsWith("#")) {
				parseSql(list)
			} else if (line.startsWith("[") && line.endsWith("]")) {
				val alias = line.substring(1, line.length - 1)
				alias :: List(list.mkString("\n").trim())
			} else {
				parseSql(line :: list)
			}
		}

		def parse: List[(String, List[String])] = if (lines.hasNext) {
			val l = parseSql(Nil)
			(l.head, l.tail) :: parse
		} else Nil

		parse.toMap
	}

	def getAlias(sqlAlias: String) = m(sqlAlias)

	def update(sqlAlias: String, args: Any*) = m(sqlAlias).map(jdbc.update(_, args: _*))

	override def toString = "Queries(%s)".format(m)
}

object Queries {
	def apply(jdbc: Jdbc, source: Source) = new Queries(jdbc, source)

	def fromClassPath(clz: Class[_], jdbc: Jdbc, resource: String) = {
		val source = Source.fromInputStream(clz.getResourceAsStream(resource))
		new Queries(jdbc, source)
	}
}