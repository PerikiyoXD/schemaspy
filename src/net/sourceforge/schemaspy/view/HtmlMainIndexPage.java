// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.util.HtmlEncoder;
import net.sourceforge.schemaspy.util.LineWriter;

public class HtmlMainIndexPage extends HtmlFormatter
{
	private static HtmlMainIndexPage instance;
	private final NumberFormat integerFormatter;

	private HtmlMainIndexPage()
	{
		this.integerFormatter = NumberFormat.getIntegerInstance();
	}

	public static HtmlMainIndexPage getInstance()
	{
		return HtmlMainIndexPage.instance;
	}

	public void write(
			final Database database, final Collection<Table> collection, final boolean b, final LineWriter lineWriter
	) throws IOException
	{
		final TreeSet<Table> set = new TreeSet<Table>(new Comparator<Table>()
		{
			public int compare(final Table table, final Table table2)
			{
				return table.compareTo(table2);
			}
		});
		set.addAll(collection);
		boolean b2 = false;
		int n = 0;
		boolean b3 = false;
		for (final Table table : set)
		{
			if (table.isView())
			{
				++n;
			}
			b2 |= (table.getId() != null);
			if (table.getComments() != null)
			{
				b3 = true;
			}
		}
		this.writeHeader(database, set.size() - n, n, b2, b, b3, lineWriter);
		int n2 = 0;
		int n3 = 0;
		int n4 = 0;
		for (final Table table2 : set)
		{
			this.writeLineItem(table2, b2, lineWriter);
			if (!table2.isView())
			{
				n2 += table2.getColumns().size();
			} else
			{
				n3 += table2.getColumns().size();
			}
			n4 += table2.getNumRows();
		}
		this.writeFooter(set.size() - n, n2, n, n3, n4, lineWriter);
	}

	private void writeHeader(
			final Database database, final int n, final int n2, final boolean b, final boolean b2, final boolean b3,
			final LineWriter lineWriter
	) throws IOException
	{
		final ArrayList<String> list = new ArrayList<String>();
		list.add("$(function(){");
		list.add("  associate($('#showTables'), $('.tbl'));");
		list.add("  associate($('#showViews'),  $('.view'));");
		list.add("  jQuery.fn.alternateRowColors = function() {");
		list.add("    $('tbody tr:visible').each(function(i) {");
		list.add("      if (i % 2 == 0) {");
		list.add("        $(this).removeClass('even').addClass('odd');");
		list.add("      } else {");
		list.add("        $(this).removeClass('odd').addClass('even');");
		list.add("      }");
		list.add("    });");
		list.add("    return this;");
		list.add("  };");
		list.add("  $('#showTables, #showViews').click(function() {");
		list.add("    $('table.dataTable').alternateRowColors();");
		list.add("  });");
		list.add("  $('table.dataTable').alternateRowColors();");
		list.add("})");
		this.writeHeader(database, null, null, b2, list, lineWriter);
		lineWriter.writeln("<table width='100%'>");
		lineWriter.writeln(" <tr><td class='container'>");
		this.writeGeneratedBy(database.getConnectTime(), lineWriter);
		lineWriter.writeln(" </td></tr>");
		lineWriter.writeln(" <tr>");
		lineWriter.write("  <td class='container'>Database Type: ");
		lineWriter.write(database.getDatabaseProduct());
		lineWriter.writeln("  </td>");
		lineWriter.writeln("  <td class='container' align='right' valign='top' rowspan='3'>");
		if (this.sourceForgeLogoEnabled())
		{
			lineWriter.writeln(
					"    <a href='http://sourceforge.net' target='_blank'><img src='http://sourceforge.net/sflogo.php?group_id=137197&amp;type=1' alt='SourceForge.net' border='0' height='31' width='88'></a><br>"
			);
		}
		lineWriter.write("    <br>");
		this.writeFeedMe(lineWriter);
		lineWriter.writeln("  </td>");
		lineWriter.writeln(" </tr>");
		lineWriter.writeln(" <tr>");
		lineWriter.write("  <td class='container'>");
		String s = database.getName();
		if (database.getSchema() != null)
		{
			s = s + '.' + database.getSchema();
		}
		lineWriter.write("<br><a href='" + s + ".xml' title='XML Representation'>XML Representation</a>");
		lineWriter.write(
				"<br><a href='insertionOrder.txt' title='Useful for loading data into a database'>Insertion Order</a>&nbsp;"
		);
		lineWriter.write(
				"<a href='deletionOrder.txt' title='Useful for purging data from a database'>Deletion Order</a>"
		);
		lineWriter.write("&nbsp;(for database loading/purging scripts)");
		lineWriter.writeln("</td>");
		lineWriter.writeln(" </tr>");
		lineWriter.writeln("</table>");
		lineWriter.writeln("<div class='indent'>");
		lineWriter.write("<p>");
		lineWriter.write("<b>");
		if (n2 == 0)
		{
			lineWriter.writeln(
					"<label for='showTables' style='display:none;'><input type='checkbox' id='showTables' checked></label>"
			);
		} else if (n == 0)
		{
			lineWriter.writeln(
					"<label for='showViews' style='display:none;'><input type='checkbox' id='showViews' checked></label>"
			);
		} else
		{
			lineWriter.write("<label for='showTables'><input type='checkbox' id='showTables' checked>Tables</label>");
			lineWriter.write(" <label for='showViews'><input type='checkbox' id='showViews' checked>Views</label>");
		}
		lineWriter.writeln(
				" <label for='showComments'><input type=checkbox " + (b3 ? "checked " : "")
						+ "id='showComments'>Comments</label>"
		);
		lineWriter.writeln("</b>");
		lineWriter.writeln("<table class='dataTable' border='1' rules='groups'>");
		for (int n3 = 4 + (b ? 1 : 0) + (this.displayNumRows ? 1 : 0), i = 0; i < n3; ++i)
		{
			lineWriter.writeln("<colgroup>");
		}
		lineWriter.writeln("<colgroup class='comment'>");
		lineWriter.writeln("<thead align='left'>");
		lineWriter.writeln("<tr>");
		String str;
		if (n2 == 0)
		{
			str = "Table";
		} else if (n == 0)
		{
			str = "View";
		} else
		{
			str = "Table / View";
		}
		lineWriter.writeln("  <th valign='bottom'>" + str + "</th>");
		if (b)
		{
			lineWriter.writeln("  <th align='center' valign='bottom'>ID</th>");
		}
		lineWriter.writeln("  <th align='right' valign='bottom'>Children</th>");
		lineWriter.writeln("  <th align='right' valign='bottom'>Parents</th>");
		lineWriter.writeln("  <th align='right' valign='bottom'>Columns</th>");
		if (this.displayNumRows)
		{
			lineWriter.writeln("  <th align='right' valign='bottom'>Rows</th>");
		}
		lineWriter.writeln("  <th class='comment' align='left' valign='bottom'>Comments</th>");
		lineWriter.writeln("</tr>");
		lineWriter.writeln("</thead>");
		lineWriter.writeln("<tbody>");
	}

	private void writeLineItem(final Table table, final boolean b, final LineWriter lineWriter) throws IOException
	{
		lineWriter.write(" <tr class='" + (table.isView() ? "view" : "tbl") + "' valign='top'>");
		lineWriter.write("  <td class='detail'><a href='tables/");
		lineWriter.write(table.getName());
		lineWriter.write(".html'>");
		lineWriter.write(table.getName());
		lineWriter.writeln("</a></td>");
		if (b)
		{
			lineWriter.write("  <td class='detail' align='right'>");
			final Object id = table.getId();
			if (id != null)
			{
				lineWriter.write(String.valueOf(id));
			} else
			{
				lineWriter.writeln("&nbsp;");
			}
			lineWriter.writeln("</td>");
		}
		lineWriter.write("  <td class='detail' align='right'>");
		final int numNonImpliedChildren = table.getNumNonImpliedChildren();
		if (numNonImpliedChildren != 0)
		{
			lineWriter.write(String.valueOf(this.integerFormatter.format(numNonImpliedChildren)));
		}
		lineWriter.writeln("</td>");
		lineWriter.write("  <td class='detail' align='right'>");
		final int numNonImpliedParents = table.getNumNonImpliedParents();
		if (numNonImpliedParents != 0)
		{
			lineWriter.write(String.valueOf(this.integerFormatter.format(numNonImpliedParents)));
		}
		lineWriter.writeln("</td>");
		lineWriter.write("  <td class='detail' align='right'>");
		lineWriter.write(String.valueOf(this.integerFormatter.format(table.getColumns().size())));
		lineWriter.writeln("</td>");
		if (this.displayNumRows)
		{
			lineWriter.write("  <td class='detail' align='right'>");
			if (!table.isView())
			{
				lineWriter.write(String.valueOf(this.integerFormatter.format(table.getNumRows())));
			} else
			{
				lineWriter.write("<span title='Views contain no real rows'>view</span>");
			}
			lineWriter.writeln("</td>");
		}
		lineWriter.write("  <td class='comment detail'>");
		final String comments = table.getComments();
		if (comments != null)
		{
			if (this.encodeComments)
			{
				for (int i = 0; i < comments.length(); ++i)
				{
					lineWriter.write(HtmlEncoder.encodeToken(comments.charAt(i)));
				}
			} else
			{
				lineWriter.write(comments);
			}
		}
		lineWriter.writeln("</td>");
		lineWriter.writeln("  </tr>");
	}

	protected void writeFooter(
			final int n, final int n2, final int n3, final int n4, final int n5, final LineWriter lineWriter
	) throws IOException
	{
		lineWriter.writeln("  <tr>");
		lineWriter.writeln("    <td class='detail'>&nbsp;</td>");
		lineWriter.writeln("    <td class='detail'>&nbsp;</td>");
		lineWriter.writeln("    <td class='detail'>&nbsp;</td>");
		lineWriter.writeln("    <td class='detail'>&nbsp;</td>");
		if (this.displayNumRows)
		{
			lineWriter.writeln("    <td class='detail'>&nbsp;</td>");
		}
		lineWriter.writeln("    <td class='comment detail'>&nbsp;</td>");
		lineWriter.writeln("  </tr>");
		final String str = (n == 1) ? " Table" : " Tables";
		lineWriter.writeln("  <tr class='tbl'>");
		lineWriter.writeln("    <td class='detail'><b>" + this.integerFormatter.format(n) + str + "</b></td>");
		lineWriter.writeln("    <td class='detail'>&nbsp;</td>");
		lineWriter.writeln("    <td class='detail'>&nbsp;</td>");
		lineWriter.writeln("    <td class='detail' align='right'><b>" + this.integerFormatter.format(n2) + "</b></td>");
		if (this.displayNumRows)
		{
			lineWriter.writeln(
					"    <td class='detail' align='right'><b>" + this.integerFormatter.format(n5) + "</b></td>"
			);
		}
		lineWriter.writeln("    <td class='comment detail'>&nbsp;</td>");
		lineWriter.writeln("  </tr>");
		final String str2 = (n3 == 1) ? " View" : " Views";
		lineWriter.writeln("  <tr class='view'>");
		lineWriter.writeln("    <td class='detail'><b>" + this.integerFormatter.format(n3) + str2 + "</b></td>");
		lineWriter.writeln("    <td class='detail'>&nbsp;</td>");
		lineWriter.writeln("    <td class='detail'>&nbsp;</td>");
		lineWriter.writeln("    <td class='detail' align='right'><b>" + this.integerFormatter.format(n4) + "</b></td>");
		if (this.displayNumRows)
		{
			lineWriter.writeln("    <td class='detail'>&nbsp;</td>");
		}
		lineWriter.writeln("    <td class='comment detail'>&nbsp;</td>");
		lineWriter.writeln("  </tr>");
		lineWriter.writeln("</table>");
		super.writeFooter(lineWriter);
	}

	@Override
	protected boolean isMainIndex()
	{
		return true;
	}

	static
	{
		HtmlMainIndexPage.instance = new HtmlMainIndexPage();
	}
}
