// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class DirectoryCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
{
	private static final long serialVersionUID = 1L;
	private final DbConfigTableModel model;
	private final JTextField dirField;
	private final JPanel editor;
	private File selectedDir;
	private int selectedRow;
	private int selectedColumn;

	public DirectoryCellEditor(final DbConfigTableModel model, final File currentDirectory)
	{
		this.model = model;
		this.dirField = new JTextField();
		this.dirField.getDocument().addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(final DocumentEvent documentEvent)
			{
				model.setValueAt(
						new File(DirectoryCellEditor.this.dirField.getText()), DirectoryCellEditor.this.selectedRow,
						DirectoryCellEditor.this.selectedColumn
				);
			}

			public void removeUpdate(final DocumentEvent documentEvent)
			{
			}

			public void changedUpdate(final DocumentEvent documentEvent)
			{
				model.setValueAt(
						new File(DirectoryCellEditor.this.dirField.getText()), DirectoryCellEditor.this.selectedRow,
						DirectoryCellEditor.this.selectedColumn
				);
			}
		});
		this.dirField.setBorder(null);
		final JFileChooser fileChooser = new JFileChooser(currentDirectory);
		fileChooser.setFileSelectionMode(1);
		final JButton comp = new JButton("...");
		comp.setPreferredSize(new Dimension(12, 12));
		comp.setMinimumSize(comp.getPreferredSize());
		comp.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent actionEvent)
			{
				fileChooser.setCurrentDirectory(new File(DirectoryCellEditor.this.dirField.getText()));
				if (fileChooser.showOpenDialog((Component) actionEvent.getSource()) == 0)
				{
					DirectoryCellEditor.this.dirField.setText(fileChooser.getSelectedFile().getPath());
				}
			}
		});
		(this.editor = new JPanel()).setLayout(new GridBagLayout());
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1.0;
		constraints.fill = 2;
		this.editor.add(this.dirField, constraints);
		final GridBagConstraints constraints2 = new GridBagConstraints();
		constraints2.insets = new Insets(0, 0, 0, 1);
		this.editor.add(comp, constraints2);
	}

	public Component getTableCellEditorComponent(
			final JTable table, final Object o, final boolean b, final int selectedRow, final int selectedColumn
	)
	{
		this.selectedRow = selectedRow;
		this.selectedColumn = selectedColumn;
		this.selectedDir = (File) o;
		this.dirField.setText((this.selectedDir == null) ? null : this.selectedDir.toString());
		this.editor.setToolTipText(this.model.getDescription(selectedRow));
		return this.editor;
	}

	public Component getTableCellRendererComponent(
			final JTable table, final Object o, final boolean b, final boolean b2, final int n, final int n2
	)
	{
		return this.getTableCellEditorComponent(table, o, b, n, n2);
	}

	public Object getCellEditorValue()
	{
		return this.model.getValueAt(this.selectedRow, this.selectedColumn);
	}
}
