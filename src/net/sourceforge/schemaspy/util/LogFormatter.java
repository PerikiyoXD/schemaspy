// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter
{
	private final String lineSeparator;
	private final int MAX_LEVEL_LEN = 7;
	private static final String formatSpec = "HH:mm:ss.";
	private static final ThreadLocal<DateFormat> dateFormatter;
	private static final ThreadLocal<Date> date;

	public LogFormatter()
	{
		this.lineSeparator = System.getProperty("line.separator");
	}

	@Override
	public String format(final LogRecord record)
	{
		final StringBuilder sb = new StringBuilder(128);
		LogFormatter.date.get().setTime(record.getMillis());
		sb.append(LogFormatter.dateFormatter.get().format(LogFormatter.date.get()));
		sb.append(Long.toString(record.getMillis() % 1000L + 1000L).substring(1));
		sb.append(" ");
		final StringBuilder s = new StringBuilder(record.getLevel().getLocalizedName());
		if (s.length() > MAX_LEVEL_LEN)
		{
			s.setLength(MAX_LEVEL_LEN);
		}
		s.append(":");
		while (s.length() < 8)
		{
			s.append(' ');
		}
		sb.append((CharSequence) s);
		sb.append(" ");
		String str;
		if (record.getSourceClassName() != null)
		{
			str = record.getSourceClassName();
		} else
		{
			str = record.getLoggerName();
		}
		final int lastIndex = str.lastIndexOf(46);
		if (lastIndex >= 0 && lastIndex < str.length() - 1)
		{
			str = str.substring(lastIndex + 1);
		}
		sb.append(str);
		if (record.getSourceMethodName() != null)
		{
			sb.append('.');
			sb.append(record.getSourceMethodName());
		}
		sb.append(" - ");
		sb.append(this.formatMessage(record));
		sb.append(this.lineSeparator);
		if (record.getThrown() != null)
		{
			try
			{
				final StringWriter out = new StringWriter();
				record.getThrown().printStackTrace(new PrintWriter(out, true));
				sb.append(out.toString());
			}
			catch (Exception ex)
			{
			}
		}
		return sb.toString();
	}

	static
	{
		dateFormatter = new ThreadLocal<DateFormat>()
		{
			public DateFormat initialValue()
			{
				return new SimpleDateFormat(formatSpec);
			}
		};
		date = new ThreadLocal<Date>()
		{
			public Date initialValue()
			{
				return new Date();
			}
		};
	}
}
