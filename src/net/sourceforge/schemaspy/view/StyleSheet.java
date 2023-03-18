// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.model.InvalidConfigurationException;
import net.sourceforge.schemaspy.util.LineWriter;

public class StyleSheet
{
	private static StyleSheet instance;
	private final String css;
	private String bodyBackgroundColor;
	private String tableHeadBackgroundColor;
	private String tableBackgroundColor;
	private String linkColor;
	private String linkVisitedColor;
	private String primaryKeyBackgroundColor;
	private String indexedColumnBackgroundColor;
	private String selectedTableBackgroundColor;
	private String excludedColumnBackgroundColor;
	private final List<String> ids;

	private StyleSheet(final BufferedReader bufferedReader) throws IOException
	{
		this.ids = new ArrayList<String>();
		final String property = System.getProperty("line.separator");
		final StringBuilder sb = new StringBuilder();
		String line;
		while ((line = bufferedReader.readLine()) != null)
		{
			sb.append(line);
			sb.append(property);
		}
		this.css = sb.toString();
		for (int i = sb.indexOf("/*"); i != -1; i = sb.indexOf("/*"))
		{
			sb.replace(i, sb.indexOf("*/") + 2, "");
		}
		final StringTokenizer stringTokenizer = new StringTokenizer(sb.toString(), "{}");
		String lowerCase = null;
		while (stringTokenizer.hasMoreTokens())
		{
			final String trim = stringTokenizer.nextToken().trim();
			if (lowerCase == null)
			{
				lowerCase = trim.toLowerCase();
				this.ids.add(lowerCase);
			} else
			{
				final Map<String, String> attributes = this.parseAttributes(trim);
				if (lowerCase.equals(".content"))
				{
					this.bodyBackgroundColor = attributes.get("background");
				} else if (lowerCase.equals("th"))
				{
					this.tableHeadBackgroundColor = attributes.get("background-color");
				} else if (lowerCase.equals("td"))
				{
					this.tableBackgroundColor = attributes.get("background-color");
				} else if (lowerCase.equals(".primarykey"))
				{
					this.primaryKeyBackgroundColor = attributes.get("background");
				} else if (lowerCase.equals(".indexedcolumn"))
				{
					this.indexedColumnBackgroundColor = attributes.get("background");
				} else if (lowerCase.equals(".selectedtable"))
				{
					this.selectedTableBackgroundColor = attributes.get("background");
				} else if (lowerCase.equals(".excludedcolumn"))
				{
					this.excludedColumnBackgroundColor = attributes.get("background");
				} else if (lowerCase.equals("a:link"))
				{
					this.linkColor = attributes.get("color");
				} else if (lowerCase.equals("a:visited"))
				{
					this.linkVisitedColor = attributes.get("color");
				}
				lowerCase = null;
			}
		}
	}

	public static StyleSheet getInstance() throws ParseException
	{
		if (StyleSheet.instance == null)
		{
			try
			{
				StyleSheet.instance = new StyleSheet(new BufferedReader(getReader(Config.getInstance().getCss())));
			}
			catch (IOException ex)
			{
				throw new ParseException(ex);
			}
		}
		return StyleSheet.instance;
	}

	private static Reader getReader(final String s) throws IOException
	{
		final File file = new File(s);
		if (file.exists())
		{
			return new FileReader(file);
		}
		final File file2 = new File(System.getProperty("user.dir"), s);
		if (file2.exists())
		{
			return new FileReader(file2);
		}
		final InputStream resourceAsStream = StyleSheet.class.getClassLoader().getResourceAsStream(s);
		if (resourceAsStream == null)
		{
			throw new ParseException("Unable to find requested style sheet: " + s);
		}
		return new InputStreamReader(resourceAsStream);
	}

	private Map<String, String> parseAttributes(final String s)
	{
		final HashMap<String, String> hashMap = new HashMap<String, String>();
		try
		{
			final StringTokenizer stringTokenizer = new StringTokenizer(s, ";");
			while (stringTokenizer.hasMoreTokens())
			{
				final StringTokenizer stringTokenizer2 = new StringTokenizer(stringTokenizer.nextToken(), ":");
				hashMap.put(
						stringTokenizer2.nextToken().trim().toLowerCase(),
						stringTokenizer2.nextToken().trim().toLowerCase()
				);
			}
		}
		catch (NoSuchElementException ex)
		{
			System.err.println("Failed to extract attributes from '" + s + "'");
			throw ex;
		}
		return hashMap;
	}

	public void write(final LineWriter lineWriter) throws IOException
	{
		lineWriter.write(this.css);
	}

	public String getBodyBackground()
	{
		if (this.bodyBackgroundColor == null)
		{
			throw new MissingCssPropertyException(".content", "background");
		}
		return this.bodyBackgroundColor;
	}

	public String getTableBackground()
	{
		if (this.tableBackgroundColor == null)
		{
			throw new MissingCssPropertyException("td", "background-color");
		}
		return this.tableBackgroundColor;
	}

	public String getTableHeadBackground()
	{
		if (this.tableHeadBackgroundColor == null)
		{
			throw new MissingCssPropertyException("th", "background-color");
		}
		return this.tableHeadBackgroundColor;
	}

	public String getPrimaryKeyBackground()
	{
		if (this.primaryKeyBackgroundColor == null)
		{
			throw new MissingCssPropertyException(".primaryKey", "background");
		}
		return this.primaryKeyBackgroundColor;
	}

	public String getIndexedColumnBackground()
	{
		if (this.indexedColumnBackgroundColor == null)
		{
			throw new MissingCssPropertyException(".indexedColumn", "background");
		}
		return this.indexedColumnBackgroundColor;
	}

	public String getSelectedTableBackground()
	{
		if (this.selectedTableBackgroundColor == null)
		{
			throw new MissingCssPropertyException(".selectedTable", "background");
		}
		return this.selectedTableBackgroundColor;
	}

	public String getExcludedColumnBackgroundColor()
	{
		if (this.excludedColumnBackgroundColor == null)
		{
			throw new MissingCssPropertyException(".excludedColumn", "background");
		}
		return this.excludedColumnBackgroundColor;
	}

	public String getLinkColor()
	{
		if (this.linkColor == null)
		{
			throw new MissingCssPropertyException("a:link", "color");
		}
		return this.linkColor;
	}

	public String getLinkVisitedColor()
	{
		if (this.linkVisitedColor == null)
		{
			throw new MissingCssPropertyException("a:visited", "color");
		}
		return this.linkVisitedColor;
	}

	public static class MissingCssPropertyException extends InvalidConfigurationException
	{
		private static final long serialVersionUID = 1L;

		public MissingCssPropertyException(final String str, final String str2)
		{
			super(
					"Required property '" + str2 + "' was not found for the definition of '" + str + "' in "
							+ Config.getInstance().getCss()
			);
		}
	}

	public static class ParseException extends InvalidConfigurationException
	{
		private static final long serialVersionUID = 1L;

		public ParseException(final Exception ex)
		{
			super(ex);
		}

		public ParseException(final String s)
		{
			super(s);
		}
	}
}
