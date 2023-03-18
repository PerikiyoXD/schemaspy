// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.model.TableIndex;
import net.sourceforge.schemaspy.util.LineWriter;

public class HtmlColumnsPage extends HtmlFormatter
{
	private static HtmlColumnsPage instance;

	private HtmlColumnsPage()
	{
	}

	public static HtmlColumnsPage getInstance()
	{
		return HtmlColumnsPage.instance;
	}

	public List<ColumnInfo> getColumnInfos()
	{
		final ArrayList<ColumnInfo> list = new ArrayList<ColumnInfo>();
		list.add(new ColumnInfo("Table", new ByTableComparator()));
		list.add(new ColumnInfo("Column", new ByColumnComparator()));
		list.add(new ColumnInfo("Type", new ByTypeComparator()));
		list.add(new ColumnInfo("Size", new BySizeComparator()));
		list.add(new ColumnInfo("Nulls", new ByNullableComparator()));
		list.add(new ColumnInfo("Auto", new ByAutoUpdateComparator()));
		list.add(new ColumnInfo("Default", new ByDefaultValueComparator()));
		return list;
	}

	public void write(
			final Database database, final Collection<Table> collection, final ColumnInfo columnInfo, final boolean b,
			final LineWriter lineWriter
	) throws IOException
	{
		final TreeSet<TableColumn> set = new TreeSet<TableColumn>(columnInfo.getComparator());
		final HashSet<TableColumn> set2 = new HashSet<TableColumn>();
		final HashSet<TableColumn> set3 = new HashSet<TableColumn>();
		for (final Table table : collection)
		{
			set.addAll(table.getColumns());
			set2.addAll(table.getPrimaryColumns());
			final Iterator<TableIndex> iterator2 = table.getIndexes().iterator();
			while (iterator2.hasNext())
			{
				set3.addAll(iterator2.next().getColumns());
			}
		}
		this.writeHeader(database, set.size(), b, columnInfo, lineWriter);
		final HtmlTablePage instance = HtmlTablePage.getInstance();
		for (final TableColumn tableColumn : set)
		{
			instance.writeColumn(
					tableColumn, tableColumn.getTable().getName(), (Set<TableColumn>) set2, set3, true, false,
					lineWriter
			);
		}
		this.writeFooter(lineWriter);
	}

	private void writeHeader(
			final Database database, final int i, final boolean b, final ColumnInfo columnInfo,
			final LineWriter lineWriter
	) throws IOException
	{
		this.writeHeader(database, null, "Columns", b, lineWriter);
		lineWriter.writeln("<table width='100%' border='0'>");
		lineWriter.writeln("<tr><td class='container'>");
		this.writeGeneratedBy(database.getConnectTime(), lineWriter);
		lineWriter.writeln("</td><td class='container' rowspan='2' align='right' valign='top'>");
		this.writeLegend(false, false, lineWriter);
		lineWriter.writeln("</td></tr>");
		lineWriter.writeln("<tr valign='top'><td class='container' align='left' valign='top'>");
		lineWriter.writeln("<p>");
		lineWriter.writeln("<form name='options' action=''>");
		lineWriter.writeln(" <label for='showComments'><input type=checkbox id='showComments'>Comments</label>");
		lineWriter.writeln(" <label for='showLegend'><input type=checkbox checked id='showLegend'>Legend</label>");
		lineWriter.writeln("</form>");
		lineWriter.writeln("</table>");
		lineWriter.writeln("<div class='indent'>");
		lineWriter.write("<b>");
		lineWriter.write(database.getName());
		if (database.getSchema() != null)
		{
			lineWriter.write(46);
			lineWriter.write(database.getSchema());
		}
		lineWriter.write(" contains ");
		lineWriter.write(String.valueOf(i));
		lineWriter.write(" columns</b> - click on heading to sort:");
		final Collection<Table> tables = database.getTables();
		this.writeMainTableHeader(
				tables.size() > 0 && tables.iterator().next().getId() != null, columnInfo, lineWriter
		);
		lineWriter.writeln("<tbody valign='top'>");
	}

	public void writeMainTableHeader(final boolean b, final ColumnInfo columnInfo, final LineWriter lineWriter)
			throws IOException
	{
		final boolean b2 = columnInfo != null;
		lineWriter.writeln("<a name='columns'></a>");
		lineWriter.writeln("<table id='columns' class='dataTable' border='1' rules='groups'>");
		int n = 6;
		if (b && !b2)
		{
			++n;
		}
		if (b2)
		{
			++n;
		} else
		{
			n += 2;
		}
		for (int i = 0; i < n; ++i)
		{
			lineWriter.writeln("<colgroup>");
		}
		lineWriter.writeln("<colgroup class='comment'>");
		lineWriter.writeln("<thead align='left'>");
		lineWriter.writeln("<tr>");
		if (b && !b2)
		{
			lineWriter.writeln(this.getTH(columnInfo, "ID", null, "right"));
		}
		if (b2)
		{
			lineWriter.writeln(this.getTH(columnInfo, "Table", null, null));
		}
		lineWriter.writeln(this.getTH(columnInfo, "Column", null, null));
		lineWriter.writeln(this.getTH(columnInfo, "Type", null, null));
		lineWriter.writeln(this.getTH(columnInfo, "Size", null, null));
		lineWriter.writeln(this.getTH(columnInfo, "Nulls", "Are nulls allowed?", null));
		lineWriter.writeln(this.getTH(columnInfo, "Auto", "Is column automatically updated?", null));
		lineWriter.writeln(this.getTH(columnInfo, "Default", "Default value", null));
		if (!b2)
		{
			lineWriter.write("  <th title='Columns in tables that reference this column'>");
			lineWriter.writeln("<span class='notSortedByColumn'>Children</span></th>");
			lineWriter.write("  <th title='Columns in tables that are referenced by this column'>");
			lineWriter.writeln("<span class='notSortedByColumn'>Parents</span></th>");
		}
		lineWriter
				.writeln("  <th title='Comments' class='comment'><span class='notSortedByColumn'>Comments</span></th>");
		lineWriter.writeln("</tr>");
		lineWriter.writeln("</thead>");
	}

	private String getTH(final ColumnInfo columnInfo, final String s, final String str, final String str2)
	{
		final StringBuilder sb = new StringBuilder("  <th");
		if (str2 != null)
		{
			sb.append(" align='");
			sb.append(str2);
			sb.append("'");
		}
		if (str != null)
		{
			sb.append(" title='");
			sb.append(str);
			sb.append("'");
		}
		if (columnInfo != null)
		{
			if (columnInfo.getColumnName().equals(s))
			{
				sb.append(" class='sortedByColumn'>");
				sb.append(s);
			} else
			{
				sb.append(" class='notSortedByColumn'>");
				sb.append("<a href='");
				sb.append(columnInfo.getLocation(s));
				sb.append("#columns'><span class='notSortedByColumn'>");
				sb.append(s);
				sb.append("</span></a>");
			}
		} else
		{
			sb.append('>');
			sb.append(s);
		}
		sb.append("</th>");
		return sb.toString();
	}

	@Override
	protected void writeFooter(final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("</table>");
		lineWriter.writeln("</div>");
		super.writeFooter(lineWriter);
	}

	@Override
	protected boolean isColumnsPage()
	{
		return true;
	}

	static
	{
		HtmlColumnsPage.instance = new HtmlColumnsPage();
	}

	public class ColumnInfo
	{
		private final String columnName;
		private final Comparator<TableColumn> comparator;

		private ColumnInfo(final String columnName, final Comparator<TableColumn> comparator)
		{
			this.columnName = columnName;
			this.comparator = comparator;
		}

		public String getColumnName()
		{
			return this.columnName;
		}

		public String getLocation()
		{
			return this.getLocation(this.columnName);
		}

		public String getLocation(final String str)
		{
			return "columns.by" + str + ".html";
		}

		private Comparator<TableColumn> getComparator()
		{
			return this.comparator;
		}

		@Override
		public String toString()
		{
			return this.getLocation();
		}
	}

	private class ByColumnComparator implements Comparator<TableColumn>
	{
		public int compare(final TableColumn tableColumn, final TableColumn tableColumn2)
		{
			int n = tableColumn.getName().compareToIgnoreCase(tableColumn2.getName());
			if (n == 0)
			{
				n = tableColumn.getTable().compareTo(tableColumn2.getTable());
			}
			return n;
		}
	}

	private class ByTableComparator implements Comparator<TableColumn>
	{
		public int compare(final TableColumn tableColumn, final TableColumn tableColumn2)
		{
			int n = tableColumn.getTable().compareTo(tableColumn2.getTable());
			if (n == 0)
			{
				n = tableColumn.getName().compareToIgnoreCase(tableColumn2.getName());
			}
			return n;
		}
	}

	private class ByTypeComparator implements Comparator<TableColumn>
	{
		private final Comparator<TableColumn> bySize;

		private ByTypeComparator()
		{
			this.bySize = new BySizeComparator();
		}

		public int compare(final TableColumn tableColumn, final TableColumn tableColumn2)
		{
			int n = tableColumn.getType().compareToIgnoreCase(tableColumn2.getType());
			if (n == 0)
			{
				n = this.bySize.compare(tableColumn, tableColumn2);
			}
			return n;
		}
	}

	private class BySizeComparator implements Comparator<TableColumn>
	{
		private final Comparator<TableColumn> byColumn;

		private BySizeComparator()
		{
			this.byColumn = new ByColumnComparator();
		}

		public int compare(final TableColumn tableColumn, final TableColumn tableColumn2)
		{
			int compare = tableColumn.getLength() - tableColumn2.getLength();
			if (compare == 0)
			{
				compare = tableColumn.getDecimalDigits() - tableColumn2.getDecimalDigits();
				if (compare == 0)
				{
					compare = this.byColumn.compare(tableColumn, tableColumn2);
				}
			}
			return compare;
		}
	}

	private class ByNullableComparator implements Comparator<TableColumn>
	{
		private final Comparator<TableColumn> byColumn;

		private ByNullableComparator()
		{
			this.byColumn = new ByColumnComparator();
		}

		public int compare(final TableColumn tableColumn, final TableColumn tableColumn2)
		{
			int compare = (tableColumn.isNullable() == tableColumn2.isNullable()) ? 0
					: (tableColumn.isNullable() ? -1 : 1);
			if (compare == 0)
			{
				compare = this.byColumn.compare(tableColumn, tableColumn2);
			}
			return compare;
		}
	}

	private class ByAutoUpdateComparator implements Comparator<TableColumn>
	{
		private final Comparator<TableColumn> byColumn;

		private ByAutoUpdateComparator()
		{
			this.byColumn = new ByColumnComparator();
		}

		public int compare(final TableColumn tableColumn, final TableColumn tableColumn2)
		{
			int compare = (tableColumn.isAutoUpdated() == tableColumn2.isAutoUpdated()) ? 0
					: (tableColumn.isAutoUpdated() ? -1 : 1);
			if (compare == 0)
			{
				compare = this.byColumn.compare(tableColumn, tableColumn2);
			}
			return compare;
		}
	}

	private class ByDefaultValueComparator implements Comparator<TableColumn>
	{
		private final Comparator<TableColumn> byColumn;

		private ByDefaultValueComparator()
		{
			this.byColumn = new ByColumnComparator();
		}

		public int compare(final TableColumn tableColumn, final TableColumn tableColumn2)
		{
			int n = String.valueOf(tableColumn.getDefaultValue())
					.compareToIgnoreCase(String.valueOf(tableColumn2.getDefaultValue()));
			if (n == 0)
			{
				n = this.byColumn.compare(tableColumn, tableColumn2);
			}
			return n;
		}
	}
}
