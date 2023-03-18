// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.util.Dot;

public class DotConnector implements Comparable<DotConnector>
{
	private final TableColumn parentColumn;
	private final Table parentTable;
	private final TableColumn childColumn;
	private final Table childTable;
	private final boolean implied;
	private final boolean bottomJustify;
	private String parentPort;
	private String childPort;

	public DotConnector(final TableColumn parentColumn, final TableColumn childColumn, final boolean implied)
	{
		this.parentColumn = parentColumn;
		this.childColumn = childColumn;
		this.implied = implied;
		this.parentPort = parentColumn.getName();
		this.parentTable = parentColumn.getTable();
		this.childPort = childColumn.getName();
		this.childTable = childColumn.getTable();
		this.bottomJustify = !Dot.getInstance().supportsCenteredEastWestEdges();
	}

	public boolean pointsTo(final Table table)
	{
		return table.equals(this.parentTable);
	}

	public boolean isImplied()
	{
		return this.implied;
	}

	public void connectToParentDetails()
	{
		this.parentPort = this.parentColumn.getName() + ".type";
	}

	public void connectToParentTitle()
	{
		this.parentPort = "elipses";
	}

	public void connectToChildTitle()
	{
		this.childPort = "elipses";
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("  \"");
		if (this.childTable.isRemote())
		{
			sb.append(this.childTable.getSchema());
			sb.append('.');
		}
		sb.append(this.childTable.getName());
		sb.append("\":\"");
		sb.append(this.childPort);
		sb.append("\":");
		if (this.bottomJustify)
		{
			sb.append("s");
		}
		sb.append("w -> \"");
		if (this.parentTable.isRemote())
		{
			sb.append(this.parentTable.getSchema());
			sb.append('.');
		}
		sb.append(this.parentTable.getName());
		sb.append("\":\"");
		sb.append(this.parentPort);
		sb.append("\":");
		if (this.bottomJustify)
		{
			sb.append("s");
		}
		sb.append("e ");
		sb.append("[arrowhead=none");
		sb.append(" dir=back");
		sb.append(" arrowtail=");
		if (this.childColumn.isUnique())
		{
			sb.append("teeodot");
		} else
		{
			sb.append("crowodot");
		}
		if (this.implied)
		{
			sb.append(" style=dashed");
		}
		sb.append("];");
		return sb.toString();
	}

	public int compareTo(final DotConnector dotConnector)
	{
		int n = this.childTable.compareTo(dotConnector.childTable);
		if (n == 0)
		{
			n = this.childColumn.getName().compareToIgnoreCase(dotConnector.childColumn.getName());
		}
		if (n == 0)
		{
			n = this.parentTable.compareTo(dotConnector.parentTable);
		}
		if (n == 0)
		{
			n = this.parentColumn.getName().compareToIgnoreCase(dotConnector.parentColumn.getName());
		}
		if (n == 0 && this.implied != dotConnector.implied)
		{
			n = (this.implied ? 1 : -1);
		}
		return n;
	}

	@Override
	public boolean equals(final Object o)
	{
		return o instanceof DotConnector && this.compareTo((DotConnector) o) == 0;
	}

	@Override
	public int hashCode()
	{
		return ((this.parentTable == null) ? 0 : this.parentTable.getName().hashCode()) << 16
				& ((this.childTable == null) ? 0 : this.childTable.getName().hashCode());
	}

	public TableColumn getParentColumn()
	{
		return this.parentColumn;
	}

	public Table getParentTable()
	{
		return this.parentTable;
	}

	public TableColumn getChildColumn()
	{
		return this.childColumn;
	}

	public Table getChildTable()
	{
		return this.childTable;
	}
}
