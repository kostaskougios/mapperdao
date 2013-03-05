package com.googlecode.mapperdao.javatests.custom;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: kostas.kougios
 * Date: 05/03/13
 */
public class Attributes implements Iterable<Attribute>
{
	private List<Attribute> l = new LinkedList<>();

	@Override
	public Iterator<Attribute> iterator()
	{
		return l.iterator();
	}

	public Attributes(Iterable<Attribute> it)
	{
		for (Attribute a : it)
		{
			l.add(a);
		}
	}
}
