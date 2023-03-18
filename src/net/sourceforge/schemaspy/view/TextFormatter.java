// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.IOException;
import java.util.Collection;

import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.util.LineWriter;

public class TextFormatter
{
	private static TextFormatter instance;

	private TextFormatter()
	{
	}

	public static TextFormatter getInstance()
	{
		return TextFormatter.instance;
	}

	public void write(final Collection<Table> collection, final boolean b, final LineWriter lineWriter)
			throws IOException
	{
		for (final Table table : collection)
		{
			if (!table.isView() || b)
			{
				lineWriter.writeln(table.getName());
			}
		}
	}

	static
	{
		TextFormatter.instance = new TextFormatter();
	}
}
