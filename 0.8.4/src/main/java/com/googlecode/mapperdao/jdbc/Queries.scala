package com.googlecode.mapperdao.jdbc
import java.io.InputStream
import scala.io.Source

/**
 * load & execute queries from text files
 *
 * @author kostantinos.kougios
 *
 * 30 Sep 2011
 */
class Queries private (jdbc: Jdbc, in: InputStream) {

	if (in == null) throw new IllegalArgumentException("in parameter is null")

	private val m: Map[String, List[String]] =
		{
			val source = Source.fromInputStream(in)
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
	def apply(jdbc: Jdbc, in: InputStream) = new Queries(jdbc, in)

	def fromClassPath(clz: Class[_], jdbc: Jdbc, resource: String) = new Queries(jdbc, clz.getResourceAsStream(resource))
}