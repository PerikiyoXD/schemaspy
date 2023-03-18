// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ConsolePasswordReader extends PasswordReader
{
	private final Object console;
	private final Method readPassword;

	protected ConsolePasswordReader() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException
	{
		this.console = System.class.getMethod("console", (Class<?>[]) null).invoke(null, (Object[]) null);
		this.readPassword = this.console.getClass().getMethod("readPassword", String.class, Object[].class);
	}

	@Override
	public char[] readPassword(final String s, final Object... array)
	{
		try
		{
			return (char[]) this.readPassword.invoke(this.console, s, array);
		}
		catch (Throwable t)
		{
			return super.readPassword(s, array);
		}
	}
}
