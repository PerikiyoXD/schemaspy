// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.util.Dot;
import net.sourceforge.schemaspy.util.LineWriter;

public class HtmlOrphansPage extends HtmlDiagramFormatter
{
	private static HtmlOrphansPage instance;

	private HtmlOrphansPage()
	{
	}

	public static HtmlOrphansPage getInstance()
	{
		return HtmlOrphansPage.instance;
	}

	public boolean write(final Database database, final List<Table> list, final File file, final LineWriter lineWriter)
			throws IOException
	{
		final Dot dot = this.getDot();
		if (dot == null)
		{
			return false;
		}
		final HashSet<Table> set = new HashSet<Table>();
		for (final Table table : list)
		{
			if (!table.isOrphan(true))
			{
				set.add(table);
			}
		}
		this.writeHeader(database, "Utility Tables", !set.isEmpty(), lineWriter);
		lineWriter.writeln("<a name='diagram'>");
		try
		{
			final StringBuilder sb = new StringBuilder(65536);
			for (final Table obj : list)
			{
				final String name = obj.getName();
				final File file2 = new File(file, name + ".1degree.dot");
				final File file3 = new File(file, name + ".1degree.svg");
				final LineWriter lineWriter2 = new LineWriter(file2, "UTF-8");
				DotFormatter.getInstance().writeOrphan(obj, lineWriter2);
				lineWriter2.close();
				try
				{
					sb.append(dot.generateDiagram(file2, file3));
				}
				catch (Dot.DotFailure x)
				{
					System.err.println(x);
					return false;
				}
				lineWriter.write(
						"  <img src='diagrams/summary/" + file3.getName() + "' usemap='#" + obj
								+ "' border='0' alt='' align='top'"
				);
				if (set.contains(obj))
				{
					lineWriter.write(" class='impliedNotOrphan'");
				}
				lineWriter.writeln(">");
			}
			lineWriter.write(sb.toString());
			return true;
		}
		finally
		{
			lineWriter.writeln("</a>");
			this.writeFooter(lineWriter);
		}
	}

	private void writeHeader(final Database database, final String s, final boolean b, final LineWriter lineWriter)
			throws IOException
	{
		this.writeHeader(database, null, s, true, lineWriter);
		lineWriter.writeln("<table class='container' width='100%'>");
		lineWriter.writeln("<tr><td class='container'>");
		this.writeGeneratedBy(database.getConnectTime(), lineWriter);
		lineWriter.writeln("</td>");
		lineWriter.writeln("<td class='container' align='right' valign='top' rowspan='2'>");
		this.writeLegend(false, lineWriter);
		lineWriter.writeln("</td></tr>");
		lineWriter.writeln("<tr><td class='container' align='left' valign='top'>");
		if (b)
		{
			lineWriter.writeln("<form action=''>");
			lineWriter.writeln(" <label for='removeImpliedOrphans'><input type=checkbox id='removeImpliedOrphans'>");
			lineWriter.writeln("  Hide tables with implied relationships</label>");
			lineWriter.writeln("</form>");
		}
		lineWriter.writeln("</td></tr></table>");
	}

	@Override
	protected boolean isOrphansPage()
	{
		return true;
	}

	static
	{
		HtmlOrphansPage.instance = new HtmlOrphansPage();
	}
}
