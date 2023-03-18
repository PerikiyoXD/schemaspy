// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.schemaspy.Config;

public class Dot
{
	private static Dot instance;
	private final Version version;
	private final Version supportedVersion;
	private final Version badVersion;
	private final String lineSeparator;
	private String dotExe;
	private String format;
	private String renderer;
	private final Set<String> validatedRenderers;
	private final Set<String> invalidatedRenderers;

	private Dot()
	{
		this.supportedVersion = new Version("2.2.1");
		this.badVersion = new Version("2.4");
		this.lineSeparator = System.getProperty("line.separator");
		this.format = "svg";
		this.validatedRenderers = Collections.synchronizedSet(new HashSet<String>());
		this.invalidatedRenderers = Collections.synchronizedSet(new HashSet<String>());
		String group = null;
		final String[] cmdarray =
		{ this.getExe(), "-V" };
		try
		{
			final String line = new BufferedReader(
					new InputStreamReader(Runtime.getRuntime().exec(cmdarray).getErrorStream())
			).readLine();
			final Matcher matcher = Pattern.compile("[0-9][0-9.]+").matcher(line);
			if (matcher.find())
			{
				group = matcher.group();
			} else if (Config.getInstance().isHtmlGenerationEnabled())
			{
				System.err.println();
				System.err.println(
						"Invalid dot configuration detected.  '" + getDisplayableCommand(cmdarray) + "' returned:"
				);
				System.err.println("   " + line);
			}
		}
		catch (Exception obj)
		{
			if (Config.getInstance().isHtmlGenerationEnabled())
			{
				System.err.println("Failed to query Graphviz version information");
				System.err.println("  with: " + getDisplayableCommand(cmdarray));
				System.err.println("  " + obj);
			}
		}
		this.version = new Version(group);
	}

	public static Dot getInstance()
	{
		return Dot.instance;
	}

	public boolean exists()
	{
		return this.version.toString() != null;
	}

	public Version getVersion()
	{
		return this.version;
	}

	public boolean isValid()
	{
		return this.exists() && (this.getVersion().equals(this.supportedVersion)
				|| this.getVersion().compareTo(this.badVersion) > 0);
	}

	public String getSupportedVersions()
	{
		return "dot version " + this.supportedVersion + " or versions greater than " + this.badVersion;
	}

	public boolean supportsCenteredEastWestEdges()
	{
		return this.getVersion().compareTo(new Version("2.6")) >= 0;
	}

	public void setFormat(final String format)
	{
		this.format = format;
	}

	public String getFormat()
	{
		return this.format;
	}

	public boolean requiresGdRenderer()
	{
		return this.getVersion().compareTo(new Version("2.12")) >= 0 && this.supportsRenderer(":gd");
	}

	public void setRenderer(final String renderer)
	{
		this.renderer = renderer;
	}

	public String getRenderer()
	{
		return (this.renderer != null && this.supportsRenderer(this.renderer)) ? this.renderer
				: (this.requiresGdRenderer() ? ":gdiplus" : "");
	}

	public void setHighQuality(final boolean b)
	{
		if (b && this.supportsRenderer(":cairo"))
		{
			this.setRenderer(":cairo");
		} else if (this.supportsRenderer(":gdiplus"))
		{
			this.setRenderer(":gdiplus");
		}
	}

	public boolean isHighQuality()
	{
		return this.getRenderer().indexOf(":cairo") != -1;
	}

	public boolean supportsRenderer(final String str)
	{
		if (!this.exists())
		{
			return false;
		}
		if (this.validatedRenderers.contains(str))
		{
			return true;
		}
		if (this.invalidatedRenderers.contains(str))
		{
			return false;
		}
		try
		{
			final Process exec = Runtime.getRuntime().exec(new String[]
			{ this.getExe(), "-T" + this.getFormat() + ':' });
			String line;
			while ((line = new BufferedReader(new InputStreamReader(exec.getErrorStream())).readLine()) != null)
			{
				if (line.contains(this.getFormat() + str))
				{
					this.validatedRenderers.add(str);
				}
			}
			exec.waitFor();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		if (!this.validatedRenderers.contains(str))
		{
			this.invalidatedRenderers.add(str);
			return false;
		}
		return true;
	}

	private String getExe()
	{
		if (this.dotExe == null)
		{
			final File graphvizDir = Config.getInstance().getGraphvizDir();
			if (graphvizDir == null)
			{
				this.dotExe = "dot";
			} else
			{
				this.dotExe = new File(new File(graphvizDir, "bin"), "dot").toString();
			}
		}
		return this.dotExe;
	}

	public String generateDiagram(final File file, final File obj) throws DotFailure
	{
		final StringBuilder sb = new StringBuilder(1024);
		BufferedReader bufferedReader = null;
		final String[] cmdarray =
		{ this.getExe(), "-T" + this.getFormat(), file.toString(), "-o" + obj, "-Tcmapx" };
		final String displayableCommand = getDisplayableCommand(cmdarray);
		try
		{
			final Process exec = Runtime.getRuntime().exec(cmdarray);
			new ProcessOutputReader(displayableCommand, exec.getErrorStream()).start();
			bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			String line;
			while ((line = bufferedReader.readLine()) != null)
			{
				sb.append(line);
				sb.append(this.lineSeparator);
			}
			final int wait = exec.waitFor();
			if (wait != 0)
			{
				throw new DotFailure("'" + displayableCommand + "' failed with return code " + wait);
			}
			if (!obj.exists())
			{
				throw new DotFailure("'" + displayableCommand + "' failed to create output file");
			}
			return sb.toString().replace("/>", ">");
		}
		catch (InterruptedException cause)
		{
			throw new RuntimeException(cause);
		}
		catch (DotFailure dotFailure)
		{
			obj.delete();
			throw dotFailure;
		}
		catch (IOException obj2)
		{
			obj.delete();
			throw new DotFailure("'" + displayableCommand + "' failed with exception " + obj2);
		}
		finally
		{
			if (bufferedReader != null)
			{
				try
				{
					bufferedReader.close();
				}
				catch (IOException ex)
				{
				}
			}
		}
	}

	private static String getDisplayableCommand(final String[] array)
	{
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; ++i)
		{
			sb.append(array[i]);
			if (i + 1 < array.length)
			{
				sb.append(' ');
			}
		}
		return sb.toString();
	}

	static
	{
		Dot.instance = new Dot();
	}

	public class DotFailure extends IOException
	{
		private static final long serialVersionUID = 3833743270181351987L;

		public DotFailure(final String message)
		{
			super(message);
		}
	}

	private static class ProcessOutputReader extends Thread
	{
		private final BufferedReader processReader;
		private final String command;

		ProcessOutputReader(final String command, final InputStream in)
		{
			this.processReader = new BufferedReader(new InputStreamReader(in));
			this.command = command;
			this.setDaemon(true);
		}

		@Override
		public void run()
		{
			try
			{
				String line;
				while ((line = this.processReader.readLine()) != null)
				{
					if (line.indexOf("unrecognized") == -1 && line.indexOf("port") == -1)
					{
						System.err.println(this.command + ": " + line);
					}
				}
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
				try
				{
					this.processReader.close();
				}
				catch (Exception ex2)
				{
					ex2.printStackTrace();
				}
			}
			finally
			{
				try
				{
					this.processReader.close();
				}
				catch (Exception ex3)
				{
					ex3.printStackTrace();
				}
			}
		}
	}
}
