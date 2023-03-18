// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;

public class WriteStats
{
	private int numTables;
	private int numViews;
	private final Set<TableColumn> excludedColumns;

	public WriteStats(final Collection<Table> collection)
	{
		this.excludedColumns = new HashSet<TableColumn>();
		final Iterator<Table> iterator = collection.iterator();
		while (iterator.hasNext())
		{
			for (final TableColumn tableColumn : iterator.next().getColumns())
			{
				if (tableColumn.isExcluded())
				{
					this.excludedColumns.add(tableColumn);
				}
			}
		}
	}

	public WriteStats(final WriteStats writeStats)
	{
		this.excludedColumns = writeStats.excludedColumns;
	}

	public void wroteTable(final Table table)
	{
		if (table.isView())
		{
			++this.numViews;
		} else
		{
			++this.numTables;
		}
	}

	public int getNumTablesWritten()
	{
		return this.numTables;
	}

	public int getNumViewsWritten()
	{
		return this.numViews;
	}

	public Set<TableColumn> getExcludedColumns()
	{
		return this.excludedColumns;
	}
}
