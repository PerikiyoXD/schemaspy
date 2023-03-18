// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ForeignKeyConstraint implements Comparable<ForeignKeyConstraint>
{
	private final String name;
	private Table parentTable;
	private final List<TableColumn> parentColumns;
	private final Table childTable;
	private final List<TableColumn> childColumns;
	private final int deleteRule;
	private final int updateRule;
	private static final Logger logger;
	private static final boolean finerEnabled;

	ForeignKeyConstraint(final Table table, final String name, final int updateRule, final int deleteRule)
	{
		this.parentColumns = new ArrayList<TableColumn>();
		this.childColumns = new ArrayList<TableColumn>();
		this.name = name;
		if (ForeignKeyConstraint.finerEnabled)
		{
			ForeignKeyConstraint.logger.finer("Adding foreign key constraint '" + this.getName() + "' to " + table);
		}
		this.childTable = table;
		this.deleteRule = deleteRule;
		this.updateRule = updateRule;
	}

	public ForeignKeyConstraint(
			final TableColumn tableColumn, final TableColumn tableColumn2, final int n, final int n2
	)
	{
		this(tableColumn2.getTable(), null, n, n2);
		this.addChildColumn(tableColumn2);
		this.addParentColumn(tableColumn);
		tableColumn2.addParent(tableColumn, this);
		tableColumn.addChild(tableColumn2, this);
	}

	public ForeignKeyConstraint(final TableColumn tableColumn, final TableColumn tableColumn2)
	{
		this(tableColumn, tableColumn2, 3, 3);
	}

	void addParentColumn(final TableColumn tableColumn)
	{
		if (tableColumn != null)
		{
			this.parentColumns.add(tableColumn);
			this.parentTable = tableColumn.getTable();
		}
	}

	void addChildColumn(final TableColumn tableColumn)
	{
		if (tableColumn != null)
		{
			this.childColumns.add(tableColumn);
		}
	}

	public String getName()
	{
		return this.name;
	}

	public Table getParentTable()
	{
		return this.parentTable;
	}

	public List<TableColumn> getParentColumns()
	{
		return Collections.unmodifiableList((List<? extends TableColumn>) this.parentColumns);
	}

	public Table getChildTable()
	{
		return this.childTable;
	}

	public List<TableColumn> getChildColumns()
	{
		return Collections.unmodifiableList((List<? extends TableColumn>) this.childColumns);
	}

	public int getDeleteRule()
	{
		return this.deleteRule;
	}

	public boolean isCascadeOnDelete()
	{
		return this.getDeleteRule() == 0;
	}

	public boolean isRestrictDelete()
	{
		return this.getDeleteRule() == 3 || this.getDeleteRule() == 1;
	}

	public boolean isNullOnDelete()
	{
		return this.getDeleteRule() == 2;
	}

	public String getDeleteRuleName()
	{
		switch (this.getDeleteRule())
		{
			case 0:
			{
				return "Cascade on delete";
			}
			case 1:
			case 3:
			{
				return "Restrict delete";
			}
			case 2:
			{
				return "Null on delete";
			}
			default:
			{
				return "";
			}
		}
	}

	public String getDeleteRuleDescription()
	{
		switch (this.getDeleteRule())
		{
			case 0:
			{
				return "Cascade on delete:\n Deletion of parent deletes child";
			}
			case 1:
			case 3:
			{
				return "Restrict delete:\n Parent cannot be deleted if children exist";
			}
			case 2:
			{
				return "Null on delete:\n Foreign key to parent set to NULL when parent deleted";
			}
			default:
			{
				return "";
			}
		}
	}

	public String getDeleteRuleAlias()
	{
		switch (this.getDeleteRule())
		{
			case 0:
			{
				return "C";
			}
			case 1:
			case 3:
			{
				return "R";
			}
			case 2:
			{
				return "N";
			}
			default:
			{
				return "";
			}
		}
	}

	public int getUpdateRule()
	{
		return this.updateRule;
	}

	public boolean isImplied()
	{
		return false;
	}

	public boolean isReal()
	{
		return this.getClass() == ForeignKeyConstraint.class;
	}

	public int compareTo(final ForeignKeyConstraint foreignKeyConstraint)
	{
		if (foreignKeyConstraint == this)
		{
			return 0;
		}
		int n = this.getName().compareToIgnoreCase(foreignKeyConstraint.getName());
		if (n == 0)
		{
			final String schema = this.getChildColumns().get(0).getTable().getSchema();
			final String schema2 = foreignKeyConstraint.getChildColumns().get(0).getTable().getSchema();
			if (schema != null && schema2 != null)
			{
				n = schema.compareToIgnoreCase(schema2);
			} else if (schema == null)
			{
				n = -1;
			} else
			{
				n = 1;
			}
		}
		return n;
	}

	public static String toString(final List<TableColumn> list)
	{
		if (list.size() == 1)
		{
			return list.iterator().next().toString();
		}
		return list.toString();
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(this.childTable.getName());
		sb.append('.');
		sb.append(toString(this.childColumns));
		sb.append(" refs ");
		sb.append(this.parentTable.getName());
		sb.append('.');
		sb.append(toString(this.parentColumns));
		if (this.parentTable.isRemote())
		{
			sb.append(" in ");
			sb.append(this.parentTable.getSchema());
		}
		if (this.name != null)
		{
			sb.append(" via ");
			sb.append(this.name);
		}
		return sb.toString();
	}

	static
	{
		logger = Logger.getLogger(ForeignKeyConstraint.class.getName());
		finerEnabled = ForeignKeyConstraint.logger.isLoggable(Level.FINER);
	}
}
