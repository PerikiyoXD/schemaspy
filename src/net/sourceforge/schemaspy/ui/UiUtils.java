// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.ui;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class UiUtils
{
	public static int getPreferredColumnWidth(final JTable table, final TableColumn tableColumn)
	{
		return Math.max(getPreferredColumnHeaderWidth(table, tableColumn), getWidestCellInColumn(table, tableColumn));
	}

	public static int getPreferredColumnHeaderWidth(final JTable table, final TableColumn tableColumn)
	{
		final TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
		if (headerRenderer == null)
		{
			return 0;
		}
		return headerRenderer.getTableCellRendererComponent(table, tableColumn.getHeaderValue(), false, false, 0, 0)
				.getPreferredSize().width;
	}

	public static int getWidestCellInColumn(final JTable table, final TableColumn tableColumn)
	{
		final int modelIndex = tableColumn.getModelIndex();
		int max = 0;
		for (int i = 0; i < table.getRowCount(); ++i)
		{
			max = Math.max(
					table.getCellRenderer(i, modelIndex)
							.getTableCellRendererComponent(
									table, table.getValueAt(i, modelIndex), false, false, i, modelIndex
							).getPreferredSize().width,
					max
			);
		}
		return max;
	}
}
