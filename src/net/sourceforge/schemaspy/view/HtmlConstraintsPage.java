// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sourceforge.schemaspy.DbAnalyzer;
import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.util.HtmlEncoder;
import net.sourceforge.schemaspy.util.LineWriter;

public class HtmlConstraintsPage extends HtmlFormatter
{
	private static HtmlConstraintsPage instance;
	private int columnCounter;

	private HtmlConstraintsPage()
	{
	}

	public static HtmlConstraintsPage getInstance()
	{
		return HtmlConstraintsPage.instance;
	}

	public void write(
			final Database database, final List<ForeignKeyConstraint> list, final Collection<Table> collection,
			final boolean b, final LineWriter lineWriter
	) throws IOException
	{
		this.writeHeader(database, b, lineWriter);
		this.writeForeignKeyConstraints(list, lineWriter);
		this.writeCheckConstraints(collection, lineWriter);
		this.writeFooter(lineWriter);
	}

	private void writeHeader(final Database database, final boolean b, final LineWriter lineWriter) throws IOException
	{
		this.writeHeader(database, null, "Constraints", b, lineWriter);
		lineWriter.writeln("<div class='indent'>");
	}

	@Override
	protected void writeFooter(final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("</div>");
		super.writeFooter(lineWriter);
	}

	private void writeForeignKeyConstraints(final List<ForeignKeyConstraint> list, final LineWriter lineWriter)
			throws IOException
	{
		final TreeSet<ForeignKeyConstraint> set = new TreeSet<ForeignKeyConstraint>();
		set.addAll(list);
		lineWriter.writeln("<table width='100%'>");
		lineWriter.writeln("<tr><td class='container' valign='bottom'><b>");
		lineWriter.write(String.valueOf(set.size()));
		lineWriter.writeln(" Foreign Key Constraints:</b>");
		lineWriter.writeln("</td><td class='container' align='right'>");
		lineWriter.writeln("<table>");
		if (this.sourceForgeLogoEnabled())
		{
			lineWriter.writeln(
					"  <tr><td class='container' align='right' valign='top'><a href='http://sourceforge.net' target='_blank'><img src='http://sourceforge.net/sflogo.php?group_id=137197&amp;type=1' alt='SourceForge.net' border='0' height='31' width='88'></a></td></tr>"
			);
		}
		lineWriter.writeln("<tr><td class='container'>");
		this.writeFeedMe(lineWriter);
		lineWriter.writeln("</td></tr></table>");
		lineWriter.writeln("</td></tr>");
		lineWriter.writeln("</table><br>");
		lineWriter.writeln("<table class='dataTable' border='1' rules='groups'>");
		lineWriter.writeln("<colgroup>");
		lineWriter.writeln("<colgroup>");
		lineWriter.writeln("<colgroup>");
		lineWriter.writeln("<colgroup>");
		lineWriter.writeln("<thead align='left'>");
		lineWriter.writeln("<tr>");
		lineWriter.writeln("  <th>Constraint Name</th>");
		lineWriter.writeln("  <th>Child Column</th>");
		lineWriter.writeln("  <th>Parent Column</th>");
		lineWriter.writeln("  <th>Delete Rule</th>");
		lineWriter.writeln("</tr>");
		lineWriter.writeln("</thead>");
		lineWriter.writeln("<tbody>");
		final Iterator<ForeignKeyConstraint> iterator = set.iterator();
		while (iterator.hasNext())
		{
			this.writeForeignKeyConstraint(iterator.next(), lineWriter);
		}
		if (list.size() == 0)
		{
			lineWriter.writeln(" <tr>");
			lineWriter.writeln("  <td class='detail' valign='top' colspan='4'>None detected</td>");
			lineWriter.writeln(" </tr>");
		}
		lineWriter.writeln("</tbody>");
		lineWriter.writeln("</table>");
	}

	private void writeForeignKeyConstraint(final ForeignKeyConstraint foreignKeyConstraint, final LineWriter lineWriter)
			throws IOException
	{
		if (this.columnCounter++ % 2 == 0)
		{
			lineWriter.writeln("  <tr class='even'>");
		} else
		{
			lineWriter.writeln("  <tr class='odd'>");
		}
		lineWriter.write("  <td class='detail'>");
		lineWriter.write(foreignKeyConstraint.getName());
		lineWriter.writeln("</td>");
		lineWriter.write("  <td class='detail'>");
		final Iterator<TableColumn> iterator = foreignKeyConstraint.getChildColumns().iterator();
		while (iterator.hasNext())
		{
			final TableColumn tableColumn = iterator.next();
			lineWriter.write("<a href='tables/");
			lineWriter.write(tableColumn.getTable().getName());
			lineWriter.write(".html'>");
			lineWriter.write(tableColumn.getTable().getName());
			lineWriter.write("</a>");
			lineWriter.write(".");
			lineWriter.write(tableColumn.getName());
			if (iterator.hasNext())
			{
				lineWriter.write("<br>");
			}
		}
		lineWriter.writeln("</td>");
		lineWriter.write("  <td class='detail'>");
		final Iterator<TableColumn> iterator2 = foreignKeyConstraint.getParentColumns().iterator();
		while (iterator2.hasNext())
		{
			final TableColumn tableColumn2 = iterator2.next();
			lineWriter.write("<a href='tables/");
			lineWriter.write(tableColumn2.getTable().getName());
			lineWriter.write(".html'>");
			lineWriter.write(tableColumn2.getTable().getName());
			lineWriter.write("</a>");
			lineWriter.write(".");
			lineWriter.write(tableColumn2.getName());
			if (iterator2.hasNext())
			{
				lineWriter.write("<br>");
			}
		}
		lineWriter.writeln("</td>");
		lineWriter.write("  <td class='detail'>");
		lineWriter.write(
				"<span title='" + foreignKeyConstraint.getDeleteRuleDescription() + "'>"
						+ foreignKeyConstraint.getDeleteRuleName() + "&nbsp;</span>"
		);
		lineWriter.writeln("</td>");
		lineWriter.writeln(" </tr>");
	}

	public void writeCheckConstraints(final Collection<Table> c, final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("<a name='checkConstraints'></a><p>");
		lineWriter.writeln("<b>Check Constraints:</b>");
		lineWriter.writeln("<TABLE class='dataTable' border='1' rules='groups'>");
		lineWriter.writeln("<colgroup>");
		lineWriter.writeln("<colgroup>");
		lineWriter.writeln("<colgroup>");
		lineWriter.writeln("<thead align='left'>");
		lineWriter.writeln("<tr>");
		lineWriter.writeln("  <th>Table</th>");
		lineWriter.writeln("  <th>Constraint Name</th>");
		lineWriter.writeln("  <th>Constraint</th>");
		lineWriter.writeln("</tr>");
		lineWriter.writeln("</thead>");
		lineWriter.writeln("<tbody>");
		final List<Table> sortTablesByName = DbAnalyzer.sortTablesByName(new ArrayList<Table>(c));
		int n = 0;
		final Iterator<Table> iterator = sortTablesByName.iterator();
		while (iterator.hasNext())
		{
			n += this.writeCheckConstraints(iterator.next(), lineWriter);
		}
		if (n == 0)
		{
			lineWriter.writeln(" <tr>");
			lineWriter.writeln("  <td class='detail' valign='top' colspan='3'>None detected</td>");
			lineWriter.writeln(" </tr>");
		}
		lineWriter.writeln("</tbody>");
		lineWriter.writeln("</table>");
	}

	private int writeCheckConstraints(final Table table, final LineWriter lineWriter) throws IOException
	{
		final Map<String, String> checkConstraints = table.getCheckConstraints();
		int n = 0;
		for (final String str : checkConstraints.keySet())
		{
			lineWriter.writeln(" <tr>");
			lineWriter.write("  <td class='detail' valign='top'><a href='tables/");
			lineWriter.write(table.getName());
			lineWriter.write(".html'>");
			lineWriter.write(table.getName());
			lineWriter.write("</a></td>");
			lineWriter.write("  <td class='detail' valign='top'>");
			lineWriter.write(str);
			lineWriter.writeln("</td>");
			lineWriter.write("  <td class='detail'>");
			lineWriter.write(HtmlEncoder.encodeString(checkConstraints.get(str).toString()));
			lineWriter.writeln("</td>");
			lineWriter.writeln(" </tr>");
			++n;
		}
		return n;
	}

	@Override
	protected boolean isConstraintsPage()
	{
		return true;
	}

	static
	{
		HtmlConstraintsPage.instance = new HtmlConstraintsPage();
	}
}
