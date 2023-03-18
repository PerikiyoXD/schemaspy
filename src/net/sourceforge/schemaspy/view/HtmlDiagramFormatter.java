// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import net.sourceforge.schemaspy.util.Dot;

public class HtmlDiagramFormatter extends HtmlFormatter
{
	private static boolean printedNoDotWarning;
	private static boolean printedInvalidVersionWarning;

	protected HtmlDiagramFormatter()
	{
	}

	protected Dot getDot()
	{
		final Dot instance = Dot.getInstance();
		if (!instance.exists())
		{
			if (!HtmlDiagramFormatter.printedNoDotWarning)
			{
				HtmlDiagramFormatter.printedNoDotWarning = true;
				System.err.println();
				System.err.println("Warning: Failed to run dot.");
				System.err.println("   Download " + instance.getSupportedVersions());
				System.err.println("   from www.graphviz.org and make sure that dot is either in your path");
				System.err.println("   or point to where you installed Graphviz with the -gv option.");
				System.err.println("   Generated pages will not contain a diagramtic view of table relationships.");
			}
			return null;
		}
		if (!instance.isValid())
		{
			if (!HtmlDiagramFormatter.printedInvalidVersionWarning)
			{
				HtmlDiagramFormatter.printedInvalidVersionWarning = true;
				System.err.println();
				System.err
						.println("Warning: Invalid version of Graphviz dot detected (" + instance.getVersion() + ").");
				System.err.println(
						"   SchemaSpy requires " + instance.getSupportedVersions() + ". from www.graphviz.org."
				);
				System.err.println("   Generated pages will not contain a diagramatic view of table relationships.");
			}
			return null;
		}
		return instance;
	}

	static
	{
		HtmlDiagramFormatter.printedNoDotWarning = false;
		HtmlDiagramFormatter.printedInvalidVersionWarning = false;
	}
}
