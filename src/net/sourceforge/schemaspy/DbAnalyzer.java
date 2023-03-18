// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.ImpliedForeignKeyConstraint;
import net.sourceforge.schemaspy.model.RailsForeignKeyConstraint;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.model.TableIndex;
import net.sourceforge.schemaspy.util.Inflection;

public class DbAnalyzer
{
	public static List<ImpliedForeignKeyConstraint> getImpliedConstraints(final Collection<Table> collection)
	{
		final ArrayList<TableColumn> list = new ArrayList<TableColumn>();
		final TreeMap<TableColumn, Table> treeMap = new TreeMap<TableColumn, Table>(new Comparator<TableColumn>()
		{
			public int compare(final TableColumn tableColumn, final TableColumn tableColumn2)
			{
				int n = tableColumn.getName().compareToIgnoreCase(tableColumn2.getName());
				if (n == 0)
				{
					n = tableColumn.getType().compareToIgnoreCase(tableColumn2.getType());
				}
				if (n == 0)
				{
					n = tableColumn.getLength() - tableColumn2.getLength();
				}
				return n;
			}
		});
		int n = 0;
		for (final Table table : collection)
		{
			final List<TableColumn> primaryColumns = table.getPrimaryColumns();
			if (primaryColumns.size() == 1)
			{
				for (final TableColumn tableColumn : primaryColumns)
				{
					if (tableColumn.allowsImpliedChildren() && treeMap.put(tableColumn, table) != null)
					{
						++n;
					}
				}
			}
			for (final TableColumn tableColumn2 : table.getColumns())
			{
				if (!tableColumn2.isForeignKey() && tableColumn2.allowsImpliedParents())
				{
					list.add(tableColumn2);
				}
			}
		}
		if (n > treeMap.size())
		{
			return new ArrayList<ImpliedForeignKeyConstraint>();
		}
		sortColumnsByTable(list);
		final ArrayList<ImpliedForeignKeyConstraint> list2 = new ArrayList<ImpliedForeignKeyConstraint>();
		for (final TableColumn tableColumn3 : list)
		{
			final Table table2 = treeMap.get(tableColumn3);
			if (table2 != null && table2 != tableColumn3.getTable())
			{
				final TableColumn column = table2.getColumn(tableColumn3.getName());
				if (column.getParentConstraint(tableColumn3) != null)
				{
					continue;
				}
				list2.add(new ImpliedForeignKeyConstraint(column, tableColumn3));
			}
		}
		return list2;
	}

	public static List<RailsForeignKeyConstraint> getRailsConstraints(final Map<String, Table> map)
	{
		final ArrayList<RailsForeignKeyConstraint> list = new ArrayList<RailsForeignKeyConstraint>(map.size());
		final Iterator<Table> iterator = map.values().iterator();
		while (iterator.hasNext())
		{
			for (final TableColumn tableColumn : iterator.next().getColumns())
			{
				final String lowerCase = tableColumn.getName().toLowerCase();
				if (!tableColumn.isForeignKey() && tableColumn.allowsImpliedParents() && lowerCase.endsWith("_id"))
				{
					final Table table = map.get(Inflection.pluralize(lowerCase.substring(0, lowerCase.length() - 3)));
					if (table == null)
					{
						continue;
					}
					final TableColumn column = table.getColumn("ID");
					if (column == null)
					{
						continue;
					}
					list.add(new RailsForeignKeyConstraint(column, tableColumn));
				}
			}
		}
		return list;
	}

	public static List<ForeignKeyConstraint> getForeignKeyConstraints(final Collection<Table> collection)
	{
		final ArrayList<ForeignKeyConstraint> list = new ArrayList<ForeignKeyConstraint>();
		final Iterator<Table> iterator = collection.iterator();
		while (iterator.hasNext())
		{
			list.addAll(iterator.next().getForeignKeys());
		}
		return (List<ForeignKeyConstraint>) list;
	}

	public static List<Table> getOrphans(final Collection<Table> collection)
	{
		final ArrayList<Table> list = new ArrayList<Table>();
		for (final Table table : collection)
		{
			if (table.isOrphan(false))
			{
				list.add(table);
			}
		}
		return sortTablesByName(list);
	}

	public static List<TableColumn> getMustBeUniqueNullableColumns(final Collection<Table> collection)
	{
		final ArrayList<TableColumn> list = new ArrayList<TableColumn>();
		final Iterator<Table> iterator = collection.iterator();
		while (iterator.hasNext())
		{
			for (final TableIndex tableIndex : iterator.next().getIndexes())
			{
				if (tableIndex.isUniqueNullable())
				{
					list.addAll(tableIndex.getColumns());
				}
			}
		}
		return sortColumnsByTable(list);
	}

	public static List<Table> getTablesWithoutIndexes(final Collection<Table> collection)
	{
		final ArrayList<Table> list = new ArrayList<Table>();
		for (final Table table : collection)
		{
			if (!table.isView() && table.getIndexes().size() == 0)
			{
				list.add(table);
			}
		}
		return sortTablesByName(list);
	}

	public static List<Table> getTablesWithIncrementingColumnNames(final Collection<Table> collection)
	{
		final ArrayList<Table> list = new ArrayList<Table>();
		for (final Table table : collection)
		{
			final HashMap<String, Long> hashMap = new HashMap<String, Long>();
			final Iterator<TableColumn> iterator2 = table.getColumns().iterator();
			while (iterator2.hasNext())
			{
				String str = iterator2.next().getName();
				String string = null;
				for (int n = str.length() - 1; n > 0 && Character.isDigit(str.charAt(n)); --n)
				{
					string = String.valueOf(str.charAt(n)) + ((string == null) ? "" : string);
				}
				if (string == null)
				{
					string = "1";
					str += string;
				}
				final String substring = str.substring(0, str.length() - string.length());
				final long long1 = Long.parseLong(string);
				final Long n2 = hashMap.get(substring);
				if (n2 != null && Math.abs(n2 - long1) == 1L)
				{
					list.add(table);
					break;
				}
				hashMap.put(substring, long1);
			}
		}
		return sortTablesByName(list);
	}

	public static List<Table> getTablesWithOneColumn(final Collection<Table> collection)
	{
		final ArrayList<Table> list = new ArrayList<Table>();
		for (final Table table : collection)
		{
			if (table.getColumns().size() == 1)
			{
				list.add(table);
			}
		}
		return sortTablesByName(list);
	}

	public static List<Table> sortTablesByName(final List<Table> list)
	{
		Collections.sort(list, new Comparator<Table>()
		{
			public int compare(final Table table, final Table table2)
			{
				return table.compareTo(table2);
			}
		});
		return list;
	}

	public static List<TableColumn> sortColumnsByTable(final List<TableColumn> list)
	{
		Collections.sort(list, new Comparator<TableColumn>()
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
		});
		return list;
	}

	public static List<TableColumn> getDefaultNullStringColumns(final Collection<Table> collection)
	{
		final ArrayList<TableColumn> list = new ArrayList<TableColumn>();
		final Iterator<Table> iterator = collection.iterator();
		while (iterator.hasNext())
		{
			for (final TableColumn tableColumn : iterator.next().getColumns())
			{
				final Object defaultValue = tableColumn.getDefaultValue();
				if (
					defaultValue != null && defaultValue instanceof String
							&& defaultValue.toString().trim().equalsIgnoreCase("null")
				)
				{
					list.add(tableColumn);
				}
			}
		}
		return sortColumnsByTable(list);
	}

	public static List<String> getSchemas(final DatabaseMetaData databaseMetaData) throws SQLException
	{
		final ArrayList<String> list = new ArrayList<String>();
		final ResultSet schemas = databaseMetaData.getSchemas();
		while (schemas.next())
		{
			list.add(schemas.getString("TABLE_SCHEM"));
		}
		schemas.close();
		return list;
	}

	public static List<String> getPopulatedSchemas(final DatabaseMetaData databaseMetaData) throws SQLException
	{
		return getPopulatedSchemas(databaseMetaData, ".*");
	}

	public static List<String> getPopulatedSchemas(final DatabaseMetaData databaseMetaData, final String regex)
			throws SQLException
	{
		final TreeSet<String> c = new TreeSet<String>();
		final Pattern compile = Pattern.compile(regex);
		final Logger logger = Logger.getLogger(DbAnalyzer.class.getName());
		final boolean loggable = logger.isLoggable(Level.FINE);
		final Iterator<String> iterator = getSchemas(databaseMetaData).iterator();
		while (iterator.hasNext())
		{
			final String string = iterator.next().toString();
			if (compile.matcher(string).matches())
			{
				ResultSet tables = null;
				try
				{
					tables = databaseMetaData.getTables(null, string, "%", null);
					if (tables.next())
					{
						if (loggable)
						{
							logger.fine(
									"Including schema " + string + ": matches + \"" + compile + "\" and contains tables"
							);
						}
						c.add(string);
					} else
					{
						if (!loggable)
						{
							continue;
						}
						logger.fine(
								"Excluding schema " + string + ": matches \"" + compile + "\" but contains no tables"
						);
					}
				}
				catch (SQLException ex)
				{
				}
				finally
				{
					if (tables != null)
					{
						tables.close();
					}
				}
			} else
			{
				if (!loggable)
				{
					continue;
				}
				logger.fine("Excluding schema " + string + ": doesn't match \"" + compile + '\"');
			}
		}
		return new ArrayList<String>(c);
	}

	public static void dumpResultSetRow(final ResultSet set, final String str) throws SQLException
	{
		final ResultSetMetaData metaData = set.getMetaData();
		final int columnCount = metaData.getColumnCount();
		System.out.println(columnCount + " columns of " + str + ":");
		for (int i = 1; i <= columnCount; ++i)
		{
			System.out.print(metaData.getColumnLabel(i));
			System.out.print(": ");
			System.out.print(String.valueOf(set.getString(i)));
			System.out.print("\t");
		}
		System.out.println();
	}
}
