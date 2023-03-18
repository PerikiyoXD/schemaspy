// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

public class InvalidConfigurationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	private String paramName;

	public InvalidConfigurationException(final String message)
	{
		super(message);
	}

	public InvalidConfigurationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public InvalidConfigurationException(final Throwable cause)
	{
		super(cause);
	}

	public InvalidConfigurationException setParamName(final String paramName)
	{
		this.paramName = paramName;
		return this;
	}

	public String getParamName()
	{
		return this.paramName;
	}
}
