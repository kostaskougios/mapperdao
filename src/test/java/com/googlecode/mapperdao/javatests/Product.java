package com.googlecode.mapperdao.javatests;

import java.util.Set;

/**
 * @author kostantinos.kougios
 *
 * 3 Jul 2012
 */
public class Product
{
	private String			name;
	private Set<Attribute>	attributes;

	public Set<Attribute> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(Set<Attribute> attributes)
	{
		this.attributes = attributes;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
