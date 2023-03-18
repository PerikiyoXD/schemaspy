// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.util.Dot;
import net.sourceforge.schemaspy.util.LineWriter;

public class HtmlRelationshipsPage extends HtmlDiagramFormatter
{
	private static final HtmlRelationshipsPage instance;
	private static final boolean fineEnabled;

	private HtmlRelationshipsPage()
	{
	}

	public static HtmlRelationshipsPage getInstance()
	{
		return HtmlRelationshipsPage.instance;
	}

	public boolean write(
			final Database database, final File file, final String s, final boolean b, final boolean b2,
			final boolean b3, final Set<TableColumn> set, final LineWriter lineWriter
	)
	{
		final File file2 = new File(file, s + ".real.compact.dot");
		final File file3 = new File(file, s + ".real.compact.svg");
		final File file4 = new File(file, s + ".real.large.dot");
		final File file5 = new File(file, s + ".real.large.svg");
		final File file6 = new File(file, s + ".implied.compact.dot");
		final File file7 = new File(file, s + ".implied.compact.svg");
		final File file8 = new File(file, s + ".implied.large.dot");
		final File file9 = new File(file, s + ".implied.large.svg");
		try
		{
			final Dot dot = this.getDot();
			if (dot == null)
			{
				this.writeHeader(database, null, "All Relationships", b, lineWriter);
				lineWriter.writeln("<div class='content'>");
				this.writeInvalidGraphvizInstallation(lineWriter);
				lineWriter.writeln("</div>");
				this.writeFooter(lineWriter);
				return false;
			}
			this.writeHeader(database, "All Relationships", b, b2, b3, lineWriter);
			lineWriter.writeln("<table width=\"100%\"><tr><td class=\"container\">");
			if (b2)
			{
				if (!HtmlRelationshipsPage.fineEnabled)
				{
					System.out.print(".");
				}
				lineWriter.writeln(dot.generateDiagram(file2, file3));
				lineWriter.writeln(
						"  <a name='diagram'><img id='realCompactImg' src='diagrams/summary/" + file3.getName()
								+ "' usemap='#compactRelationshipsDiagram' class='diagram' border='0' alt=''></a>"
				);
				try
				{
					if (!HtmlRelationshipsPage.fineEnabled)
					{
						System.out.print(".");
					}
					lineWriter.writeln(dot.generateDiagram(file4, file5));
					lineWriter.writeln(
							"  <a name='diagram'><img id='realLargeImg' src='diagrams/summary/" + file5.getName()
									+ "' usemap='#largeRelationshipsDiagram' class='diagram' border='0' alt=''></a>"
					);
				}
				catch (Dot.DotFailure x)
				{
					System.err.println("dot failed to generate all of the relationships diagrams:");
					System.err.println(x);
					System.err.println("...but the relationships page may still be usable.");
				}
			}
			try
			{
				if (b3)
				{
					if (!HtmlRelationshipsPage.fineEnabled)
					{
						System.out.print(".");
					}
					lineWriter.writeln(dot.generateDiagram(file6, file7));
					lineWriter.writeln(
							"  <a name='diagram'><img id='impliedCompactImg' src='diagrams/summary/" + file7.getName()
									+ "' usemap='#compactImpliedRelationshipsDiagram' class='diagram' border='0' alt=''></a>"
					);
					if (!HtmlRelationshipsPage.fineEnabled)
					{
						System.out.print(".");
					}
					lineWriter.writeln(dot.generateDiagram(file8, file9));
					lineWriter.writeln(
							"  <a name='diagram'><img id='impliedLargeImg' src='diagrams/summary/" + file9.getName()
									+ "' usemap='#largeImpliedRelationshipsDiagram' class='diagram' border='0' alt=''></a>"
					);
				}
			}
			catch (Dot.DotFailure x2)
			{
				System.err.println("dot failed to generate all of the relationships diagrams:");
				System.err.println(x2);
				System.err.println("...but the relationships page may still be usable.");
			}
			if (!HtmlRelationshipsPage.fineEnabled)
			{
				System.out.print(".");
			}
			lineWriter.writeln("</td></tr></table>");
			this.writeExcludedColumns(set, null, lineWriter);
			this.writeFooter(lineWriter);
			return true;
		}
		catch (Dot.DotFailure x3)
		{
			System.err.println(x3);
			return false;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	private void writeHeader(
			final Database database, final String s, final boolean b, final boolean b2, final boolean b3,
			final LineWriter lineWriter
	) throws IOException
	{
		this.writeHeader(database, null, s, b, lineWriter);
		lineWriter.writeln("<table class='container' width='100%'>");
		lineWriter.writeln("<tr><td class='container'>");
		this.writeGeneratedBy(database.getConnectTime(), lineWriter);
		lineWriter.writeln("</td>");
		lineWriter.writeln("<td class='container' align='right' valign='top' rowspan='2'>");
		this.writeLegend(false, lineWriter);
		lineWriter.writeln("</td></tr>");
		if (!b2)
		{
			lineWriter.writeln("<tr><td class='container' align='left' valign='top'>");
			if (b3)
			{
				lineWriter.writeln("No 'real' Foreign Key relationships were detected in the schema.<br>");
				lineWriter.writeln(
						"Displayed relationships are implied by a column's name/type/size matching another table's primary key.<p>"
				);
			} else
			{
				lineWriter.writeln("No relationships were detected in the schema.");
			}
			lineWriter.writeln("</td></tr>");
		}
		lineWriter.writeln("<tr><td class='container' align='left' valign='top'>");
		lineWriter.writeln("<form name='options' action=''>");
		if (b3)
		{
			lineWriter.write("  <span ");
			if (!b2)
			{
				lineWriter.write("style=\"display:none\" ");
			}
			lineWriter.writeln(
					"title=\"Show relationships implied by column name/type/size matching another table's primary key\">"
			);
			lineWriter.write(
					"    <label for='implied'><input type='checkbox' id='implied'" + (b2 ? "" : " checked") + '>'
			);
			lineWriter.writeln("Implied relationships</label>");
			lineWriter.writeln("  </span>");
		}
		if (b2 || b3)
		{
			lineWriter.writeln(
					"  <span title=\"By default only columns that are primary keys, foreign keys or indexes are shown\">"
			);
			lineWriter.write("    <label for='showNonKeys'><input type='checkbox' id='showNonKeys'>");
			lineWriter.writeln("All columns</label>");
			lineWriter.writeln("  </span>");
		}
		lineWriter.writeln("</form>");
		lineWriter.writeln("</td></tr></table>");
	}

	@Override
	protected boolean isRelationshipsPage()
	{
		return true;
	}

	static
	{
		instance = new HtmlRelationshipsPage();
		fineEnabled = Logger.getLogger(HtmlRelationshipsPage.class.getName()).isLoggable(Level.FINE);
	}
}
