// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

public class ProcessExecutionException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ProcessExecutionException(final String message)
	{
		super(message);
	}

	public ProcessExecutionException(final String str, final Throwable cause)
	{
		super(str + " " + cause.getMessage(), cause);
	}

	public ProcessExecutionException(final Throwable cause)
	{
		super(cause);
	}
}
