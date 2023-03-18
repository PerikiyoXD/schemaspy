// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.model.TableIndex;

public class DotNode
{
	private final Table table;
	private final DotNodeConfig config;
	private final String path;
	private final Set<TableColumn> excludedColumns;
	private final String lineSeparator;
	private final boolean displayNumRows;

	public DotNode(final Table table, final String s)
	{
		this(table, s, new DotNodeConfig(true, true));
	}

	public DotNode(final Table table, final String str, final DotNodeConfig config)
	{
		this.excludedColumns = new HashSet<TableColumn>();
		this.lineSeparator = System.getProperty("line.separator");
		this.displayNumRows = Config.getInstance().isNumRowsEnabled();
		this.table = table;
		this.path = str + (table.isRemote() ? ("../../" + table.getSchema() + "/tables/") : "");
		this.config = config;
	}

	public DotNode(final Table table, final boolean b, final String s)
	{
		this(table, s, b ? new DotNodeConfig(true, false) : new DotNodeConfig());
	}

	public void setShowImplied(final boolean b)
	{
		this.config.showImpliedRelationships = b;
	}

	public Table getTable()
	{
		return this.table;
	}

	public void excludeColumn(final TableColumn tableColumn)
	{
		this.excludedColumns.add(tableColumn);
	}

	@Override
	public String toString()
	{
		final StyleSheet instance = StyleSheet.getInstance();
		final StringBuilder sb = new StringBuilder();
		final String name = this.table.getName();
		final String string = (this.table.isRemote() ? (this.table.getSchema() + ".") : "") + name;
		final String str = this.config.showColumnDetails ? "COLSPAN=\"2\" " : "COLSPAN=\"3\" ";
		sb.append("  \"" + string + "\" [" + this.lineSeparator);
		sb.append("    label=<" + this.lineSeparator);
		sb.append(
				"    <TABLE BORDER=\"" + (this.config.showColumnDetails ? "2" : "0")
						+ "\" CELLBORDER=\"1\" CELLSPACING=\"0\" BGCOLOR=\"" + instance.getTableBackground() + "\">"
						+ this.lineSeparator
		);
		sb.append("      <TR>");
		sb.append(
				"<TD COLSPAN=\"3\" BGCOLOR=\"" + instance.getTableHeadBackground() + "\" ALIGN=\"CENTER\">" + string
						+ "</TD>"
		);
		sb.append("</TR>" + this.lineSeparator);
		boolean b = false;
		if (this.config.showColumns)
		{
			final List<TableColumn> primaryColumns = this.table.getPrimaryColumns();
			final HashSet<TableColumn> set = new HashSet<TableColumn>();
			final Iterator<TableIndex> iterator = this.table.getIndexes().iterator();
			while (iterator.hasNext())
			{
				set.addAll(iterator.next().getColumns());
			}
			set.removeAll(primaryColumns);
			for (final TableColumn tableColumn : this.table.getColumns())
			{
				if (
					this.config.showTrivialColumns || this.config.showColumnDetails || tableColumn.isPrimary()
							|| tableColumn.isForeignKey() || set.contains(tableColumn)
				)
				{
					sb.append("      <TR>");
					sb.append("<TD PORT=\"" + tableColumn.getName() + "\" " + str);
					if (this.excludedColumns.contains(tableColumn))
					{
						sb.append("BGCOLOR=\"" + instance.getExcludedColumnBackgroundColor() + "\" ");
					} else if (primaryColumns.contains(tableColumn))
					{
						sb.append("BGCOLOR=\"" + instance.getPrimaryKeyBackground() + "\" ");
					} else if (set.contains(tableColumn))
					{
						sb.append("BGCOLOR=\"" + instance.getIndexedColumnBackground() + "\" ");
					}
					sb.append("ALIGN=\"LEFT\">");
					sb.append(tableColumn.getName());
					sb.append("</TD>");
					if (this.config.showColumnDetails)
					{
						sb.append("<TD PORT=\"");
						sb.append(tableColumn.getName());
						sb.append(".type\" ALIGN=\"LEFT\">");
						sb.append(tableColumn.getType().toLowerCase());
						sb.append("[");
						sb.append(tableColumn.getDetailedSize());
						sb.append("]</TD>");
					}
					sb.append("</TR>" + this.lineSeparator);
				} else
				{
					b = true;
				}
			}
		}
		if (b || !this.config.showColumns)
		{
			sb.append("      <TR><TD PORT=\"elipses\" COLSPAN=\"3\" ALIGN=\"LEFT\">...</TD></TR>" + this.lineSeparator);
		}
		sb.append("      <TR>");
		sb.append("<TD ALIGN=\"LEFT\" BGCOLOR=\"" + instance.getBodyBackground() + "\">");
		final int i = this.config.showImpliedRelationships ? this.table.getNumParents()
				: this.table.getNumNonImpliedParents();
		if (i > 0 || this.config.showColumnDetails)
		{
			sb.append("&lt; " + i);
		} else
		{
			sb.append("  ");
		}
		sb.append("</TD>");
		sb.append("<TD ALIGN=\"RIGHT\" BGCOLOR=\"" + instance.getBodyBackground() + "\">");
		if (this.table.isView())
		{
			sb.append("view");
		} else
		{
			final int numRows = this.table.getNumRows();
			if (this.displayNumRows && numRows != -1)
			{
				sb.append(NumberFormat.getInstance().format(numRows));
				sb.append(" row");
				if (numRows != 1)
				{
					sb.append('s');
				}
			} else
			{
				sb.append("  ");
			}
		}
		sb.append("</TD>");
		sb.append("<TD ALIGN=\"RIGHT\" BGCOLOR=\"" + instance.getBodyBackground() + "\">");
		final int j = this.config.showImpliedRelationships ? this.table.getNumChildren()
				: this.table.getNumNonImpliedChildren();
		if (j > 0 || this.config.showColumnDetails)
		{
			sb.append(j + " &gt;");
		} else
		{
			sb.append("  ");
		}
		sb.append("</TD></TR>" + this.lineSeparator);
		sb.append("    </TABLE>>" + this.lineSeparator);
		if (!this.table.isRemote() || Config.getInstance().isOneOfMultipleSchemas())
		{
			sb.append("    URL=\"" + this.path + toNCR(name) + ".html\"" + this.lineSeparator);
		}
		sb.append("    tooltip=\"" + toNCR(string) + "\"" + this.lineSeparator);
		sb.append("  ];");
		return sb.toString();
	}

	private static String toNCR(final String s)
	{
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); ++i)
		{
			final char char1 = s.charAt(i);
			if (char1 <= '\u007f')
			{
				sb.append(char1);
			} else
			{
				sb.append("&#");
				sb.append(Integer.parseInt(Integer.toHexString(char1), 16));
				sb.append(";");
			}
		}
		return sb.toString();
	}

	public static class DotNodeConfig
	{
		private final boolean showColumns;
		private boolean showTrivialColumns;
		private final boolean showColumnDetails;
		private boolean showImpliedRelationships;

		public DotNodeConfig()
		{
			final boolean b = false;
			this.showImpliedRelationships = b;
			this.showColumnDetails = b;
			this.showTrivialColumns = b;
			this.showColumns = b;
		}

		public DotNodeConfig(final boolean showTrivialColumns, final boolean showColumnDetails)
		{
			this.showColumns = true;
			this.showTrivialColumns = showTrivialColumns;
			this.showColumnDetails = showColumnDetails;
		}
	}
}
