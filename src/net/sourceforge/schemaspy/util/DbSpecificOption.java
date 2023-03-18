// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

public class DbSpecificOption
{
	private final String name;
	private Object value;
	private final String description;

	public DbSpecificOption(final String name, final String value, final String description)
	{
		this.name = name;
		this.value = value;
		this.description = description;
	}

	public DbSpecificOption(final String s, final String s2)
	{
		this(s, null, s2);
	}

	public String getName()
	{
		return this.name;
	}

	public Object getValue()
	{
		return this.value;
	}

	public void setValue(final Object value)
	{
		this.value = value;
	}

	public String getDescription()
	{
		return this.description;
	}

	@Override
	public String toString()
	{
		return this.description;
	}
}
