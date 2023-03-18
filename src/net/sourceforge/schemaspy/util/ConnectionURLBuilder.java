// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import net.sourceforge.schemaspy.Config;

public class ConnectionURLBuilder
{
	private final String connectionURL;
	private final List<DbSpecificOption> options;
	private final Logger logger;

	public ConnectionURLBuilder(final Config config, final Properties properties)
	{
		this.logger = Logger.getLogger(this.getClass().getName());
		final ArrayList<String> list = new ArrayList<String>();
		for (final String str : config.getDbSpecificOptions().keySet())
		{
			list.add((str.startsWith("-") ? "" : "-") + str);
			list.add(config.getDbSpecificOptions().get(str));
		}
		list.addAll(config.getRemainingParameters());
		this.options = new DbSpecificConfig(config.getDbType()).getOptions();
		this.connectionURL = this.buildUrl(list, properties, config);
		final List<String> remainingParameters = config.getRemainingParameters();
		final Iterator<DbSpecificOption> iterator2 = this.options.iterator();
		while (iterator2.hasNext())
		{
			final int index = remainingParameters.indexOf("-" + iterator2.next().getName());
			if (index >= 0)
			{
				remainingParameters.remove(index);
				remainingParameters.remove(index);
			}
		}
		this.logger.config("connectionURL: " + this.connectionURL);
	}

	private String buildUrl(final List<String> list, final Properties properties, final Config config)
	{
		String s = properties.getProperty("connectionSpec");
		for (final DbSpecificOption dbSpecificOption : this.options)
		{
			dbSpecificOption.setValue(this.getParam(list, dbSpecificOption, config));
			s = s.replaceAll("\\<" + dbSpecificOption.getName() + "\\>", dbSpecificOption.getValue().toString());
		}
		return s;
	}

	public String getConnectionURL()
	{
		return this.connectionURL;
	}

	public List<DbSpecificOption> getOptions()
	{
		return this.options;
	}

	private String getParam(final List<String> list, final DbSpecificOption dbSpecificOption, final Config config)
	{
		String s = null;
		final int index = list.indexOf("-" + dbSpecificOption.getName());
		if (index < 0)
		{
			if (config != null)
			{
				s = config.getParam(dbSpecificOption.getName());
			}
			if (s == null)
			{
				throw new Config.MissingRequiredParameterException(
						dbSpecificOption.getName(), dbSpecificOption.getDescription(), true
				);
			}
		} else
		{
			list.remove(index);
			s = list.get(index).toString();
			list.remove(index);
		}
		return s;
	}
}
