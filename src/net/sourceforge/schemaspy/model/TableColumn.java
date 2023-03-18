// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.sourceforge.schemaspy.model.xml.TableColumnMeta;

public class TableColumn
{
	private final Table table;
	private final String name;
	private final Object id;
	private final String type;
	private final int length;
	private final int decimalDigits;
	private final String detailedSize;
	private final boolean isNullable;
	private boolean isAutoUpdated;
	private Boolean isUnique;
	private final Object defaultValue;
	private String comments;
	private final Map<TableColumn, ForeignKeyConstraint> parents;
	private final Map<TableColumn, ForeignKeyConstraint> children;
	private boolean allowImpliedParents;
	private boolean allowImpliedChildren;
	private boolean isExcluded;
	private boolean isAllExcluded;
	private static final Logger logger;
	private static final boolean finerEnabled;

	TableColumn(final Table table, final ResultSet set, final Pattern obj, final Pattern obj2) throws SQLException
	{
		this.parents = new HashMap<TableColumn, ForeignKeyConstraint>();
		this.children = new TreeMap<TableColumn, ForeignKeyConstraint>(new ColumnComparator());
		this.allowImpliedParents = true;
		this.allowImpliedChildren = true;
		this.isExcluded = false;
		this.isAllExcluded = false;
		this.table = table;
		final String string = set.getString("COLUMN_NAME");
		this.name = ((string == null) ? null : string.intern());
		final String string2 = set.getString("TYPE_NAME");
		this.type = ((string2 == null) ? "unknown" : string2.intern());
		this.decimalDigits = set.getInt("DECIMAL_DIGITS");
		final Number n = (Number) set.getObject("BUFFER_LENGTH");
		if (n != null && n.shortValue() > 0)
		{
			this.length = n.shortValue();
		} else
		{
			this.length = set.getInt("COLUMN_SIZE");
		}
		final StringBuilder sb = new StringBuilder();
		sb.append(this.length);
		if (this.decimalDigits > 0)
		{
			sb.append(',');
			sb.append(this.decimalDigits);
		}
		this.detailedSize = sb.toString();
		this.isNullable = (set.getInt("NULLABLE") == 1);
		this.defaultValue = set.getString("COLUMN_DEF");
		this.setComments(set.getString("REMARKS"));
		this.id = set.getInt("ORDINAL_POSITION") - 1;
		this.isAllExcluded = this.matches(obj2);
		this.isExcluded = (this.isAllExcluded || this.matches(obj));
		if (this.isExcluded && TableColumn.finerEnabled)
		{
			TableColumn.logger.finer(
					"Excluding column " + this.getTable() + '.' + this.getName() + ": matches " + obj2 + ":"
							+ this.isAllExcluded + " " + obj + ":" + this.matches(obj)
			);
		}
	}

	public TableColumn(final Table table, final TableColumnMeta tableColumnMeta)
	{
		this.parents = new HashMap<TableColumn, ForeignKeyConstraint>();
		this.children = new TreeMap<TableColumn, ForeignKeyConstraint>(new ColumnComparator());
		this.allowImpliedParents = true;
		this.allowImpliedChildren = true;
		this.isExcluded = false;
		this.isAllExcluded = false;
		this.table = table;
		this.name = tableColumnMeta.getName();
		this.id = null;
		this.type = "Unknown";
		this.length = 0;
		this.decimalDigits = 0;
		this.detailedSize = "";
		this.isNullable = false;
		this.isAutoUpdated = false;
		this.defaultValue = null;
		this.comments = tableColumnMeta.getComments();
	}

	public Table getTable()
	{
		return this.table;
	}

	public String getName()
	{
		return this.name;
	}

	public Object getId()
	{
		return this.id;
	}

	public String getType()
	{
		return this.type;
	}

	public int getLength()
	{
		return this.length;
	}

	public int getDecimalDigits()
	{
		return this.decimalDigits;
	}

	public String getDetailedSize()
	{
		return this.detailedSize;
	}

	public boolean isNullable()
	{
		return this.isNullable;
	}

	public boolean isAutoUpdated()
	{
		return this.isAutoUpdated;
	}

	public void setIsAutoUpdated(final boolean isAutoUpdated)
	{
		this.isAutoUpdated = isAutoUpdated;
	}

	public boolean isUnique()
	{
		if (this.isUnique == null)
		{
			for (final TableIndex tableIndex : this.table.getIndexes())
			{
				if (tableIndex.isUnique())
				{
					final List<TableColumn> columns = tableIndex.getColumns();
					if (columns.size() == 1 && columns.contains(this))
					{
						this.isUnique = true;
						break;
					}
					continue;
				}
			}
			if (this.isUnique == null)
			{
				this.isUnique = (this.table.getPrimaryColumns().size() == 1 && this.isPrimary());
			}
		}
		return this.isUnique;
	}

	public boolean isPrimary()
	{
		return this.table.getPrimaryColumns().contains(this);
	}

	public boolean isForeignKey()
	{
		return !this.parents.isEmpty();
	}

	public Object getDefaultValue()
	{
		return this.defaultValue;
	}

	public String getComments()
	{
		return this.comments;
	}

	public void setComments(final String s)
	{
		this.comments = ((s == null || s.trim().length() == 0) ? null : s.trim());
	}

	public boolean isExcluded()
	{
		return this.isExcluded;
	}

	public boolean isAllExcluded()
	{
		return this.isAllExcluded;
	}

	public void addParent(final TableColumn tableColumn, final ForeignKeyConstraint foreignKeyConstraint)
	{
		this.parents.put(tableColumn, foreignKeyConstraint);
		this.table.addedParent();
	}

	public void removeParent(final TableColumn tableColumn)
	{
		this.parents.remove(tableColumn);
	}

	public void unlinkParents()
	{
		final Iterator<TableColumn> iterator = this.parents.keySet().iterator();
		while (iterator.hasNext())
		{
			iterator.next().removeChild(this);
		}
		this.parents.clear();
	}

	public Set<TableColumn> getParents()
	{
		return this.parents.keySet();
	}

	public ForeignKeyConstraint getParentConstraint(final TableColumn tableColumn)
	{
		return this.parents.get(tableColumn);
	}

	public ForeignKeyConstraint removeAParentFKConstraint()
	{
		final Iterator<TableColumn> iterator = this.parents.keySet().iterator();
		if (iterator.hasNext())
		{
			final TableColumn tableColumn = iterator.next();
			final ForeignKeyConstraint foreignKeyConstraint = this.parents.remove(tableColumn);
			tableColumn.removeChild(this);
			return foreignKeyConstraint;
		}
		return null;
	}

	public ForeignKeyConstraint removeAChildFKConstraint()
	{
		final Iterator<TableColumn> iterator = this.children.keySet().iterator();
		if (iterator.hasNext())
		{
			final TableColumn tableColumn = iterator.next();
			final ForeignKeyConstraint foreignKeyConstraint = this.children.remove(tableColumn);
			tableColumn.removeParent(this);
			return foreignKeyConstraint;
		}
		return null;
	}

	public void addChild(final TableColumn tableColumn, final ForeignKeyConstraint foreignKeyConstraint)
	{
		this.children.put(tableColumn, foreignKeyConstraint);
		this.table.addedChild();
	}

	public void removeChild(final TableColumn tableColumn)
	{
		this.children.remove(tableColumn);
	}

	public void unlinkChildren()
	{
		final Iterator<TableColumn> iterator = this.children.keySet().iterator();
		while (iterator.hasNext())
		{
			iterator.next().removeParent(this);
		}
		this.children.clear();
	}

	public Set<TableColumn> getChildren()
	{
		return this.children.keySet();
	}

	public ForeignKeyConstraint getChildConstraint(final TableColumn tableColumn)
	{
		return this.children.get(tableColumn);
	}

	public boolean matches(final Pattern pattern)
	{
		return pattern.matcher(this.getTable().getName() + '.' + this.getName()).matches();
	}

	public void update(final TableColumnMeta tableColumnMeta)
	{
		final String comments = tableColumnMeta.getComments();
		if (comments != null)
		{
			this.setComments(comments);
		}
		if (!this.isPrimary() && tableColumnMeta.isPrimary())
		{
			this.table.setPrimaryColumn(this);
		}
		this.allowImpliedParents = !tableColumnMeta.isImpliedParentsDisabled();
		this.allowImpliedChildren = !tableColumnMeta.isImpliedChildrenDisabled();
		this.isExcluded |= tableColumnMeta.isExcluded();
		this.isAllExcluded |= tableColumnMeta.isAllExcluded();
	}

	@Override
	public String toString()
	{
		return this.getName();
	}

	public boolean allowsImpliedParents()
	{
		return this.allowImpliedParents;
	}

	public boolean allowsImpliedChildren()
	{
		return this.allowImpliedChildren;
	}

	static
	{
		logger = Logger.getLogger(TableColumn.class.getName());
		finerEnabled = TableColumn.logger.isLoggable(Level.FINER);
	}

	private class ColumnComparator implements Comparator<TableColumn>
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
}
