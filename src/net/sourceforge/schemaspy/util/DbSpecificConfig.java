// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import net.sourceforge.schemaspy.Config;

public class DbSpecificConfig
{
	private final String type;
	private String description;
	private final List<DbSpecificOption> options;
	private final Config config;

	public DbSpecificConfig(final String type)
	{
		this.options = new ArrayList<DbSpecificOption>();
		this.config = new Config();
		this.type = type;
		try
		{
			final Properties dbProperties = this.config.getDbProperties(type);
			this.description = dbProperties.getProperty("description");
			this.loadOptions(dbProperties);
		}
		catch (IOException ex)
		{
			this.description = ex.toString();
		}
	}

	private void loadOptions(final Properties properties)
	{
		boolean b = false;
		final StringTokenizer stringTokenizer = new StringTokenizer(
				properties.getProperty("connectionSpec"), "<>", true
		);
		while (stringTokenizer.hasMoreTokens())
		{
			final String nextToken = stringTokenizer.nextToken();
			if (nextToken.equals("<"))
			{
				b = true;
			} else if (nextToken.equals(">"))
			{
				b = false;
			} else
			{
				if (!b)
				{
					continue;
				}
				this.options.add(new DbSpecificOption(nextToken, properties.getProperty(nextToken)));
			}
		}
	}

	public List<DbSpecificOption> getOptions()
	{
		return this.options;
	}

	public Config getConfig()
	{
		return this.config;
	}

	public void dumpUsage()
	{
		System.out.println(" " + new File(this.type).getName() + ":");
		System.out.println("  " + this.description);
		for (final DbSpecificOption dbSpecificOption : this.getOptions())
		{
			System.out.println(
					"   -" + dbSpecificOption.getName() + " "
							+ ((dbSpecificOption.getDescription() != null)
									? ("  \t" + dbSpecificOption.getDescription())
									: "")
			);
		}
	}

	@Override
	public String toString()
	{
		return this.description;
	}
}
