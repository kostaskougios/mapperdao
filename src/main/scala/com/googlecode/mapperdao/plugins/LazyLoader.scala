package com.googlecode.mapperdao.plugins

/**
 * @author kostantinos.kougios
 *
 *         24 May 2012
 */
abstract class LazyLoader[PC, T] extends (() => Any)