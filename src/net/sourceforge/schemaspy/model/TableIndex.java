// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableIndex implements Comparable<TableIndex>
{
	private final String name;
	private final boolean isUnique;
	private Object id;
	private boolean isPrimary;
	private final List<TableColumn> columns;
	private final List<Boolean> columnsAscending;

	public TableIndex(final ResultSet set) throws SQLException
	{
		this.columns = new ArrayList<TableColumn>();
		this.columnsAscending = new ArrayList<Boolean>();
		this.name = set.getString("INDEX_NAME");
		this.isUnique = !set.getBoolean("NON_UNIQUE");
	}

	public void setId(final Object id)
	{
		this.id = id;
	}

	public Object getId()
	{
		return this.id;
	}

	public String getName()
	{
		return this.name;
	}

	void addColumn(final TableColumn tableColumn, final String s)
	{
		if (tableColumn != null)
		{
			this.columns.add(tableColumn);
			this.columnsAscending.add(s == null || s.equals("A"));
		}
	}

	public String getType()
	{
		if (this.isPrimaryKey())
		{
			return "Primary key";
		}
		if (this.isUnique())
		{
			return "Must be unique";
		}
		return "Performance";
	}

	public boolean isPrimaryKey()
	{
		return this.isPrimary;
	}

	public void setIsPrimaryKey(final boolean isPrimary)
	{
		this.isPrimary = isPrimary;
	}

	public boolean isUnique()
	{
		return this.isUnique;
	}

	public String getColumnsAsString()
	{
		final StringBuilder sb = new StringBuilder();
		for (final TableColumn obj : this.columns)
		{
			if (sb.length() > 0)
			{
				sb.append(" + ");
			}
			sb.append(obj);
		}
		return sb.toString();
	}

	public List<TableColumn> getColumns()
	{
		return Collections.unmodifiableList((List<? extends TableColumn>) this.columns);
	}

	public boolean isUniqueNullable()
	{
		if (!this.isUnique())
		{
			return false;
		}
		boolean b = true;
		for (final TableColumn tableColumn : this.getColumns())
		{
			b = (tableColumn != null && tableColumn.isNullable());
			if (!b)
			{
				break;
			}
		}
		return b;
	}

	public boolean isAscending(final TableColumn tableColumn)
	{
		return this.columnsAscending.get(this.columns.indexOf(tableColumn));
	}

	public int compareTo(final TableIndex tableIndex)
	{
		if (this.isPrimaryKey() && !tableIndex.isPrimaryKey())
		{
			return -1;
		}
		if (!this.isPrimaryKey() && tableIndex.isPrimaryKey())
		{
			return 1;
		}
		final Object id = this.getId();
		final Object id2 = tableIndex.getId();
		if (id == null || id2 == null)
		{
			return this.getName().compareToIgnoreCase(tableIndex.getName());
		}
		if (id instanceof Number)
		{
			return ((Number) id).intValue() - ((Number) id2).intValue();
		}
		return id.toString().compareToIgnoreCase(id2.toString());
	}
}
