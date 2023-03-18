// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.ui;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import net.sourceforge.schemaspy.util.DbSpecificConfig;

public class DbConfigPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JComboBox<DbTypeSelectorModel> databaseTypeSelector;
	private final DbConfigTableModel model;
	private JTable table;

	public DbConfigPanel()
	{
		this.model = new DbConfigTableModel();
		this.initialize();
	}

	private void initialize()
	{
		this.table = new JTable(this.model)
		{
			private static final long serialVersionUID = 1L;

			{
				this.setDefaultRenderer(Boolean.TYPE, this.getDefaultRenderer(Boolean.class));
				this.setDefaultEditor(Boolean.TYPE, this.getDefaultEditor(Boolean.class));
				this.setDefaultRenderer(Number.class, this.getDefaultRenderer(String.class));
				this.setDefaultEditor(Number.class, this.getDefaultEditor(String.class));
				final DirectoryCellEditor directoryCellEditor = new DirectoryCellEditor(
						DbConfigPanel.this.model, new File("/")
				);
				this.setDefaultRenderer(File.class, directoryCellEditor);
				this.setDefaultEditor(File.class, directoryCellEditor);
			}

			@Override
			public TableCellRenderer getCellRenderer(final int row, final int column)
			{
				TableCellRenderer tableCellRenderer;
				if (column == 0)
				{
					tableCellRenderer = super.getCellRenderer(row, column);
				} else
				{
					tableCellRenderer = this.getDefaultRenderer(DbConfigPanel.this.model.getClass(row));
				}
				if (tableCellRenderer instanceof JComponent)
				{
					((JComponent) tableCellRenderer).setToolTipText(DbConfigPanel.this.model.getDescription(row));
				}
				return tableCellRenderer;
			}

			@Override
			public TableCellEditor getCellEditor(final int n, final int n2)
			{
				return this.getDefaultEditor(DbConfigPanel.this.model.getClass(n));
			}
		};
		this.model.addTableModelListener(new TableModelListener()
		{
			public void tableChanged(final TableModelEvent tableModelEvent)
			{
				final TableColumn column = DbConfigPanel.this.table.getColumnModel().getColumn(0);
				column.setPreferredWidth(UiUtils.getPreferredColumnWidth(DbConfigPanel.this.table, column) + 4);
				column.setMaxWidth(column.getPreferredWidth());
				DbConfigPanel.this.table.sizeColumnsToFit(0);
			}
		});
		this.setLayout(new BorderLayout());
		final JScrollPane comp = new JScrollPane(this.table);
		comp.setViewportBorder(null);
		this.add(comp, "Center");
		this.add(this.getDatabaseTypeSelector(), "North");
	}

	private JComboBox<DbTypeSelectorModel> getDatabaseTypeSelector()
	{
		if (this.databaseTypeSelector == null)
		{
			final DbTypeSelectorModel aModel = new DbTypeSelectorModel("ora");
			(this.databaseTypeSelector = new JComboBox<DbTypeSelectorModel>(aModel)).addItemListener(new ItemListener()
			{
				public void itemStateChanged(final ItemEvent itemEvent)
				{
					if (itemEvent.getStateChange() == 1)
					{
						DbConfigPanel.this.model.setDbSpecificConfig((DbSpecificConfig) itemEvent.getItem());
					}
				}
			});
			final DbSpecificConfig dbSpecificConfig = (DbSpecificConfig) aModel.getSelectedItem();
			if (dbSpecificConfig != null)
			{
				this.model.setDbSpecificConfig(dbSpecificConfig);
			}
		}
		return this.databaseTypeSelector;
	}
}
