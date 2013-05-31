package com.googlecode.mapperdao.state.recreation

import com.googlecode.mapperdao._
import com.googlecode.mapperdao.plugins._
import com.googlecode.mapperdao.schema.Type

/**
 * @author kostantinos.kougios
 *
 *         11 Dec 2012
 */
class MockFactory(typeManager: TypeManager, typeRegistry: TypeRegistry)
{
	private val mockPlugins: List[SelectMock] = List(
		new OneToManyMockPlugin,
		new ManyToManyMockPlugin,
		new ManyToOneMockPlugin,
		new OneToOneMockPlugin
	)

	def createMock[ID, T](
		data: Option[Any],
		tpe: Type[ID, T],
		mods: Map[String, Any]
		): T with Persisted = {
		val mockMods = new scala.collection.mutable.HashMap[String, Any] ++ mods
		mockPlugins.foreach {
			_.updateMock(tpe, mockMods)
		}
		val vm = ValuesMap.fromMap(null, mockMods)
		val persistDetails = typeRegistry.persistDetails(tpe)
		val preMock = tpe.constructor(persistDetails, data, vm)
		val mock = tpe.constructor(persistDetails, data, ValuesMap.fromType(typeManager, tpe, preMock))
		// mark it as mock
		mock.mapperDaoValuesMap.mock = true
		mock.mapperDaoValuesMap.o = mock
		mock
	}

}