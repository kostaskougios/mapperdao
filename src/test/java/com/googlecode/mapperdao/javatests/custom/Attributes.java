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
	private final List<Attribute> l = new LinkedList<>();

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

	public int size()
	{
		return l.size();
	}

	public boolean contains(Attribute a)
	{
		return l.contains(a);
	}

	public boolean remove(Attribute a)
	{
		return l.remove(a);
	}

	public boolean add(Attribute a)
	{
		return l.add(a);
	}
}
