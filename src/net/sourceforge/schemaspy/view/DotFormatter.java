// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.Revision;
import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.util.Dot;
import net.sourceforge.schemaspy.util.LineWriter;

public class DotFormatter
{
	private static DotFormatter instance;
	private final int fontSize;

	private DotFormatter()
	{
		this.fontSize = Config.getInstance().getFontSize();
	}

	public static DotFormatter getInstance()
	{
		return DotFormatter.instance;
	}

	public Set<ForeignKeyConstraint> writeRealRelationships(
			final Table table, final boolean b, final WriteStats writeStats, final LineWriter lineWriter
	) throws IOException
	{
		return this.writeRelationships(table, b, writeStats, false, lineWriter);
	}

	public void writeAllRelationships(
			final Table table, final boolean b, final WriteStats writeStats, final LineWriter lineWriter
	) throws IOException
	{
		this.writeRelationships(table, b, writeStats, true, lineWriter);
	}

	private Set<ForeignKeyConstraint> writeRelationships(
			final Table table, final boolean b, final WriteStats writeStats, final boolean b2,
			final LineWriter lineWriter
	) throws IOException
	{
		final HashSet<Table> set = new HashSet<Table>();
		final HashSet<ForeignKeyConstraint> set2 = new HashSet<ForeignKeyConstraint>();
		final DotConnectorFinder instance = DotConnectorFinder.getInstance();
		this.writeHeader(
				b2 ? "impliedTwoDegreesRelationshipsDiagram"
						: (b ? "twoDegreesRelationshipsDiagram" : "oneDegreeRelationshipsDiagram"),
				true, lineWriter
		);
		final Set<Table> immediateRelatives = this.getImmediateRelatives(table, true, b2, set2);
		final TreeSet<DotConnector> set3 = new TreeSet<DotConnector>(instance.getRelatedConnectors(table, b2));
		set.add(table);
		final TreeMap<Table, DotNode> treeMap = new TreeMap<Table, DotNode>();
		for (final Table table2 : immediateRelatives)
		{
			if (!set.add(table2))
			{
				continue;
			}
			treeMap.put(table2, new DotNode(table2, true, ""));
			set3.addAll(instance.getRelatedConnectors(table2, table, true, b2));
		}
		for (final DotConnector dotConnector : set3)
		{
			if (dotConnector.pointsTo(table))
			{
				dotConnector.connectToParentDetails();
			}
		}
		final HashSet<Table> set4 = new HashSet<Table>();
		final TreeSet<DotConnector> set5 = new TreeSet<DotConnector>();
		if (b)
		{
			for (final Table table3 : immediateRelatives)
			{
				final Set<Table> immediateRelatives2 = this.getImmediateRelatives(table3, false, b2, set2);
				for (final Table table4 : immediateRelatives2)
				{
					if (!set.add(table4))
					{
						continue;
					}
					set5.addAll(instance.getRelatedConnectors(table4, table3, false, b2));
					treeMap.put(table4, new DotNode(table4, false, ""));
				}
				set4.addAll(immediateRelatives2);
			}
		}
		final ArrayList<Table> list = new ArrayList<Table>(treeMap.keySet());
		final Iterator<Table> iterator5 = (Iterator<Table>) list.iterator();
		while (iterator5.hasNext())
		{
			final Table table5 = iterator5.next();
			iterator5.remove();
			for (final Table table6 : list)
			{
				for (final DotConnector dotConnector2 : instance.getRelatedConnectors(table5, table6, false, b2))
				{
					if (b && (set4.contains(table5) || set4.contains(table6)))
					{
						set5.add(dotConnector2);
					} else
					{
						set3.add(dotConnector2);
					}
				}
			}
		}
		this.markExcludedColumns((Map<Table, DotNode>) treeMap, writeStats.getExcludedColumns());
		for (final DotConnector dotConnector3 : set5)
		{
			if (
				set4.contains(dotConnector3.getParentTable())
						&& !immediateRelatives.contains(dotConnector3.getParentTable())
			)
			{
				dotConnector3.connectToParentTitle();
			}
			if (
				set4.contains(dotConnector3.getChildTable())
						&& !immediateRelatives.contains(dotConnector3.getChildTable())
			)
			{
				dotConnector3.connectToChildTitle();
			}
		}
		treeMap.put(table, new DotNode(table, ""));
		set3.addAll(set5);
		for (final DotConnector dotConnector4 : set3)
		{
			if (dotConnector4.isImplied())
			{
				final DotNode dotNode = treeMap.get(dotConnector4.getParentTable());
				if (dotNode != null)
				{
					dotNode.setShowImplied(true);
				}
				final DotNode dotNode2 = treeMap.get(dotConnector4.getChildTable());
				if (dotNode2 != null)
				{
					dotNode2.setShowImplied(true);
				}
			}
			lineWriter.writeln(dotConnector4.toString());
		}
		for (final DotNode dotNode3 : treeMap.values())
		{
			lineWriter.writeln(dotNode3.toString());
			writeStats.wroteTable(dotNode3.getTable());
		}
		lineWriter.writeln("}");
		return set2;
	}

	private Set<Table> getImmediateRelatives(
			final Table table, final boolean b, final boolean b2, final Set<ForeignKeyConstraint> set
	)
	{
		final HashSet<TableColumn> set2 = new HashSet<TableColumn>();
		for (final TableColumn tableColumn : table.getColumns())
		{
			if (!tableColumn.isAllExcluded())
			{
				if (!b && tableColumn.isExcluded())
				{
					continue;
				}
				for (final TableColumn tableColumn2 : tableColumn.getChildren())
				{
					if (!tableColumn2.isAllExcluded())
					{
						if (!b && tableColumn2.isExcluded())
						{
							continue;
						}
						final ForeignKeyConstraint childConstraint = tableColumn.getChildConstraint(tableColumn2);
						if (b2 || !childConstraint.isImplied())
						{
							set2.add(tableColumn2);
						} else
						{
							set.add(childConstraint);
						}
					}
				}
				for (final TableColumn tableColumn3 : tableColumn.getParents())
				{
					if (!tableColumn3.isAllExcluded())
					{
						if (!b && tableColumn3.isExcluded())
						{
							continue;
						}
						final ForeignKeyConstraint parentConstraint = tableColumn.getParentConstraint(tableColumn3);
						if (b2 || !parentConstraint.isImplied())
						{
							set2.add(tableColumn3);
						} else
						{
							set.add(parentConstraint);
						}
					}
				}
			}
		}
		final HashSet<Table> set3 = new HashSet<Table>();
		final Iterator<TableColumn> iterator4 = set2.iterator();
		while (iterator4.hasNext())
		{
			set3.add(iterator4.next().getTable());
		}
		set3.remove(table);
		return set3;
	}

	private void writeHeader(final String str, final boolean b, final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln(
				"// dot " + Dot.getInstance().getVersion() + " on " + System.getProperty("os.name") + " "
						+ System.getProperty("os.version")
		);
		lineWriter.writeln("// SchemaSpy rev " + new Revision());
		lineWriter.writeln("digraph \"" + str + "\" {");
		lineWriter.writeln("  graph [");
		final boolean rankDirBugEnabled = Config.getInstance().isRankDirBugEnabled();
		if (!rankDirBugEnabled)
		{
			lineWriter.writeln("    rankdir=\"RL\"");
		}
		lineWriter.writeln("    bgcolor=\"" + StyleSheet.getInstance().getBodyBackground() + "\"");
		if (b)
		{
			if (rankDirBugEnabled)
			{
				lineWriter.writeln("    label=\"\\nLayout is significantly better without '-rankdirbug' option\"");
			} else
			{
				lineWriter.writeln("    label=\"\\nGenerated by SchemaSpy\"");
			}
			lineWriter.writeln("    labeljust=\"l\"");
		}
		lineWriter.writeln("    nodesep=\"0.18\"");
		lineWriter.writeln("    ranksep=\"0.46\"");
		lineWriter.writeln("    fontname=\"" + Config.getInstance().getFont() + "\"");
		lineWriter.writeln("    fontsize=\"" + this.fontSize + "\"");
		lineWriter.writeln("  ];");
		lineWriter.writeln("  node [");
		lineWriter.writeln("    fontname=\"" + Config.getInstance().getFont() + "\"");
		lineWriter.writeln("    fontsize=\"" + this.fontSize + "\"");
		lineWriter.writeln("    shape=\"plaintext\"");
		lineWriter.writeln("  ];");
		lineWriter.writeln("  edge [");
		lineWriter.writeln("    arrowsize=\"0.8\"");
		lineWriter.writeln("  ];");
	}

	public void writeRealRelationships(
			final Database database, final Collection<Table> collection, final boolean b, final boolean b2,
			final WriteStats writeStats, final LineWriter lineWriter
	) throws IOException
	{
		this.writeRelationships(database, collection, b, b2, false, writeStats, lineWriter);
	}

	public boolean writeAllRelationships(
			final Database database, final Collection<Table> collection, final boolean b, final boolean b2,
			final WriteStats writeStats, final LineWriter lineWriter
	) throws IOException
	{
		return this.writeRelationships(database, collection, b, b2, true, writeStats, lineWriter);
	}

	private boolean writeRelationships(
			final Database database, final Collection<Table> collection, final boolean b, final boolean b2,
			final boolean b3, final WriteStats writeStats, final LineWriter lineWriter
	) throws IOException
	{
		final DotConnectorFinder instance = DotConnectorFinder.getInstance();
		final DotNode.DotNodeConfig dotNodeConfig = b2 ? new DotNode.DotNodeConfig(!b, false)
				: new DotNode.DotNodeConfig();
		boolean b4 = false;
		String s;
		if (b3)
		{
			if (b)
			{
				s = "compactImpliedRelationshipsDiagram";
			} else
			{
				s = "largeImpliedRelationshipsDiagram";
			}
		} else if (b)
		{
			s = "compactRelationshipsDiagram";
		} else
		{
			s = "largeRelationshipsDiagram";
		}
		this.writeHeader(s, true, lineWriter);
		final TreeMap<Table, DotNode> treeMap = new TreeMap<Table, DotNode>();
		for (final Table table : collection)
		{
			if (!table.isOrphan(b3))
			{
				treeMap.put(table, new DotNode(table, "tables/", dotNodeConfig));
			}
		}
		for (final Table table2 : database.getRemoteTables())
		{
			treeMap.put(table2, new DotNode(table2, "tables/", dotNodeConfig));
		}
		final TreeSet<DotConnector> set = new TreeSet<DotConnector>();
		final Iterator<DotNode> iterator3 = treeMap.values().iterator();
		while (iterator3.hasNext())
		{
			set.addAll(instance.getRelatedConnectors(iterator3.next().getTable(), b3));
		}
		this.markExcludedColumns(treeMap, writeStats.getExcludedColumns());
		for (final DotNode dotNode : treeMap.values())
		{
			final Table table3 = dotNode.getTable();
			lineWriter.writeln(dotNode.toString());
			writeStats.wroteTable(table3);
			b4 = (b4 || (b3 && table3.isOrphan(false)));
		}
		final Iterator<DotConnector> iterator5 = set.iterator();
		while (iterator5.hasNext())
		{
			lineWriter.writeln(iterator5.next().toString());
		}
		lineWriter.writeln("}");
		return b4;
	}

	private void markExcludedColumns(final Map<Table, DotNode> map, final Set<TableColumn> set)
	{
		for (final TableColumn tableColumn : set)
		{
			final DotNode dotNode = map.get(tableColumn.getTable());
			if (dotNode != null)
			{
				dotNode.excludeColumn(tableColumn);
			}
		}
	}

	public void writeOrphan(final Table table, final LineWriter lineWriter) throws IOException
	{
		this.writeHeader(table.getName(), false, lineWriter);
		lineWriter.writeln(new DotNode(table, true, "tables/").toString());
		lineWriter.writeln("}");
	}

	static
	{
		DotFormatter.instance = new DotFormatter();
	}
}
