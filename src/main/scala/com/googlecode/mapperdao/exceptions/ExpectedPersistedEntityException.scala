package com.googlecode.mapperdao.exceptions

/**
 * @author kostantinos.kougios
 *
 * 26 Oct 2012
 */
class ExpectedPersistedEntityException(val entity: Any)
	extends IllegalStateException("expected persisted entity but was " + entity)