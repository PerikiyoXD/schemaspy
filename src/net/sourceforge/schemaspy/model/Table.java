// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.model.xml.ForeignKeyMeta;
import net.sourceforge.schemaspy.model.xml.TableColumnMeta;
import net.sourceforge.schemaspy.model.xml.TableMeta;
import net.sourceforge.schemaspy.util.CaseInsensitiveMap;

public class Table implements Comparable<Table>
{
	private final String schema;
	private final String name;
	protected final CaseInsensitiveMap<TableColumn> columns;
	private final List<TableColumn> primaryKeys;
	private final CaseInsensitiveMap<ForeignKeyConstraint> foreignKeys;
	private final CaseInsensitiveMap<TableIndex> indexes;
	private Object id;
	private final Map<String, String> checkConstraints;
	private Integer numRows;
	protected final Database db;
	protected final Properties properties;
	private String comments;
	private int maxChildren;
	private int maxParents;
	private static final Logger logger;

	public Table(
			final Database db, final String str, final String s, final String comments, final Properties properties,
			final Pattern pattern, final Pattern pattern2
	) throws SQLException
	{
		this.columns = new CaseInsensitiveMap<TableColumn>();
		this.primaryKeys = new ArrayList<TableColumn>();
		this.foreignKeys = new CaseInsensitiveMap<ForeignKeyConstraint>();
		this.indexes = new CaseInsensitiveMap<TableIndex>();
		this.checkConstraints = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
		this.schema = str;
		this.name = s;
		this.db = db;
		this.properties = properties;
		Table.logger.fine(
				("Creating " + this.getClass().getSimpleName().toLowerCase() + " " + str == null) ? s : (str + '.' + s)
		);
		this.setComments(comments);
		this.initColumns(pattern, pattern2);
		this.initIndexes();
		this.initPrimaryKeys(db.getMetaData());
	}

	public void connectForeignKeys(final Map<String, Table> map, final Pattern pattern, final Pattern pattern2)
			throws SQLException
	{
		ResultSet set = null;
		try
		{
			set = this.db.getMetaData().getImportedKeys(null, this.getSchema(), this.getName());
			while (set.next())
			{
				this.addForeignKey(
						set.getString("FK_NAME"), set.getString("FKCOLUMN_NAME"), set.getString("PKTABLE_SCHEM"),
						set.getString("PKTABLE_NAME"), set.getString("PKCOLUMN_NAME"), set.getInt("UPDATE_RULE"),
						set.getInt("DELETE_RULE"), map, pattern, pattern2
				);
			}
		}
		finally
		{
			if (set != null)
			{
				set.close();
			}
		}
		if (this.getSchema() != null)
		{
			try
			{
				set = this.db.getMetaData().getExportedKeys(null, this.getSchema(), this.getName());
				while (set.next())
				{
					final String string = set.getString("FKTABLE_SCHEM");
					if (!this.getSchema().equals(string))
					{
						this.db.addRemoteTable(
								string, set.getString("FKTABLE_NAME"), this.getSchema(), this.properties, pattern,
								pattern2
						);
					}
				}
			}
			finally
			{
				if (set != null)
				{
					set.close();
				}
			}
		}
	}

	public Collection<ForeignKeyConstraint> getForeignKeys()
	{
		return Collections.unmodifiableCollection(this.foreignKeys.values());
	}

	public void addCheckConstraint(final String s, final String s2)
	{
		this.checkConstraints.put(s, s2);
	}

	protected void addForeignKey(
			final String s, final String str, final String anObject, final String str2, final String str3, final int n,
			final int n2, final Map<String, Table> map, final Pattern pattern, final Pattern pattern2
	) throws SQLException
	{
		if (s == null)
		{
			return;
		}
		ForeignKeyConstraint foreignKeyConstraint = this.foreignKeys.get(s);
		if (foreignKeyConstraint == null)
		{
			foreignKeyConstraint = new ForeignKeyConstraint(this, s, n, n2);
			this.foreignKeys.put(s, foreignKeyConstraint);
		}
		final TableColumn column = this.getColumn(str);
		if (column != null)
		{
			foreignKeyConstraint.addChildColumn(column);
			Table addRemoteTable = map.get(str2);
			final String schema = Config.getInstance().getSchema();
			if (addRemoteTable == null || (schema != null && anObject != null && !schema.equals(anObject)))
			{
				addRemoteTable = this.db.addRemoteTable(anObject, str2, schema, this.properties, pattern, pattern2);
			}
			if (addRemoteTable != null)
			{
				final TableColumn column2 = addRemoteTable.getColumn(str3);
				if (column2 != null)
				{
					foreignKeyConstraint.addParentColumn(column2);
					column.addParent(column2, foreignKeyConstraint);
					column2.addChild(column, foreignKeyConstraint);
				} else
				{
					Table.logger.warning(
							"Couldn't add FK '" + foreignKeyConstraint.getName() + "' to table '" + this
									+ "' - Column '" + str3 + "' doesn't exist in table '" + addRemoteTable + "'"
					);
				}
			} else
			{
				Table.logger.warning(
						"Couldn't add FK '" + foreignKeyConstraint.getName() + "' to table '" + this
								+ "' - Unknown Referenced Table '" + str2 + "'"
				);
			}
		} else
		{
			Table.logger.warning(
					"Couldn't add FK '" + foreignKeyConstraint.getName() + "' to table '" + this + "' - Column '" + str
							+ "' doesn't exist"
			);
		}
	}

	private void initPrimaryKeys(final DatabaseMetaData databaseMetaData) throws SQLException
	{
		if (this.properties == null)
		{
			return;
		}
		ResultSet primaryKeys = null;
		try
		{
			primaryKeys = databaseMetaData.getPrimaryKeys(null, this.getSchema(), this.getName());
			while (primaryKeys.next())
			{
				this.setPrimaryColumn(primaryKeys);
			}
		}
		finally
		{
			if (primaryKeys != null)
			{
				primaryKeys.close();
			}
		}
	}

	private void setPrimaryColumn(final ResultSet set) throws SQLException
	{
		final String string = set.getString("PK_NAME");
		if (string == null)
		{
			return;
		}
		final TableIndex index = this.getIndex(string);
		if (index != null)
		{
			index.setIsPrimaryKey(true);
		}
		this.setPrimaryColumn(this.getColumn(set.getString("COLUMN_NAME")));
	}

	void setPrimaryColumn(final TableColumn tableColumn)
	{
		this.primaryKeys.add(tableColumn);
	}

	private void initColumns(final Pattern pattern, final Pattern pattern2) throws SQLException
	{
		ResultSet columns = null;
		synchronized (Table.class)
		{
			try
			{
				columns = this.db.getMetaData().getColumns(null, this.getSchema(), this.getName(), "%");
				while (columns.next())
				{
					this.addColumn(columns, pattern, pattern2);
				}
			}
			catch (SQLException ex)
			{
				class ColumnInitializationFailure extends SQLException
				{
					private static final long serialVersionUID = 1L;

					public ColumnInitializationFailure(SQLException ex)
					{
						super(
								"Failed to collect column details for " + (Table.this.isView() ? "view" : "table")
										+ " '" + Table.this.getName() + "' in schema '" + Table.this.getSchema()
										+ "' - " + ex.getMessage()
						);
						this.initCause(getCause());
					}
				}
				throw new ColumnInitializationFailure(ex);
			}
			finally
			{
				if (columns != null)
				{
					columns.close();
				}
			}
		}
		if (!this.isView() && !this.isRemote())
		{
			this.initColumnAutoUpdate(false);
		}
	}

	private void initColumnAutoUpdate(final boolean b) throws SQLException
	{
		ResultSet executeQuery = null;
		PreparedStatement prepareStatement = null;
		final StringBuilder sb = new StringBuilder("select * from ");
		if (this.getSchema() != null)
		{
			sb.append(this.getSchema());
			sb.append('.');
		}
		if (b)
		{
			final String trim = this.db.getMetaData().getIdentifierQuoteString().trim();
			sb.append(trim + this.getName() + trim);
		} else
		{
			sb.append(this.db.getQuotedIdentifier(this.getName()));
		}
		sb.append(" where 0 = 1");
		try
		{
			prepareStatement = this.db.getMetaData().getConnection().prepareStatement(sb.toString());
			executeQuery = prepareStatement.executeQuery();
			final ResultSetMetaData metaData = executeQuery.getMetaData();
			for (int i = metaData.getColumnCount(); i > 0; --i)
			{
				this.getColumn(metaData.getColumnName(i)).setIsAutoUpdated(metaData.isAutoIncrement(i));
			}
		}
		catch (SQLException obj)
		{
			if (b)
			{
				Table.logger.warning("Failed to determine auto increment status: " + obj);
				Table.logger.warning("SQL: " + sb.toString());
			} else
			{
				this.initColumnAutoUpdate(true);
			}
		}
		finally
		{
			if (executeQuery != null)
			{
				executeQuery.close();
			}
			if (prepareStatement != null)
			{
				prepareStatement.close();
			}
		}
	}

	protected void addColumn(final ResultSet set, final Pattern pattern, final Pattern pattern2) throws SQLException
	{
		final String string = set.getString("COLUMN_NAME");
		if (string == null)
		{
			return;
		}
		if (this.getColumn(string) == null)
		{
			final TableColumn tableColumn = new TableColumn(this, set, pattern, pattern2);
			this.columns.put(tableColumn.getName(), tableColumn);
		}
	}

	protected TableColumn addColumn(final TableColumnMeta tableColumnMeta)
	{
		final TableColumn tableColumn = new TableColumn(this, tableColumnMeta);
		this.columns.put(tableColumn.getName(), tableColumn);
		return tableColumn;
	}

	private void initIndexes() throws SQLException
	{
		if (this.isView() || this.isRemote())
		{
			return;
		}
		if (this.initIndexes(this.properties.getProperty("selectIndexesSql")))
		{
			return;
		}
		ResultSet indexInfo = null;
		try
		{
			indexInfo = this.db.getMetaData().getIndexInfo(null, this.getSchema(), this.getName(), false, true);
			while (indexInfo.next())
			{
				if (indexInfo.getShort("TYPE") != 0)
				{
					this.addIndex(indexInfo);
				}
			}
		}
		catch (SQLException obj)
		{
			Table.logger.warning(
					"Unable to extract index info for table '" + this.getName() + "' in schema '" + this.getSchema()
							+ "': " + obj
			);
		}
		finally
		{
			if (indexInfo != null)
			{
				indexInfo.close();
			}
		}
	}

	private boolean initIndexes(final String str)
	{
		if (str == null)
		{
			return false;
		}
		PreparedStatement prepareStatement = null;
		ResultSet executeQuery = null;
		try
		{
			prepareStatement = this.db.prepareStatement(str, this.getName());
			executeQuery = prepareStatement.executeQuery();
			while (executeQuery.next())
			{
				if (executeQuery.getShort("TYPE") != 0)
				{
					this.addIndex(executeQuery);
				}
			}
		}
		catch (SQLException ex)
		{
			Table.logger.warning("Failed to query index information with SQL: " + str);
			Table.logger.warning(ex.toString());
			return false;
		}
		finally
		{
			if (executeQuery != null)
			{
				try
				{
					executeQuery.close();
				}
				catch (Exception ex2)
				{
					ex2.printStackTrace();
				}
			}
			if (prepareStatement != null)
			{
				try
				{
					prepareStatement.close();
				}
				catch (Exception ex3)
				{
					ex3.printStackTrace();
				}
			}
		}
		return true;
	}

	public TableIndex getIndex(final String s)
	{
		return this.indexes.get(s);
	}

	private void addIndex(final ResultSet set) throws SQLException
	{
		final String string = set.getString("INDEX_NAME");
		if (string == null)
		{
			return;
		}
		TableIndex index = this.getIndex(string);
		if (index == null)
		{
			index = new TableIndex(set);
			this.indexes.put(index.getName(), index);
		}
		index.addColumn(this.getColumn(set.getString("COLUMN_NAME")), set.getString("ASC_OR_DESC"));
	}

	public String getSchema()
	{
		return this.schema;
	}

	public String getName()
	{
		return this.name;
	}

	public void setId(final Object id)
	{
		this.id = id;
	}

	public Object getId()
	{
		return this.id;
	}

	public Map<String, String> getCheckConstraints()
	{
		return this.checkConstraints;
	}

	public Set<TableIndex> getIndexes()
	{
		return new HashSet<TableIndex>(this.indexes.values());
	}

	public List<TableColumn> getPrimaryColumns()
	{
		return this.primaryKeys;
	}

	public String getComments()
	{
		return this.comments;
	}

	public void setComments(final String s)
	{
		String comments = (s == null || s.trim().length() == 0) ? null : s.trim();
		if (comments != null)
		{
			int index = comments.indexOf("; InnoDB free: ");
			if (index == -1)
			{
				index = (comments.startsWith("InnoDB free: ") ? 0 : -1);
			}
			if (index != -1)
			{
				final String trim = comments.substring(0, index).trim();
				comments = ((trim.length() == 0) ? null : trim);
			}
		}
		this.comments = comments;
	}

	public TableColumn getColumn(final String s)
	{
		return this.columns.get(s);
	}

	public List<TableColumn> getColumns()
	{
		final TreeSet<TableColumn> c = new TreeSet<TableColumn>(new ByColumnIdComparator());
		c.addAll(this.columns.values());
		return new ArrayList<TableColumn>(c);
	}

	public boolean isRoot()
	{
		final Iterator<TableColumn> iterator = this.columns.values().iterator();
		while (iterator.hasNext())
		{
			if (iterator.next().isForeignKey())
			{
				return false;
			}
		}
		return true;
	}

	public boolean isLeaf()
	{
		final Iterator<TableColumn> iterator = this.columns.values().iterator();
		while (iterator.hasNext())
		{
			if (!iterator.next().getChildren().isEmpty())
			{
				return false;
			}
		}
		return true;
	}

	public int getMaxParents()
	{
		return this.maxParents;
	}

	public void addedParent()
	{
		++this.maxParents;
	}

	public void unlinkParents()
	{
		final Iterator<TableColumn> iterator = this.columns.values().iterator();
		while (iterator.hasNext())
		{
			iterator.next().unlinkParents();
		}
	}

	public int getMaxChildren()
	{
		return this.maxChildren;
	}

	public void addedChild()
	{
		++this.maxChildren;
	}

	public void unlinkChildren()
	{
		final Iterator<TableColumn> iterator = this.columns.values().iterator();
		while (iterator.hasNext())
		{
			iterator.next().unlinkChildren();
		}
	}

	public ForeignKeyConstraint removeSelfReferencingConstraint()
	{
		return this.remove(this.getSelfReferencingConstraint());
	}

	private ForeignKeyConstraint remove(final ForeignKeyConstraint foreignKeyConstraint)
	{
		if (foreignKeyConstraint != null)
		{
			for (int i = 0; i < foreignKeyConstraint.getChildColumns().size(); ++i)
			{
				final TableColumn tableColumn = foreignKeyConstraint.getChildColumns().get(i);
				final TableColumn tableColumn2 = foreignKeyConstraint.getParentColumns().get(i);
				tableColumn.removeParent(tableColumn2);
				tableColumn2.removeChild(tableColumn);
			}
		}
		return foreignKeyConstraint;
	}

	private ForeignKeyConstraint getSelfReferencingConstraint()
	{
		for (final TableColumn tableColumn : this.columns.values())
		{
			for (final TableColumn tableColumn2 : tableColumn.getParents())
			{
				if (this.compareTo(tableColumn2.getTable()) == 0)
				{
					return tableColumn.getParentConstraint(tableColumn2);
				}
			}
		}
		return null;
	}

	public List<ForeignKeyConstraint> removeNonRealForeignKeys()
	{
		final ArrayList<ForeignKeyConstraint> list = new ArrayList<ForeignKeyConstraint>();
		for (final TableColumn tableColumn : this.columns.values())
		{
			final Iterator<TableColumn> iterator2 = tableColumn.getParents().iterator();
			while (iterator2.hasNext())
			{
				final ForeignKeyConstraint parentConstraint = tableColumn.getParentConstraint(iterator2.next());
				if (parentConstraint != null && !parentConstraint.isReal())
				{
					list.add(parentConstraint);
				}
			}
		}
		final Iterator<ForeignKeyConstraint> iterator3 = list.iterator();
		while (iterator3.hasNext())
		{
			this.remove(iterator3.next());
		}
		return list;
	}

	public int getNumChildren()
	{
		int n = 0;
		final Iterator<TableColumn> iterator = this.columns.values().iterator();
		while (iterator.hasNext())
		{
			n += iterator.next().getChildren().size();
		}
		return n;
	}

	public int getNumNonImpliedChildren()
	{
		int n = 0;
		for (final TableColumn tableColumn : this.columns.values())
		{
			final Iterator<TableColumn> iterator2 = tableColumn.getChildren().iterator();
			while (iterator2.hasNext())
			{
				if (!tableColumn.getChildConstraint(iterator2.next()).isImplied())
				{
					++n;
				}
			}
		}
		return n;
	}

	public int getNumParents()
	{
		int n = 0;
		final Iterator<TableColumn> iterator = this.columns.values().iterator();
		while (iterator.hasNext())
		{
			n += iterator.next().getParents().size();
		}
		return n;
	}

	public int getNumNonImpliedParents()
	{
		int n = 0;
		for (final TableColumn tableColumn : this.columns.values())
		{
			final Iterator<TableColumn> iterator2 = tableColumn.getParents().iterator();
			while (iterator2.hasNext())
			{
				if (!tableColumn.getParentConstraint(iterator2.next()).isImplied())
				{
					++n;
				}
			}
		}
		return n;
	}

	public ForeignKeyConstraint removeAForeignKeyConstraint()
	{
		final List<TableColumn> columns = this.getColumns();
		int n = 0;
		int n2 = 0;
		for (final TableColumn tableColumn : columns)
		{
			n += tableColumn.getParents().size();
			n2 += tableColumn.getChildren().size();
		}
		for (final TableColumn tableColumn2 : columns)
		{
			ForeignKeyConstraint foreignKeyConstraint;
			if (n <= n2)
			{
				foreignKeyConstraint = tableColumn2.removeAParentFKConstraint();
			} else
			{
				foreignKeyConstraint = tableColumn2.removeAChildFKConstraint();
			}
			if (foreignKeyConstraint != null)
			{
				return foreignKeyConstraint;
			}
		}
		return null;
	}

	public boolean isView()
	{
		return false;
	}

	public boolean isRemote()
	{
		return false;
	}

	public String getViewSql()
	{
		return null;
	}

	public int getNumRows()
	{
		if (this.numRows == null)
		{
			this.numRows = (Config.getInstance().isNumRowsEnabled() ? this.fetchNumRows() : -1);
		}
		return this.numRows;
	}

	public void setNumRows(final int i)
	{
		this.numRows = i;
	}

	protected int fetchNumRows()
	{
		if (this.properties == null)
		{
			return 0;
		}
		Throwable t = null;
		final String property = this.properties.getProperty("selectRowCountSql");
		if (property != null)
		{
			PreparedStatement prepareStatement = null;
			ResultSet executeQuery = null;
			try
			{
				prepareStatement = this.db.prepareStatement(property, this.getName());
				executeQuery = prepareStatement.executeQuery();
				if (executeQuery.next())
				{
					return executeQuery.getInt("row_count");
				}
			}
			catch (SQLException ex)
			{
				t = ex;
			}
			finally
			{
				if (executeQuery != null)
				{
					try
					{
						executeQuery.close();
					}
					catch (SQLException ex4)
					{
					}
				}
				if (prepareStatement != null)
				{
					try
					{
						prepareStatement.close();
					}
					catch (SQLException ex5)
					{
					}
				}
			}
		}
		try
		{
			return this.fetchNumRows("count(*)", false);
		}
		catch (SQLException ex2)
		{
			try
			{
				return this.fetchNumRows("count(1)", false);
			}
			catch (SQLException ex3)
			{
				Table.logger
						.warning("Unable to extract the number of rows for table " + this.getName() + ", using '-1'");
				if (t != null)
				{
					Table.logger.warning(t.toString());
				}
				Table.logger.warning(ex2.toString());
				Table.logger.warning(ex3.toString());
				return -1;
			}
		}
	}

	protected int fetchNumRows(final String str, final boolean b) throws SQLException
	{
		PreparedStatement prepareStatement = null;
		ResultSet executeQuery = null;
		final StringBuilder sb = new StringBuilder("select ");
		sb.append(str);
		sb.append(" from ");
		if (this.getSchema() != null)
		{
			sb.append(this.getSchema());
			sb.append('.');
		}
		if (b)
		{
			final String trim = this.db.getMetaData().getIdentifierQuoteString().trim();
			sb.append(trim + this.getName() + trim);
		} else
		{
			sb.append(this.db.getQuotedIdentifier(this.getName()));
		}
		try
		{
			prepareStatement = this.db.getConnection().prepareStatement(sb.toString());
			executeQuery = prepareStatement.executeQuery();
			if (executeQuery.next())
			{
				return executeQuery.getInt(1);
			}
			return -1;
		}
		catch (SQLException ex)
		{
			if (b)
			{
				throw ex;
			}
			return this.fetchNumRows(str, true);
		}
		finally
		{
			if (executeQuery != null)
			{
				executeQuery.close();
			}
			if (prepareStatement != null)
			{
				prepareStatement.close();
			}
		}
	}

	public void update(final TableMeta tableMeta)
	{
		final String comments = tableMeta.getComments();
		if (comments != null)
		{
			this.comments = comments;
		}
		for (final TableColumnMeta tableColumnMeta : tableMeta.getColumns())
		{
			TableColumn tableColumn = this.getColumn(tableColumnMeta.getName());
			if (tableColumn == null)
			{
				if (tableMeta.getRemoteSchema() == null)
				{
					Table.logger.warning(
							"Unrecognized column '" + tableColumnMeta.getName() + "' for table '" + this.getName()
									+ '\''
					);
					continue;
				}
				tableColumn = this.addColumn(tableColumnMeta);
			}
			tableColumn.update(tableColumnMeta);
		}
	}

	public void connect(final TableMeta tableMeta, final Map<String, Table> map, final Map<String, Table> map2)
	{
		for (final TableColumnMeta tableColumnMeta : tableMeta.getColumns())
		{
			final TableColumn column = this.getColumn(tableColumnMeta.getName());
			for (final ForeignKeyMeta foreignKeyMeta : tableColumnMeta.getForeignKeys())
			{
				final Table table = (foreignKeyMeta.getRemoteSchema() == null) ? map.get(foreignKeyMeta.getTableName())
						: map2.get(foreignKeyMeta.getRemoteSchema() + '.' + foreignKeyMeta.getTableName());
				if (table != null)
				{
					final TableColumn column2 = table.getColumn(foreignKeyMeta.getColumnName());
					if (column2 == null)
					{
						Table.logger.warning(table.getName() + '.' + foreignKeyMeta.getColumnName() + " doesn't exist");
					} else
					{
						new ForeignKeyConstraint(column2, column)
						{
							@Override
							public String getName()
							{
								return "Defined in XML";
							}
						};
					}
				} else
				{
					Table.logger.warning(
							"Undefined table '" + foreignKeyMeta.getTableName() + "' referenced by '" + this.getName()
									+ '.' + column.getName() + '\''
					);
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return this.getName();
	}

	public boolean isOrphan(final boolean b)
	{
		if (b)
		{
			return this.getMaxParents() == 0 && this.getMaxChildren() == 0;
		}
		for (final TableColumn tableColumn : this.columns.values())
		{
			final Iterator<TableColumn> iterator2 = tableColumn.getParents().iterator();
			while (iterator2.hasNext())
			{
				if (!tableColumn.getParentConstraint(iterator2.next()).isImplied())
				{
					return false;
				}
			}
			final Iterator<TableColumn> iterator3 = tableColumn.getChildren().iterator();
			while (iterator3.hasNext())
			{
				if (!tableColumn.getChildConstraint(iterator3.next()).isImplied())
				{
					return false;
				}
			}
		}
		return true;
	}

	public int compareTo(final Table table)
	{
		if (table == this)
		{
			return 0;
		}
		int n = this.getName().compareToIgnoreCase(table.getName());
		if (n == 0)
		{
			final String schema = this.getSchema();
			final String schema2 = table.getSchema();
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

	static
	{
		logger = Logger.getLogger(Table.class.getName());
	}

	private static class ByColumnIdComparator implements Comparator<TableColumn>
	{
		public int compare(final TableColumn tableColumn, final TableColumn tableColumn2)
		{
			if (tableColumn.getId() == null || tableColumn2.getId() == null)
			{
				return tableColumn.getName().compareToIgnoreCase(tableColumn2.getName());
			}
			if (tableColumn.getId() instanceof Number)
			{
				return ((Number) tableColumn.getId()).intValue() - ((Number) tableColumn2.getId()).intValue();
			}
			return tableColumn.getId().toString().compareToIgnoreCase(tableColumn2.getId().toString());
		}
	}
}
