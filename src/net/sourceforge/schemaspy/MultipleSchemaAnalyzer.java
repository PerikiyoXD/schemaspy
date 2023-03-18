// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.sourceforge.schemaspy.model.ProcessExecutionException;
import net.sourceforge.schemaspy.util.LineWriter;
import net.sourceforge.schemaspy.view.HtmlMultipleSchemasIndexPage;

public final class MultipleSchemaAnalyzer
{
	private static MultipleSchemaAnalyzer instance;
	private final Logger logger;
	private final boolean fineEnabled;

	private MultipleSchemaAnalyzer()
	{
		this.logger = Logger.getLogger(this.getClass().getName());
		this.fineEnabled = this.logger.isLoggable(Level.FINE);
	}

	public static MultipleSchemaAnalyzer getInstance()
	{
		return MultipleSchemaAnalyzer.instance;
	}

	public void analyze(
			final String s, final DatabaseMetaData databaseMetaData, final String str, final List<String> list,
			final List<String> list2, final String s2, final File file, final String s3, final String pathname
	) throws SQLException, IOException
	{
		final long currentTimeMillis = System.currentTimeMillis();
		final ArrayList<String> c = new ArrayList<String>();
		c.add("java");
		c.add("-Doneofmultipleschemas=true");
		if (new File(pathname).isDirectory())
		{
			c.add("-cp");
			c.add(pathname);
			c.add(Main.class.getName());
		} else
		{
			c.add("-jar");
			c.add(pathname);
		}
		for (final String str2 : list2)
		{
			if (str2.startsWith("-"))
			{
				c.add(str2);
			} else
			{
				c.add("\"" + str2 + "\"");
			}
		}
		List<String> populatedSchemas;
		if (list == null)
		{
			System.out.println("Analyzing schemas that match regular expression '" + str + "':");
			System.out.println("(use -schemaSpec on command line or in .properties to exclude other schemas)");
			populatedSchemas = this.getPopulatedSchemas(databaseMetaData, str, s2);
		} else
		{
			System.out.println("Analyzing schemas:");
			populatedSchemas = list;
		}
		final Iterator<String> iterator2 = populatedSchemas.iterator();
		while (iterator2.hasNext())
		{
			System.out.print(" " + iterator2.next());
		}
		System.out.println();
		this.writeIndexPage(s, populatedSchemas, databaseMetaData, file, s3);
		for (final String s4 : populatedSchemas)
		{
			final ArrayList<String> list3 = new ArrayList<String>(c);
			if (s == null)
			{
				list3.add("-db");
			} else
			{
				list3.add("-s");
			}
			list3.add(s4);
			list3.add("-o");
			list3.add(new File(file, s4).toString());
			System.out.println("Analyzing " + s4);
			System.out.flush();
			final Process exec = Runtime.getRuntime().exec(list3.toArray(new String[0]));
			new ProcessOutputReader(exec.getInputStream(), System.out).start();
			new ProcessOutputReader(exec.getErrorStream(), System.err).start();
			try
			{
				final int wait = exec.waitFor();
				if (wait != 0)
				{
					final StringBuilder sb = new StringBuilder("Failed to execute this process (rc " + wait + "):");
					for (final String str3 : list3)
					{
						sb.append(" ");
						sb.append(str3);
					}
					throw new ProcessExecutionException(sb.toString());
				}
				continue;
			}
			catch (InterruptedException ex)
			{
			}
		}
		final long currentTimeMillis2 = System.currentTimeMillis();
		System.out.println();
		System.out.println(
				"Wrote relationship details of " + populatedSchemas.size() + " schema"
						+ ((populatedSchemas.size() == 1) ? "" : "s") + " in "
						+ (currentTimeMillis2 - currentTimeMillis) / 1000L + " seconds."
		);
		System.out.println("Start with " + new File(file, "index.html"));
	}

	public void analyze(
			final String s, final List<String> list, final List<String> list2, final String s2, final File file,
			final String s3, final String s4
	) throws SQLException, IOException
	{
		this.analyze(s, null, null, list, list2, s2, file, s3, s4);
	}

	private void writeIndexPage(
			final String s, final List<String> list, final DatabaseMetaData databaseMetaData, final File parent,
			final String s2
	) throws IOException
	{
		if (list.size() > 0)
		{
			final LineWriter lineWriter = new LineWriter(new File(parent, "index.html"), s2);
			HtmlMultipleSchemasIndexPage.getInstance().write(s, list, databaseMetaData, lineWriter);
			lineWriter.close();
		}
	}

	private List<String> getPopulatedSchemas(
			final DatabaseMetaData databaseMetaData, final String regex, final String s
	) throws SQLException
	{
		List<String> list;
		if (databaseMetaData.supportsSchemasInTableDefinitions())
		{
			final Pattern compile = Pattern.compile(regex);
			list = DbAnalyzer.getPopulatedSchemas(databaseMetaData, regex);
			final Iterator<String> iterator = list.iterator();
			while (iterator.hasNext())
			{
				final String str = iterator.next();
				if (!compile.matcher(str).matches())
				{
					if (this.fineEnabled)
					{
						this.logger.fine("Excluding schema " + str + ": doesn't match + \"" + compile + '\"');
					}
					iterator.remove();
				} else
				{
					if (!this.fineEnabled)
					{
						continue;
					}
					this.logger.fine("Including schema " + str + ": matches + \"" + compile + '\"');
				}
			}
		} else
		{
			list = Arrays.asList(s);
		}
		return list;
	}

	static
	{
		MultipleSchemaAnalyzer.instance = new MultipleSchemaAnalyzer();
	}

	private static class ProcessOutputReader extends Thread
	{
		private final Reader processReader;
		private final PrintStream out;

		ProcessOutputReader(final InputStream in, final PrintStream out)
		{
			this.processReader = new InputStreamReader(in);
			this.out = out;
			this.setDaemon(true);
		}

		@Override
		public void run()
		{
			try
			{
				int read;
				while ((read = this.processReader.read()) != -1)
				{
					this.out.print((char) read);
					this.out.flush();
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
