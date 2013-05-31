package com.googlecode.mapperdao.internal

import com.googlecode.mapperdao.{TypeManager, EntityBase}

/**
 * @author: kostas.kougios
 *          Date: 31/05/13
 */
class PersistedDetails(
	val entity: EntityBase[_, _],
	val typeManager: TypeManager
	)
