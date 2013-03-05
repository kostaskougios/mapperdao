package com.googlecode.mapperdao.javatests.custom;

/**
 * @author kostantinos.kougios
 *         <p/>
 *         3 Jul 2012
 */
public class Product
{
	private String name;
	private Attributes attributes;

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
