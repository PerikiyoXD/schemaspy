// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

public class ConnectionFailure extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ConnectionFailure(final String message)
	{
		super(message);
	}

	public ConnectionFailure(final String str, final Throwable cause)
	{
		super(str + " " + cause.getMessage(), cause);
	}

	public ConnectionFailure(final Throwable cause)
	{
		super(cause);
	}
}
