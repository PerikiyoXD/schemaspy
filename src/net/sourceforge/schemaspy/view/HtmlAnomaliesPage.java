// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.schemaspy.DbAnalyzer;
import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.util.LineWriter;

public class HtmlAnomaliesPage extends HtmlFormatter
{
	private static HtmlAnomaliesPage instance;

	private HtmlAnomaliesPage()
	{
	}

	public static HtmlAnomaliesPage getInstance()
	{
		return HtmlAnomaliesPage.instance;
	}

	public void write(
			final Database database, final Collection<Table> c, final List<? extends ForeignKeyConstraint> list,
			final boolean b, final LineWriter lineWriter
	) throws IOException
	{
		this.writeHeader(database, b, lineWriter);
		this.writeImpliedConstraints(list, lineWriter);
		this.writeTablesWithoutIndexes(DbAnalyzer.getTablesWithoutIndexes(new HashSet<Table>(c)), lineWriter);
		this.writeUniqueNullables(DbAnalyzer.getMustBeUniqueNullableColumns(new HashSet<Table>(c)), lineWriter);
		this.writeTablesWithOneColumn(DbAnalyzer.getTablesWithOneColumn(c), lineWriter);
		this.writeTablesWithIncrementingColumnNames(DbAnalyzer.getTablesWithIncrementingColumnNames(c), lineWriter);
		this.writeDefaultNullStrings(DbAnalyzer.getDefaultNullStringColumns(new HashSet<Table>(c)), lineWriter);
		this.writeFooter(lineWriter);
	}

	private void writeHeader(final Database database, final boolean b, final LineWriter lineWriter) throws IOException
	{
		this.writeHeader(database, null, "Anomalies", b, lineWriter);
		lineWriter.writeln("<table width='100%'>");
		if (this.sourceForgeLogoEnabled())
		{
			lineWriter.writeln(
					"  <tr><td class='container' align='right' valign='top' colspan='2'><a href='http://sourceforge.net' target='_blank'><img src='http://sourceforge.net/sflogo.php?group_id=137197&amp;type=1' alt='SourceForge.net' border='0' height='31' width='88'></a></td></tr>"
			);
		}
		lineWriter.writeln("<tr>");
		lineWriter
				.writeln("<td class='container'><b>Things that might not be 'quite right' about your schema:</b></td>");
		lineWriter.writeln("<td class='container' align='right'>");
		this.writeFeedMe(lineWriter);
		lineWriter.writeln("</td></tr></table>");
		lineWriter.writeln("<ul>");
	}

	private void writeImpliedConstraints(final List<? extends ForeignKeyConstraint> list, final LineWriter lineWriter)
			throws IOException
	{
		lineWriter.writeln("<li>");
		lineWriter.writeln("<b>Columns whose name and type imply a relationship to another table's primary key:</b>");
		int n = 0;
		final Iterator<? extends ForeignKeyConstraint> iterator = list.iterator();
		while (iterator.hasNext())
		{
			if (!((ForeignKeyConstraint) iterator.next()).getChildTable().isView())
			{
				++n;
			}
		}
		if (n > 0)
		{
			lineWriter.writeln("<table class='dataTable' border='1' rules='groups'>");
			lineWriter.writeln("<colgroup>");
			lineWriter.writeln("<colgroup>");
			lineWriter.writeln("<thead align='left'>");
			lineWriter.writeln("<tr>");
			lineWriter.writeln("  <th>Child Column</th>");
			lineWriter.writeln("  <th>Implied Parent Column</th>");
			lineWriter.writeln("</tr>");
			lineWriter.writeln("</thead>");
			lineWriter.writeln("<tbody>");
			for (final ForeignKeyConstraint foreignKeyConstraint : list)
			{
				final Table childTable = foreignKeyConstraint.getChildTable();
				if (!childTable.isView())
				{
					lineWriter.writeln(" <tr>");
					lineWriter.write("  <td class='detail'>");
					final String name = childTable.getName();
					lineWriter.write("<a href='tables/");
					lineWriter.write(name);
					lineWriter.write(".html'>");
					lineWriter.write(name);
					lineWriter.write("</a>.");
					lineWriter.write(ForeignKeyConstraint.toString(foreignKeyConstraint.getChildColumns()));
					lineWriter.writeln("</td>");
					lineWriter.write("  <td class='detail'>");
					final String name2 = foreignKeyConstraint.getParentTable().getName();
					lineWriter.write("<a href='tables/");
					lineWriter.write(name2);
					lineWriter.write(".html'>");
					lineWriter.write(name2);
					lineWriter.write("</a>.");
					lineWriter.write(ForeignKeyConstraint.toString(foreignKeyConstraint.getParentColumns()));
					lineWriter.writeln("</td>");
					lineWriter.writeln(" </tr>");
				}
			}
			lineWriter.writeln("</tbody>");
			lineWriter.writeln("</table>");
		}
		this.writeSummary(n, lineWriter);
		lineWriter.writeln("<p></li>");
	}

	private void writeUniqueNullables(final List<TableColumn> list, final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("<li>");
		lineWriter.writeln("<b>Columns that are flagged as both 'nullable' and 'must be unique':</b>");
		this.writeColumnBasedAnomaly(list, lineWriter);
		lineWriter.writeln("<p></li>");
	}

	private void writeTablesWithoutIndexes(final List<Table> list, final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("<li>");
		lineWriter.writeln("<b>Tables without indexes:</b>");
		if (!list.isEmpty())
		{
			lineWriter.writeln("<table class='dataTable' border='1' rules='groups'>");
			lineWriter.writeln("<colgroup>");
			if (this.displayNumRows)
			{
				lineWriter.writeln("<colgroup>");
			}
			lineWriter.writeln("<thead align='left'>");
			lineWriter.writeln("<tr>");
			lineWriter.write("  <th>Table</th>");
			if (this.displayNumRows)
			{
				lineWriter.write("<th>Rows</th>");
			}
			lineWriter.writeln();
			lineWriter.writeln("</tr>");
			lineWriter.writeln("</thead>");
			lineWriter.writeln("<tbody>");
			for (final Table table : list)
			{
				lineWriter.writeln(" <tr>");
				lineWriter.write("  <td class='detail'>");
				lineWriter.write("<a href='tables/");
				lineWriter.write(table.getName());
				lineWriter.write(".html'>");
				lineWriter.write(table.getName());
				lineWriter.write("</a>");
				lineWriter.writeln("</td>");
				if (this.displayNumRows)
				{
					lineWriter.write("  <td class='detail' align='right'>");
					if (!table.isView())
					{
						lineWriter.write(String.valueOf(NumberFormat.getIntegerInstance().format(table.getNumRows())));
					}
					lineWriter.writeln("</td>");
				}
				lineWriter.writeln(" </tr>");
			}
			lineWriter.writeln("</tbody>");
			lineWriter.writeln("</table>");
		}
		this.writeSummary(list.size(), lineWriter);
		lineWriter.writeln("<p></li>");
	}

	private void writeTablesWithIncrementingColumnNames(final List<Table> list, final LineWriter lineWriter)
			throws IOException
	{
		lineWriter.writeln("<li>");
		lineWriter.writeln("<b>Tables with incrementing column names, potentially indicating denormalization:</b>");
		if (!list.isEmpty())
		{
			lineWriter.writeln("<table class='dataTable' border='1' rules='groups'>");
			lineWriter.writeln("<thead align='left'>");
			lineWriter.writeln("<tr>");
			lineWriter.writeln("  <th>Table</th>");
			lineWriter.writeln("</tr>");
			lineWriter.writeln("</thead>");
			lineWriter.writeln("<tbody>");
			for (final Table table : list)
			{
				lineWriter.writeln(" <tr>");
				lineWriter.write("  <td class='detail'>");
				lineWriter.write("<a href='tables/");
				lineWriter.write(table.getName());
				lineWriter.write(".html'>");
				lineWriter.write(table.getName());
				lineWriter.write("</a>");
				lineWriter.writeln("</td>");
				lineWriter.writeln(" </tr>");
			}
			lineWriter.writeln("</tbody>");
			lineWriter.writeln("</table>");
		}
		this.writeSummary(list.size(), lineWriter);
		lineWriter.writeln("<p></li>");
	}

	private void writeTablesWithOneColumn(final List<Table> list, final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("<li>");
		lineWriter.write("<b>Tables that contain a single column:</b>");
		if (!list.isEmpty())
		{
			lineWriter.writeln("<table class='dataTable' border='1' rules='groups'>");
			lineWriter.writeln("<colgroup>");
			lineWriter.writeln("<colgroup>");
			lineWriter.writeln("<thead align='left'>");
			lineWriter.writeln("<tr>");
			lineWriter.writeln("  <th>Table</th>");
			lineWriter.writeln("  <th>Column</th>");
			lineWriter.writeln("</tr>");
			lineWriter.writeln("</thead>");
			lineWriter.writeln("<tbody>");
			for (final Table table : list)
			{
				lineWriter.writeln(" <tr>");
				lineWriter.write("  <td class='detail'>");
				lineWriter.write("<a href='tables/");
				lineWriter.write(table.getName());
				lineWriter.write(".html'>");
				lineWriter.write(table.getName());
				lineWriter.write("</a></td><td class='detail'>");
				lineWriter.write(table.getColumns().get(0).toString());
				lineWriter.writeln("</td>");
				lineWriter.writeln(" </tr>");
			}
			lineWriter.writeln("</tbody>");
			lineWriter.writeln("</table>");
		}
		this.writeSummary(list.size(), lineWriter);
		lineWriter.writeln("<p></li>");
	}

	private void writeDefaultNullStrings(final List<TableColumn> list, final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("<li>");
		lineWriter.writeln(
				"<b>Columns whose default value is the word 'NULL' or 'null', but the SQL NULL value may have been intended:</b>"
		);
		this.writeColumnBasedAnomaly(list, lineWriter);
		lineWriter.writeln("<p></li>");
	}

	private void writeColumnBasedAnomaly(final List<TableColumn> list, final LineWriter lineWriter) throws IOException
	{
		if (!list.isEmpty())
		{
			lineWriter.writeln("<table class='dataTable' border='1' rules='groups'>");
			lineWriter.writeln("<thead align='left'>");
			lineWriter.writeln("<tr>");
			lineWriter.writeln("  <th>Column</th>");
			lineWriter.writeln("</tr>");
			lineWriter.writeln("</thead>");
			lineWriter.writeln("<tbody>");
			for (final TableColumn tableColumn : list)
			{
				lineWriter.writeln(" <tr>");
				lineWriter.write("  <td class='detail'>");
				final String name = tableColumn.getTable().getName();
				lineWriter.write("<a href='tables/");
				lineWriter.write(name);
				lineWriter.write(".html'>");
				lineWriter.write(name);
				lineWriter.write("</a>.");
				lineWriter.write(tableColumn.getName());
				lineWriter.writeln("</td>");
				lineWriter.writeln(" </tr>");
			}
			lineWriter.writeln("</tbody>");
			lineWriter.writeln("</table>");
		}
		this.writeSummary(list.size(), lineWriter);
	}

	private void writeSummary(final int i, final LineWriter lineWriter) throws IOException
	{
		switch (i)
		{
			case 0:
			{
				lineWriter.write("<br>Anomaly not detected");
				break;
			}
			case 1:
			{
				lineWriter.write("1 instance of anomaly detected");
				break;
			}
			default:
			{
				lineWriter.write(i + " instances of anomaly detected");
				break;
			}
		}
	}

	@Override
	protected void writeFooter(final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("</ul>");
		super.writeFooter(lineWriter);
	}

	@Override
	protected boolean isAnomaliesPage()
	{
		return true;
	}

	static
	{
		HtmlAnomaliesPage.instance = new HtmlAnomaliesPage();
	}
}
