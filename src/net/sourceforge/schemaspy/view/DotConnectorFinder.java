// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;

public class DotConnectorFinder
{
	private static DotConnectorFinder instance;

	private DotConnectorFinder()
	{
	}

	public static DotConnectorFinder getInstance()
	{
		return DotConnectorFinder.instance;
	}

	public Set<DotConnector> getRelatedConnectors(final Table table, final boolean b)
	{
		final HashSet<DotConnector> set = new HashSet<DotConnector>();
		final Iterator<TableColumn> iterator = table.getColumns().iterator();
		while (iterator.hasNext())
		{
			set.addAll(this.getRelatedConnectors(iterator.next(), null, false, b));
		}
		return (Set<DotConnector>) set;
	}

	public Set<DotConnector> getRelatedConnectors(
			final Table table, final Table table2, final boolean b, final boolean b2
	)
	{
		final HashSet<DotConnector> set = new HashSet<DotConnector>();
		final Iterator<TableColumn> iterator = table.getColumns().iterator();
		while (iterator.hasNext())
		{
			set.addAll(this.getRelatedConnectors(iterator.next(), table2, b, b2));
		}
		final Iterator<TableColumn> iterator2 = table2.getColumns().iterator();
		while (iterator2.hasNext())
		{
			set.addAll(this.getRelatedConnectors(iterator2.next(), table, b, b2));
		}
		return (Set<DotConnector>) set;
	}

	private Set<DotConnector> getRelatedConnectors(
			final TableColumn tableColumn, final Table table, final boolean b, final boolean b2
	)
	{
		final HashSet<DotConnector> set = new HashSet<DotConnector>();
		if (!b && tableColumn.isExcluded())
		{
			return set;
		}
		for (final TableColumn tableColumn2 : tableColumn.getParents())
		{
			final Table table2 = tableColumn2.getTable();
			if (table != null && table2 != table)
			{
				continue;
			}
			if (table == null && !b && tableColumn2.isExcluded())
			{
				continue;
			}
			final boolean implied = tableColumn.getParentConstraint(tableColumn2).isImplied();
			if (implied && !b2)
			{
				continue;
			}
			set.add(new DotConnector(tableColumn2, tableColumn, implied));
		}
		for (final TableColumn tableColumn3 : tableColumn.getChildren())
		{
			final Table table3 = tableColumn3.getTable();
			if (table != null && table3 != table)
			{
				continue;
			}
			if (table == null && !b && tableColumn3.isExcluded())
			{
				continue;
			}
			final boolean implied2 = tableColumn.getChildConstraint(tableColumn3).isImplied();
			if (implied2 && !b2)
			{
				continue;
			}
			set.add(new DotConnector(tableColumn, tableColumn3, implied2));
		}
		return set;
	}

	static
	{
		DotConnectorFinder.instance = new DotConnectorFinder();
	}
}
