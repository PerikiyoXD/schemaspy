// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.Revision;
import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.util.Dot;
import net.sourceforge.schemaspy.util.HtmlEncoder;
import net.sourceforge.schemaspy.util.LineWriter;

public class HtmlFormatter
{
	protected final boolean encodeComments;
	protected final boolean displayNumRows;
	private final boolean isMetered;

	protected HtmlFormatter()
	{
		this.encodeComments = Config.getInstance().isEncodeCommentsEnabled();
		this.displayNumRows = Config.getInstance().isNumRowsEnabled();
		this.isMetered = Config.getInstance().isMeterEnabled();
	}

	protected void writeHeader(
			final Database database, final Table obj, final String s, final boolean b, final List<String> list,
			final LineWriter lineWriter
	) throws IOException
	{
		lineWriter.writeln(
				"<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>"
		);
		lineWriter.writeln("<html>");
		lineWriter.writeln("<head>");
		lineWriter.writeln("  <!-- SchemaSpy rev " + new Revision() + " -->");
		lineWriter.write("  <title>SchemaSpy - ");
		lineWriter.write(this.getDescription(database, obj, s, false));
		lineWriter.writeln("</title>");
		lineWriter.write("  <link rel=stylesheet href='");
		if (obj != null)
		{
			lineWriter.write("../");
		}
		lineWriter.writeln("schemaSpy.css' type='text/css'>");
		lineWriter.writeln(
				"  <meta HTTP-EQUIV='Content-Type' CONTENT='text/html; charset=" + Config.getInstance().getCharset()
						+ "'>"
		);
		lineWriter.writeln(
				"  <SCRIPT LANGUAGE='JavaScript' TYPE='text/javascript' SRC='" + ((obj == null) ? "" : "../")
						+ "jquery.js'></SCRIPT>"
		);
		lineWriter.writeln(
				"  <SCRIPT LANGUAGE='JavaScript' TYPE='text/javascript' SRC='" + ((obj == null) ? "" : "../")
						+ "schemaSpy.js'></SCRIPT>"
		);
		if (obj != null)
		{
			lineWriter.writeln("  <SCRIPT LANGUAGE='JavaScript' TYPE='text/javascript'>");
			lineWriter.writeln("    table='" + obj + "';");
			lineWriter.writeln("  </SCRIPT>");
		}
		if (list != null)
		{
			lineWriter.writeln("  <SCRIPT LANGUAGE='JavaScript' TYPE='text/javascript'>");
			final Iterator<String> iterator = list.iterator();
			while (iterator.hasNext())
			{
				lineWriter.writeln("    " + iterator.next());
			}
			lineWriter.writeln("  </SCRIPT>");
		}
		lineWriter.writeln("</head>");
		lineWriter.writeln("<body>");
		this.writeTableOfContents(b, lineWriter);
		lineWriter.writeln("<div class='content' style='clear:both;'>");
		lineWriter.writeln("<table width='100%' border='0' cellpadding='0'>");
		lineWriter.writeln(" <tr>");
		lineWriter.write("  <td class='heading' valign='middle'>");
		lineWriter.write("<span class='header'>");
		if (obj == null)
		{
			lineWriter.write("SchemaSpy Analysis of ");
		}
		lineWriter.write(this.getDescription(database, obj, s, true));
		lineWriter.write("</span>");
		if (obj == null && database.getDescription() != null)
		{
			lineWriter.write("<span class='description'>" + database.getDescription().replace("\\=", "=") + "</span>");
		}
		final String str = (obj == null) ? null : obj.getComments();
		if (str != null)
		{
			lineWriter.write("<div style='padding: 0px 4px;'>");
			if (this.encodeComments)
			{
				for (int i = 0; i < str.length(); ++i)
				{
					lineWriter.write(HtmlEncoder.encodeToken(str.charAt(i)));
				}
			} else
			{
				lineWriter.write(str);
			}
			lineWriter.writeln("</div><p>");
		}
		lineWriter.writeln("</td>");
		lineWriter.writeln(
				"  <td class='heading' align='right' valign='top' title='John Currier - Creator of Cool Tools'><span class='indent'>Generated by</span><br><span class='indent'><span class='signature'><a href='http://schemaspy.sourceforge.net' target='_blank'>SchemaSpy</a></span></span></td>"
		);
		lineWriter.writeln(" </tr>");
		lineWriter.writeln("</table>");
	}

	protected void writeHeader(
			final Database database, final Table table, final String s, final boolean b, final LineWriter lineWriter
	) throws IOException
	{
		this.writeHeader(database, table, s, b, null, lineWriter);
	}

	protected void writeGeneratedBy(final String str, final LineWriter lineWriter) throws IOException
	{
		lineWriter.write("<span class='container'>");
		lineWriter.write(
				"Generated by <span class='signature'><a href='http://schemaspy.sourceforge.net' target='_blank'>SchemaSpy</a></span> on "
		);
		lineWriter.write(str);
		lineWriter.writeln("</span>");
	}

	protected void writeTableOfContents(final boolean b, final LineWriter lineWriter) throws IOException
	{
		final String pathToRoot = this.getPathToRoot();
		lineWriter.writeln("<table id='headerHolder' cellspacing='0' cellpadding='0'><tr><td>");
		lineWriter.writeln("<div id='header'>");
		lineWriter.writeln(" <ul>");
		if (Config.getInstance().isOneOfMultipleSchemas())
		{
			lineWriter.writeln(
					"  <li><a href='" + pathToRoot + "../index.html' title='All Schemas Evaluated'>Schemas</a></li>"
			);
		}
		lineWriter.writeln(
				"  <li" + (this.isMainIndex() ? " id='current'" : "") + "><a href='" + pathToRoot
						+ "index.html' title='All tables and views in the schema'>Tables</a></li>"
		);
		lineWriter.writeln(
				"  <li" + (this.isRelationshipsPage() ? " id='current'" : "") + "><a href='" + pathToRoot
						+ "relationships.html' title='Diagram of table relationships'>Relationships</a></li>"
		);
		if (b)
		{
			lineWriter.writeln(
					"  <li" + (this.isOrphansPage() ? " id='current'" : "") + "><a href='" + pathToRoot
							+ "utilities.html' title='View of tables with neither parents nor children'>Utility&nbsp;Tables</a></li>"
			);
		}
		lineWriter.writeln(
				"  <li" + (this.isConstraintsPage() ? " id='current'" : "") + "><a href='" + pathToRoot
						+ "constraints.html' title='Useful for diagnosing error messages that just give constraint name or number'>Constraints</a></li>"
		);
		lineWriter.writeln(
				"  <li" + (this.isAnomaliesPage() ? " id='current'" : "") + "><a href='" + pathToRoot
						+ "anomalies.html' title=\"Things that might not be quite right\">Anomalies</a></li>"
		);
		lineWriter.writeln(
				"  <li" + (this.isColumnsPage() ? " id='current'" : "") + "><a href='" + pathToRoot
						+ HtmlColumnsPage.getInstance().getColumnInfos().get(0)
						+ "' title=\"All of the columns in the schema\">Columns</a></li>"
		);
		lineWriter.writeln(
				"  <li><a href='http://sourceforge.net/donate/index.php?group_id=137197' title='Please help keep SchemaSpy alive' target='_blank'>Donate</a></li>"
		);
		lineWriter.writeln(" </ul>");
		lineWriter.writeln("</div>");
		lineWriter.writeln("</td></tr></table>");
	}

	protected String getDescription(final Database database, final Table table, final String str, final boolean b)
	{
		final StringBuilder sb = new StringBuilder();
		if (table != null)
		{
			if (table.isView())
			{
				sb.append("View ");
			} else
			{
				sb.append("Table ");
			}
		}
		if (b)
		{
			sb.append("<span title='Database'>");
		}
		sb.append(database.getName());
		if (b)
		{
			sb.append("</span>");
		}
		if (database.getSchema() != null)
		{
			sb.append('.');
			if (b)
			{
				sb.append("<span title='Schema'>");
			}
			sb.append(database.getSchema());
			if (b)
			{
				sb.append("</span>");
			}
		}
		if (table != null)
		{
			sb.append('.');
			if (b)
			{
				sb.append("<span title='Table'>");
			}
			sb.append(table.getName());
			if (b)
			{
				sb.append("</span>");
			}
		}
		if (str != null)
		{
			sb.append(" - ");
			sb.append(str);
		}
		return sb.toString();
	}

	protected boolean sourceForgeLogoEnabled()
	{
		return Config.getInstance().isLogoEnabled();
	}

	protected void writeLegend(final boolean b, final LineWriter lineWriter) throws IOException
	{
		this.writeLegend(b, true, lineWriter);
	}

	protected void writeLegend(final boolean b, final boolean b2, final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln(" <table class='legend' border='0'>");
		lineWriter.writeln("  <tr>");
		lineWriter.writeln("   <td class='dataTable' valign='bottom'>Legend:</td>");
		if (this.sourceForgeLogoEnabled())
		{
			lineWriter.writeln(
					"   <td class='container' align='right' valign='top'><a href='http://sourceforge.net' target='_blank'><img src='http://sourceforge.net/sflogo.php?group_id=137197&amp;type=1' alt='SourceForge.net' border='0' height='31' width='88'></a></td>"
			);
		}
		lineWriter.writeln("  </tr>");
		lineWriter.writeln("  <tr><td class='container' colspan='2'>");
		lineWriter.writeln("   <table class='dataTable' border='1'>");
		lineWriter.writeln("    <tbody>");
		lineWriter.writeln("    <tr><td class='primaryKey'>Primary key columns</td></tr>");
		lineWriter.writeln("    <tr><td class='indexedColumn'>Columns with indexes</td></tr>");
		if (b)
		{
			lineWriter.writeln(
					"    <tr class='impliedRelationship'><td class='detail'><span class='impliedRelationship'>Implied relationships</span></td></tr>"
			);
		}
		if (b2)
		{
			lineWriter.writeln("    <tr><td class='excludedColumn'>Excluded column relationships</td></tr>");
			if (!b)
			{
				lineWriter.writeln(
						"    <tr class='impliedRelationship'><td class='legendDetail'>Dashed lines show implied relationships</td></tr>"
				);
			}
			lineWriter.writeln(
					"    <tr><td class='legendDetail'>&lt; <em>n</em> &gt; number of related tables</td></tr>"
			);
		}
		lineWriter.writeln("   </table>");
		lineWriter.writeln("  </td></tr>");
		lineWriter.writeln(" </table>");
		this.writeFeedMe(lineWriter);
		lineWriter.writeln("&nbsp;");
	}

	protected void writeFeedMe(final LineWriter lineWriter) throws IOException
	{
		if (Config.getInstance().isAdsEnabled())
		{
			final StyleSheet instance = StyleSheet.getInstance();
			lineWriter.writeln("<div style=\"margin-right: 2pt;\">");
			lineWriter.writeln("<script type=\"text/javascript\"><!--");
			lineWriter.writeln("google_ad_client = \"pub-9598353634003340\";");
			lineWriter.writeln("google_ad_channel =\"SchemaSpy-generated\";");
			lineWriter.writeln("google_ad_width = 234;");
			lineWriter.writeln("google_ad_height = 60;");
			lineWriter.writeln("google_ad_format = \"234x60_as\";");
			lineWriter.writeln("google_ad_type = \"text\";");
			lineWriter.writeln("google_color_border = \"" + instance.getTableHeadBackground().substring(1) + "\";");
			lineWriter.writeln("google_color_link = \"" + instance.getLinkColor().substring(1) + "\";");
			lineWriter.writeln("google_color_text = \"000000\";");
			lineWriter.writeln("//-->");
			lineWriter.writeln("</script>");
			lineWriter.writeln("<script type=\"text/javascript\"");
			lineWriter.writeln("src=\"http://pagead2.googlesyndication.com/pagead/show_ads.js\">");
			lineWriter.writeln("</script>");
			lineWriter.writeln("</div>");
		}
	}

	protected void writeExcludedColumns(final Set<TableColumn> set, final Table table, final LineWriter lineWriter)
			throws IOException
	{
		Set<TableColumn> set2;
		if (table == null)
		{
			set2 = set;
		} else
		{
			set2 = new HashSet<TableColumn>();
			for (final TableColumn tableColumn : set)
			{
				if (tableColumn.isAllExcluded() || !tableColumn.getTable().equals(table))
				{
					set2.add(tableColumn);
				}
			}
		}
		if (set2.size() > 0)
		{
			lineWriter.writeln("<span class='excludedRelationship'>");
			lineWriter.writeln("<br>Excluded from diagram's relationships: ");
			for (final TableColumn tableColumn2 : set2)
			{
				if (!tableColumn2.getTable().equals(table))
				{
					lineWriter.write("<a href=\"" + this.getPathToRoot() + "tables/");
					lineWriter.write(tableColumn2.getTable().getName());
					lineWriter.write(".html\">");
					lineWriter.write(tableColumn2.getTable().getName());
					lineWriter.write(".");
					lineWriter.write(tableColumn2.getName());
					lineWriter.writeln("</a>&nbsp;");
				}
			}
			lineWriter.writeln("</span>");
		}
	}

	protected void writeInvalidGraphvizInstallation(final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("<br>SchemaSpy was unable to generate a diagram of table relationships.");
		lineWriter.writeln(
				"<br>SchemaSpy requires Graphviz " + Dot.getInstance().getSupportedVersions().substring(4)
						+ " from <a href='http://www.graphviz.org' target='_blank'>www.graphviz.org</a>."
		);
	}

	protected void writeFooter(final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("</div>");
		if (this.isMetered)
		{
			lineWriter.writeln("<span style='float: right;' title='This link is only on the SchemaSpy sample pages'>");
			lineWriter.writeln("<!-- Site Meter -->");
			lineWriter.writeln(
					"<script type='text/javascript' src='http://s28.sitemeter.com/js/counter.js?site=s28schemaspy'>"
			);
			lineWriter.writeln("</script>");
			lineWriter.writeln("<noscript>");
			lineWriter.writeln("<a href='http://s28.sitemeter.com/stats.asp?site=s28schemaspy' target='_top'>");
			lineWriter.writeln(
					"<img src='http://s28.sitemeter.com/meter.asp?site=s28schemaspy' alt='Site Meter' border='0'/></a>"
			);
			lineWriter.writeln("</noscript>");
			lineWriter.writeln("<!-- Copyright (c)2006 Site Meter -->");
			lineWriter.writeln("</span>");
		}
		lineWriter.writeln("</body>");
		lineWriter.writeln("</html>");
	}

	protected String getPathToRoot()
	{
		return "";
	}

	protected boolean isMainIndex()
	{
		return false;
	}

	protected boolean isRelationshipsPage()
	{
		return false;
	}

	protected boolean isOrphansPage()
	{
		return false;
	}

	protected boolean isConstraintsPage()
	{
		return false;
	}

	protected boolean isAnomaliesPage()
	{
		return false;
	}

	protected boolean isColumnsPage()
	{
		return false;
	}
}
