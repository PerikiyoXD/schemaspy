// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.model.TableIndex;
import net.sourceforge.schemaspy.util.DOMUtil;

public class XmlTableFormatter
{
	private static final XmlTableFormatter instance;
	private static final Pattern validXmlChars;

	private XmlTableFormatter()
	{
	}

	public static XmlTableFormatter getInstance()
	{
		return XmlTableFormatter.instance;
	}

	public void appendTables(final Element element, final Collection<Table> collection)
	{
		final TreeSet<Table> set = new TreeSet<Table>(new Comparator<Table>()
		{
			public int compare(final Table table, final Table table2)
			{
				return table.getName().compareToIgnoreCase(table2.getName());
			}
		});
		set.addAll(collection);
		final Element element2 = element.getOwnerDocument().createElement("tables");
		element.appendChild(element2);
		final Iterator<Table> iterator = set.iterator();
		while (iterator.hasNext())
		{
			this.appendTable(element2, iterator.next());
		}
	}

	private void appendTable(final Element element, final Table table)
	{
		final Element element2 = element.getOwnerDocument().createElement("table");
		element.appendChild(element2);
		if (table.getId() != null)
		{
			DOMUtil.appendAttribute(element2, "id", String.valueOf(table.getId()));
		}
		if (table.getSchema() != null)
		{
			DOMUtil.appendAttribute(element2, "schema", table.getSchema());
		}
		DOMUtil.appendAttribute(element2, "name", table.getName());
		if (table.getNumRows() != -1)
		{
			DOMUtil.appendAttribute(element2, "numRows", String.valueOf(table.getNumRows()));
		}
		DOMUtil.appendAttribute(element2, "type", table.isView() ? "VIEW" : "TABLE");
		DOMUtil.appendAttribute(element2, "remarks", (table.getComments() == null) ? "" : table.getComments());
		this.appendColumns(element2, table);
		this.appendPrimaryKeys(element2, table);
		this.appendIndexes(element2, table);
		this.appendCheckConstraints(element2, table);
		this.appendView(element2, table);
	}

	private void appendColumns(final Element element, final Table table)
	{
		final Iterator<TableColumn> iterator = table.getColumns().iterator();
		while (iterator.hasNext())
		{
			this.appendColumn(element, iterator.next());
		}
	}

	private Node appendColumn(final Node node, final TableColumn tableColumn)
	{
		final Document ownerDocument = node.getOwnerDocument();
		final Element element = ownerDocument.createElement("column");
		node.appendChild(element);
		DOMUtil.appendAttribute(element, "id", String.valueOf(tableColumn.getId()));
		DOMUtil.appendAttribute(element, "name", tableColumn.getName());
		DOMUtil.appendAttribute(element, "type", tableColumn.getType());
		DOMUtil.appendAttribute(element, "size", String.valueOf(tableColumn.getLength()));
		DOMUtil.appendAttribute(element, "digits", String.valueOf(tableColumn.getDecimalDigits()));
		DOMUtil.appendAttribute(element, "nullable", String.valueOf(tableColumn.isNullable()));
		DOMUtil.appendAttribute(element, "autoUpdated", String.valueOf(tableColumn.isAutoUpdated()));
		if (tableColumn.getDefaultValue() != null)
		{
			String s = tableColumn.getDefaultValue().toString();
			if (isBinary(s))
			{
				s = this.asBinary(s);
				DOMUtil.appendAttribute(element, "defaultValueIsBinary", "true");
			}
			DOMUtil.appendAttribute(element, "defaultValue", s);
		}
		DOMUtil.appendAttribute(
				element, "remarks", (tableColumn.getComments() == null) ? "" : tableColumn.getComments()
		);
		for (final TableColumn tableColumn2 : tableColumn.getChildren())
		{
			final Element element2 = ownerDocument.createElement("child");
			element.appendChild(element2);
			final ForeignKeyConstraint childConstraint = tableColumn.getChildConstraint(tableColumn2);
			DOMUtil.appendAttribute(element2, "foreignKey", childConstraint.getName());
			DOMUtil.appendAttribute(element2, "table", tableColumn2.getTable().getName());
			DOMUtil.appendAttribute(element2, "column", tableColumn2.getName());
			DOMUtil.appendAttribute(element2, "implied", String.valueOf(childConstraint.isImplied()));
			DOMUtil.appendAttribute(element2, "onDeleteCascade", String.valueOf(childConstraint.isCascadeOnDelete()));
		}
		for (final TableColumn tableColumn3 : tableColumn.getParents())
		{
			final Element element3 = ownerDocument.createElement("parent");
			element.appendChild(element3);
			final ForeignKeyConstraint parentConstraint = tableColumn.getParentConstraint(tableColumn3);
			DOMUtil.appendAttribute(element3, "foreignKey", parentConstraint.getName());
			DOMUtil.appendAttribute(element3, "table", tableColumn3.getTable().getName());
			DOMUtil.appendAttribute(element3, "column", tableColumn3.getName());
			DOMUtil.appendAttribute(element3, "implied", String.valueOf(parentConstraint.isImplied()));
			DOMUtil.appendAttribute(element3, "onDeleteCascade", String.valueOf(parentConstraint.isCascadeOnDelete()));
		}
		return element;
	}

	private void appendPrimaryKeys(final Element element, final Table table)
	{
		final Document ownerDocument = element.getOwnerDocument();
		int n = 1;
		for (final TableColumn tableColumn : table.getPrimaryColumns())
		{
			final Element element2 = ownerDocument.createElement("primaryKey");
			element.appendChild(element2);
			DOMUtil.appendAttribute(element2, "column", tableColumn.getName());
			DOMUtil.appendAttribute(element2, "sequenceNumberInPK", String.valueOf(n++));
		}
	}

	private void appendCheckConstraints(final Element element, final Table table)
	{
		final Document ownerDocument = element.getOwnerDocument();
		final Map<String, String> checkConstraints = table.getCheckConstraints();
		if (checkConstraints != null && !checkConstraints.isEmpty())
		{
			for (final String s : checkConstraints.keySet())
			{
				final Element element2 = ownerDocument.createElement("checkConstraint");
				element.appendChild(element2);
				DOMUtil.appendAttribute(element2, "name", s);
				DOMUtil.appendAttribute(element2, "constraint", checkConstraints.get(s).toString());
			}
		}
	}

	private void appendIndexes(final Node node, final Table table)
	{
		final boolean b = table.getId() != null;
		final Set<TableIndex> indexes = table.getIndexes();
		if (indexes != null && !indexes.isEmpty())
		{
			final TreeSet<TableIndex> set = new TreeSet<TableIndex>(indexes);
			final Document ownerDocument = node.getOwnerDocument();
			for (final TableIndex tableIndex : set)
			{
				final Element element = ownerDocument.createElement("index");
				if (b)
				{
					DOMUtil.appendAttribute(element, "id", String.valueOf(tableIndex.getId()));
				}
				DOMUtil.appendAttribute(element, "name", tableIndex.getName());
				DOMUtil.appendAttribute(element, "unique", String.valueOf(tableIndex.isUnique()));
				for (final TableColumn tableColumn : tableIndex.getColumns())
				{
					final Element element2 = ownerDocument.createElement("column");
					DOMUtil.appendAttribute(element2, "name", tableColumn.getName());
					DOMUtil.appendAttribute(element2, "ascending", String.valueOf(tableIndex.isAscending(tableColumn)));
					element.appendChild(element2);
				}
				node.appendChild(element);
			}
		}
	}

	private void appendView(final Element element, final Table table)
	{
		final String viewSql;
		if (table.isView() && (viewSql = table.getViewSql()) != null)
		{
			DOMUtil.appendAttribute(element, "viewSql", viewSql);
		}
	}

	private static boolean isBinary(final String input)
	{
		return !XmlTableFormatter.validXmlChars.matcher(input).matches();
	}

	private String asBinary(final String s)
	{
		final byte[] bytes = s.getBytes();
		final StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (int i = 0; i < bytes.length; ++i)
		{
			sb.append(String.format("%02X", bytes[i]));
		}
		return sb.toString();
	}

	static
	{
		instance = new XmlTableFormatter();
		validXmlChars = Pattern.compile("^[ -\ud7ff\ue000-\ufffd\\p{L}\\p{M}\\p{Z}\\p{S}\\p{N}\\p{P}]*$");
	}
}
