// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.util.LineWriter;

public class HtmlMultipleSchemasIndexPage extends HtmlFormatter
{
	private static HtmlMultipleSchemasIndexPage instance;

	private HtmlMultipleSchemasIndexPage()
	{
	}

	public static HtmlMultipleSchemasIndexPage getInstance()
	{
		return HtmlMultipleSchemasIndexPage.instance;
	}

	public void write(
			final String s, final List<String> list, final DatabaseMetaData databaseMetaData,
			final LineWriter lineWriter
	) throws IOException
	{
		this.writeHeader(s, databaseMetaData, list.size(), false, list.get(0).toString(), lineWriter);
		final Iterator<String> iterator = list.iterator();
		while (iterator.hasNext())
		{
			this.writeLineItem(iterator.next(), lineWriter);
		}
		this.writeFooter(lineWriter);
	}

	private void writeHeader(
			final String s, final DatabaseMetaData databaseMetaData, final int i, final boolean b, final String str,
			final LineWriter lineWriter
	) throws IOException
	{
		final String format = new SimpleDateFormat("EEE MMM dd HH:mm z yyyy").format(new Date());
		lineWriter.writeln(
				"<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>"
		);
		lineWriter.writeln("<html>");
		lineWriter.writeln("<head>");
		lineWriter.write("  <title>SchemaSpy Analysis");
		if (s != null)
		{
			lineWriter.write(" of Database ");
			lineWriter.write(s);
		}
		lineWriter.writeln("</title>");
		lineWriter.write("  <link rel=stylesheet href='");
		lineWriter.write(str);
		lineWriter.writeln("/schemaSpy.css' type='text/css'>");
		lineWriter.writeln(
				"  <meta HTTP-EQUIV='Content-Type' CONTENT='text/html; charset=" + Config.getInstance().getCharset()
						+ "'>"
		);
		lineWriter.writeln("</head>");
		lineWriter.writeln("<body>");
		this.writeTableOfContents(lineWriter);
		lineWriter.writeln("<div class='content' style='clear:both;'>");
		lineWriter.writeln("<table width='100%' border='0' cellpadding='0'>");
		lineWriter.writeln(" <tr>");
		lineWriter.write("  <td class='heading' valign='top'><h1>");
		lineWriter.write("SchemaSpy Analysis");
		if (s != null)
		{
			lineWriter.write(" of Database ");
			lineWriter.write(s);
		}
		lineWriter.writeln("</h1></td>");
		lineWriter.writeln(
				"  <td class='heading' align='right' valign='top' title='John Currier - Creator of Cool Tools'><span class='indent'>Generated by</span><br><span class='indent'><span class='signature'><a href='http://schemaspy.sourceforge.net' target='_blank'>SchemaSpy</a></span></span></td>"
		);
		lineWriter.writeln(" </tr>");
		lineWriter.writeln("</table>");
		lineWriter.writeln("<table width='100%'>");
		lineWriter.writeln(" <tr><td class='container'>");
		this.writeGeneratedBy(format, lineWriter);
		lineWriter.writeln(" </td></tr>");
		lineWriter.writeln(" <tr>");
		lineWriter.write("  <td class='container'>");
		if (databaseMetaData != null)
		{
			lineWriter.write("Database Type: ");
			lineWriter.write(this.getDatabaseProduct(databaseMetaData));
		}
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
		lineWriter.writeln("</table>");
		lineWriter.writeln("<div class='indent'>");
		lineWriter.write("<b>");
		lineWriter.write(String.valueOf(i));
		if (s != null)
		{
			lineWriter.write(" Schema");
		} else
		{
			lineWriter.write(" Database");
		}
		lineWriter.write((i == 1) ? "" : "s");
		lineWriter.writeln(":</b>");
		lineWriter.writeln("<TABLE class='dataTable' border='1' rules='groups'>");
		lineWriter.writeln("<colgroup>");
		lineWriter.writeln("<thead align='left'>");
		lineWriter.writeln("<tr>");
		lineWriter.write("  <th valign='bottom'>");
		if (s != null)
		{
			lineWriter.write("Schema");
		} else
		{
			lineWriter.write("Database");
		}
		lineWriter.writeln("</th>");
		if (b)
		{
			lineWriter.writeln("  <th align='center' valign='bottom'>ID</th>");
		}
		lineWriter.writeln("</tr>");
		lineWriter.writeln("</thead>");
		lineWriter.writeln("<tbody>");
	}

	private void writeLineItem(final String s, final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln(" <tr>");
		lineWriter.write("  <td class='detail'><a href='");
		lineWriter.write(s);
		lineWriter.write("/index.html'>");
		lineWriter.write(s);
		lineWriter.writeln("</a></td>");
		lineWriter.writeln(" </tr>");
	}

	protected void writeTableOfContents(final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("<table id='headerHolder' cellspacing='0' cellpadding='0'><tr><td>");
		lineWriter.writeln("<div id='header'>");
		lineWriter.writeln(" <ul>");
		lineWriter.writeln(
				"  <li id='current'><a href='index.html' title='All user schemas in the database'>Schemas</a></li>"
		);
		lineWriter.writeln(
				"  <li><a href='http://sourceforge.net/donate/index.php?group_id=137197' title='Please help keep SchemaSpy alive' target='_blank'>Donate</a></li>"
		);
		lineWriter.writeln(" </ul>");
		lineWriter.writeln("</div>");
		lineWriter.writeln("</td></tr></table>");
	}

	private String getDatabaseProduct(final DatabaseMetaData databaseMetaData)
	{
		try
		{
			return databaseMetaData.getDatabaseProductName() + " - " + databaseMetaData.getDatabaseProductVersion();
		}
		catch (SQLException ex)
		{
			return "";
		}
	}

	static
	{
		HtmlMultipleSchemasIndexPage.instance = new HtmlMultipleSchemasIndexPage();
	}
}
