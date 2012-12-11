package com.googlecode.mapperdao.state.recreation

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.plugins._

/**
 * @author kostantinos.kougios
 *
 * 11 Dec 2012
 */
class MockFactory {
	private val mockPlugins: List[SelectMock] = List(
		new OneToManyMockPlugin,
		new ManyToManyMockPlugin,
		new ManyToOneMockPlugin,
		new OneToOneMockPlugin
	)

	def createMock[ID, PC <: DeclaredIds[ID], T](
		data: Option[Any],
		entity: Entity[ID, PC, T],
		mods: scala.collection.Map[String, Any]): T with PC =
		{
			val mockMods = new scala.collection.mutable.HashMap[String, Any] ++ mods
			mockPlugins.foreach {
				_.updateMock(entity, mockMods)
			}
			val tpe = entity.tpe
			val vm = ValuesMap.fromMap(-1, mockMods)
			val preMock = tpe.constructor(data, vm)
			val mock = tpe.constructor(data, ValuesMap.fromEntity(typeManager, tpe, preMock))
			// mark it as mock
			mock.mapperDaoMock = true
			mock
		}

}