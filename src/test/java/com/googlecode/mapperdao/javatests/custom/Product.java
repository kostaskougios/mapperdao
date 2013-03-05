package com.googlecode.mapperdao.javatests.custom;

import java.util.Arrays;

/**
 * @author kostantinos.kougios
 *         3 Jul 2012
 */
public class Product
{
	private String name;
	private Attributes attributes;

	public Product()
	{
	}

	public Product(String name, Attribute... attributes)
	{
		this.name = name;
		this.attributes = new Attributes(Arrays.asList(attributes));
	}

	public Attributes getAttributes()
	{
		return attributes;
	}

	public void setAttributes(Attributes attributes)
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
