// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;

public class PasswordReader
{
	private static PasswordReader instance;

	public static synchronized PasswordReader getInstance()
	{
		if (PasswordReader.instance == null)
		{
			try
			{
				PasswordReader.instance = new ConsolePasswordReader();
			}
			catch (Throwable t)
			{
				PasswordReader.instance = new PasswordReader();
			}
		}
		return PasswordReader.instance;
	}

	protected PasswordReader()
	{
	}

	public char[] readPassword(final String format, final Object... args)
	{
		InputStream in = System.in;
		char[] a2;
		char[] a = a2 = new char[128];
		int length = a2.length;
		int n = 0;
		int i = 1;
		final Masker masker = new Masker(String.format(format, args));
		masker.start();
		try
		{
			while (i != 0)
			{
				final int read;
				switch (read = in.read())
				{
					case -1:
					case 10:
					{
						i = 0;
						continue;
					}
					case 13:
					{
						final int read2 = in.read();
						if (read2 != 10 && read2 != -1)
						{
							if (!(in instanceof PushbackInputStream))
							{
								in = new PushbackInputStream(in);
							}
							((PushbackInputStream) in).unread(read2);
							continue;
						}
						i = 0;
						continue;
					}
					default:
					{
						if (--length < 0)
						{
							a2 = new char[n + 128];
							length = a2.length - n - 1;
							System.arraycopy(a, 0, a2, 0, n);
							Arrays.fill(a, ' ');
							a = a2;
						}
						a2[n++] = (char) read;
						continue;
					}
				}
			}
		}
		catch (IOException ex)
		{
			throw new IOError(ex);
		}
		finally
		{
			masker.stopMasking();
		}
		if (n == 0)
		{
			return null;
		}
		final char[] array = new char[n];
		System.arraycopy(a2, 0, array, 0, n);
		Arrays.fill(a2, ' ');
		return array;
	}

	private static class Masker extends Thread
	{
		private volatile boolean masking;
		private final String mask;

		public Masker(final String str)
		{
			this.masking = true;
			this.mask = "\r" + str + "     \b\b\b\b\b";
			this.setPriority(Thread.currentThread().getPriority() + 1);
		}

		@Override
		public void run()
		{
			while (this.masking)
			{
				System.out.print(this.mask);
				try
				{
					Thread.sleep(100L);
				}
				catch (InterruptedException ex)
				{
					this.interrupt();
					this.masking = false;
				}
			}
		}

		public void stopMasking()
		{
			this.masking = false;
		}
	}

	public class IOError extends Error
	{
		private static final long serialVersionUID = 20100629L;

		public IOError(final Throwable cause)
		{
			super(cause);
		}
	}
}
