// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.File;
import java.io.IOException;

import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.util.Dot;
import net.sourceforge.schemaspy.util.LineWriter;

public class HtmlTableDiagrammer extends HtmlDiagramFormatter
{
	private static HtmlTableDiagrammer instance;

	private HtmlTableDiagrammer()
	{
	}

	public static HtmlTableDiagrammer getInstance()
	{
		return HtmlTableDiagrammer.instance;
	}

	public boolean write(final Table table, final File file, final LineWriter lineWriter)
	{
		final File file2 = new File(file, table.getName() + ".1degree.dot");
		final File file3 = new File(file, table.getName() + ".1degree.svg");
		final File file4 = new File(file, table.getName() + ".2degrees.dot");
		final File file5 = new File(file, table.getName() + ".2degrees.svg");
		final File file6 = new File(file, table.getName() + ".implied2degrees.dot");
		final File file7 = new File(file, table.getName() + ".implied2degrees.svg");
		try
		{
			final Dot dot = this.getDot();
			if (dot == null)
			{
				return false;
			}
			final String generateDiagram = dot.generateDiagram(file2, file3);
			lineWriter.write("<br><form action='get'><b>Close relationships");
			if (file4.exists())
			{
				lineWriter.writeln(
						"</b><span class='degrees' id='degrees' title='Detail diminishes with increased separation from "
								+ table.getName() + "'>"
				);
				lineWriter.write(
						"&nbsp;within <label for='oneDegree'><input type='radio' name='degrees' id='oneDegree' checked>one</label>"
				);
				lineWriter.write(
						"  <label for='twoDegrees'><input type='radio' name='degrees' id='twoDegrees'>two degrees</label> of separation"
				);
				lineWriter.write("</span><b>:</b>");
				lineWriter.writeln("</form>");
			} else
			{
				lineWriter.write(":</b></form>");
			}
			lineWriter.write(generateDiagram);
			lineWriter.writeln(
					"  <a name='diagram'><img id='oneDegreeImg' src='../diagrams/" + file3.getName()
							+ "' usemap='#oneDegreeRelationshipsDiagram' class='diagram' border='0' alt='' align='left'></a>"
			);
			if (file6.exists())
			{
				lineWriter.writeln(dot.generateDiagram(file6, file7));
				lineWriter.writeln(
						"  <a name='diagram'><img id='impliedTwoDegreesImg' src='../diagrams/" + file7.getName()
								+ "' usemap='#impliedTwoDegreesRelationshipsDiagram' class='diagram' border='0' alt='' align='left'></a>"
				);
			} else
			{
				file6.delete();
				file7.delete();
			}
			if (file4.exists())
			{
				lineWriter.writeln(dot.generateDiagram(file4, file5));
				lineWriter.writeln(
						"  <a name='diagram'><img id='twoDegreesImg' src='../diagrams/" + file5.getName()
								+ "' usemap='#twoDegreesRelationshipsDiagram' class='diagram' border='0' alt='' align='left'></a>"
				);
			} else
			{
				file4.delete();
				file5.delete();
			}
		}
		catch (Dot.DotFailure x)
		{
			System.err.println(x);
			return false;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	static
	{
		HtmlTableDiagrammer.instance = new HtmlTableDiagrammer();
	}
}
