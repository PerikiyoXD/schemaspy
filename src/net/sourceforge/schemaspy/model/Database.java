// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.model.xml.SchemaMeta;
import net.sourceforge.schemaspy.model.xml.TableMeta;
import net.sourceforge.schemaspy.util.CaseInsensitiveMap;

public class Database
{
	private final String databaseName;
	private final String schema;
	private String description;
	private final Map<String, Table> tables;
	private final Map<String, View> views;
	private final Map<String, Table> remoteTables;
	private final DatabaseMetaData meta;
	private final Connection connection;
	private final String connectTime;
	private Set<String> sqlKeywords;
	private Pattern invalidIdentifierPattern;
	private final Logger logger;
	private final boolean fineEnabled;

	public Database(
			final Config config, final Connection connection, final DatabaseMetaData meta, final String databaseName,
			final String schema, final Properties properties, final SchemaMeta schemaMeta
	) throws SQLException, MissingResourceException
	{
		this.tables = new CaseInsensitiveMap<Table>();
		this.views = new CaseInsensitiveMap<View>();
		this.remoteTables = new CaseInsensitiveMap<Table>();
		this.connectTime = new SimpleDateFormat("EEE MMM dd HH:mm z yyyy").format(new Date());
		this.logger = Logger.getLogger(this.getClass().getName());
		this.fineEnabled = this.logger.isLoggable(Level.FINE);
		this.connection = connection;
		this.meta = meta;
		this.databaseName = databaseName;
		this.schema = schema;
		this.description = config.getDescription();
		this.initTables(meta, properties, config);
		if (config.isViewsEnabled())
		{
			this.initViews(meta, properties, config);
		}
		this.initCheckConstraints(properties);
		this.initTableIds(properties);
		this.initIndexIds(properties);
		this.initTableComments(properties);
		this.initTableColumnComments(properties);
		this.initViewComments(properties);
		this.initViewColumnComments(properties);
		this.connectTables();
		this.updateFromXmlMetadata(schemaMeta);
	}

	public String getName()
	{
		return this.databaseName;
	}

	public String getSchema()
	{
		return this.schema;
	}

	public String getDescription()
	{
		return this.description;
	}

	public Collection<Table> getTables()
	{
		return this.tables.values();
	}

	public Map<String, Table> getTablesByName()
	{
		return this.tables;
	}

	public Collection<View> getViews()
	{
		return this.views.values();
	}

	public Collection<Table> getRemoteTables()
	{
		return this.remoteTables.values();
	}

	public Connection getConnection()
	{
		return this.connection;
	}

	public DatabaseMetaData getMetaData()
	{
		return this.meta;
	}

	public String getConnectTime()
	{
		return this.connectTime;
	}

	public String getDatabaseProduct()
	{
		try
		{
			return this.meta.getDatabaseProductName() + " - " + this.meta.getDatabaseProductVersion();
		}
		catch (SQLException ex)
		{
			return "";
		}
	}

	private void initTables(final DatabaseMetaData databaseMetaData, final Properties properties, final Config config)
			throws SQLException
	{
		final Pattern tableInclusions = config.getTableInclusions();
		final Pattern tableExclusions = config.getTableExclusions();
		final int maxDbThreads = config.getMaxDbThreads();
		final String[] types = this.getTypes("tableTypes", "TABLE", properties);
		final NameValidator nameValidator = new NameValidator("table", tableInclusions, tableExclusions, types);
		final List<BasicTableMeta> basicTableMeta = this.getBasicTableMeta(databaseMetaData, true, properties, types);
		TableCreator tableCreator;
		if (maxDbThreads == 1)
		{
			tableCreator = new TableCreator();
		} else
		{
			tableCreator = new ThreadedTableCreator(maxDbThreads);
			while (!basicTableMeta.isEmpty())
			{
				final BasicTableMeta basicTableMeta2 = basicTableMeta.remove(0);
				if (nameValidator.isValid(basicTableMeta2.name, basicTableMeta2.type))
				{
					new TableCreator().create(basicTableMeta2, properties);
					break;
				}
			}
		}
		for (final BasicTableMeta basicTableMeta3 : basicTableMeta)
		{
			if (nameValidator.isValid(basicTableMeta3.name, basicTableMeta3.type))
			{
				tableCreator.create(basicTableMeta3, properties);
			}
		}
		tableCreator.join();
	}

	private void initViews(final DatabaseMetaData databaseMetaData, final Properties properties, final Config config)
			throws SQLException
	{
		final Pattern tableInclusions = config.getTableInclusions();
		final Pattern tableExclusions = config.getTableExclusions();
		final Pattern columnExclusions = config.getColumnExclusions();
		final Pattern indirectColumnExclusions = config.getIndirectColumnExclusions();
		final String[] types = this.getTypes("viewTypes", "VIEW", properties);
		final NameValidator nameValidator = new NameValidator("view", tableInclusions, tableExclusions, types);
		for (final BasicTableMeta basicTableMeta : this.getBasicTableMeta(databaseMetaData, false, properties, types))
		{
			if (nameValidator.isValid(basicTableMeta.name, basicTableMeta.type))
			{
				final View view = new View(
						this, basicTableMeta.schema, basicTableMeta.name, basicTableMeta.remarks,
						basicTableMeta.viewSql, properties, indirectColumnExclusions, columnExclusions
				);
				this.views.put(view.getName(), view);
				if (this.logger.isLoggable(Level.FINE))
				{
					this.logger.fine("Found details of view " + view.getName());
				} else
				{
					System.out.print('.');
				}
			}
		}
	}

	private List<BasicTableMeta> getBasicTableMeta(
			final DatabaseMetaData databaseMetaData, final boolean b, final Properties properties, final String... array
	) throws SQLException
	{
		final String property = properties.getProperty(b ? "selectTablesSql" : "selectViewsSql");
		final ArrayList<BasicTableMeta> list = new ArrayList<BasicTableMeta>();
		ResultSet executeQuery = null;
		if (property != null)
		{
			final String s = b ? "table" : "view";
			PreparedStatement prepareStatement = null;
			try
			{
				prepareStatement = this.prepareStatement(property, null);
				executeQuery = prepareStatement.executeQuery();
				while (executeQuery.next())
				{
					final String string = executeQuery.getString(s + "_name");
					String s2 = this.getOptionalString(executeQuery, s + "_schema");
					if (s2 == null)
					{
						s2 = this.schema;
					}
					final String optionalString = this.getOptionalString(executeQuery, s + "_comment");
					final String s3 = b ? null : this.getOptionalString(executeQuery, "view_definition");
					final String s4 = b ? this.getOptionalString(executeQuery, "table_rows") : null;
					list.add(
							new BasicTableMeta(
									s2, string, s, optionalString, s3, (s4 == null) ? -1 : Integer.parseInt(s4)
							)
					);
				}
			}
			catch (SQLException obj)
			{
				System.out.flush();
				System.err.println();
				System.err.println("Failed to retrieve " + s + " names with custom SQL: " + obj);
				System.err.println(property);
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
		if (list.isEmpty())
		{
			final ResultSet tables = databaseMetaData.getTables(null, this.schema, "%", array);
			try
			{
				while (tables.next())
				{
					list.add(
							new BasicTableMeta(
									tables.getString("TABLE_SCHEM"), tables.getString("TABLE_NAME"),
									tables.getString("TABLE_TYPE"), this.getOptionalString(tables, "REMARKS"), null, -1
							)
					);
				}
			}
			catch (SQLException ex)
			{
				if (b)
				{
					throw ex;
				}
				System.out.flush();
				System.err.println();
				System.err.println("Ignoring view " + tables.getString("TABLE_NAME") + " due to exception:");
				ex.printStackTrace();
				System.err.println("Continuing analysis.");
			}
			finally
			{
				if (tables != null)
				{
					tables.close();
				}
			}
		}
		return list;
	}

	private String[] getTypes(final String key, final String defaultValue, final Properties properties)
	{
		final String property = properties.getProperty(key, defaultValue);
		final ArrayList<String> list = new ArrayList<String>();
		final String[] split = property.split(",");
		for (int length = split.length, i = 0; i < length; ++i)
		{
			final String trim = split[i].trim();
			if (trim.length() > 0)
			{
				list.add(trim);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	public String getOptionalString(final ResultSet set, final String s)
	{
		try
		{
			return set.getString(s);
		}
		catch (SQLException ex)
		{
			return null;
		}
	}

	private void initCheckConstraints(final Properties properties) throws SQLException
	{
		final String property = properties.getProperty("selectCheckConstraintsSql");
		if (property != null)
		{
			PreparedStatement prepareStatement = null;
			ResultSet executeQuery = null;
			try
			{
				prepareStatement = this.prepareStatement(property, null);
				executeQuery = prepareStatement.executeQuery();
				while (executeQuery.next())
				{
					final Table table = this.tables.get(executeQuery.getString("table_name"));
					if (table != null)
					{
						table.addCheckConstraint(
								executeQuery.getString("constraint_name"), executeQuery.getString("text")
						);
					}
				}
			}
			catch (SQLException obj)
			{
				System.err.println();
				System.err.println("Failed to retrieve check constraints: " + obj);
				System.err.println(property);
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
	}

	private void initTableIds(final Properties properties) throws SQLException
	{
		final String property = properties.getProperty("selectTableIdsSql");
		if (property != null)
		{
			PreparedStatement prepareStatement = null;
			ResultSet executeQuery = null;
			try
			{
				prepareStatement = this.prepareStatement(property, null);
				executeQuery = prepareStatement.executeQuery();
				while (executeQuery.next())
				{
					final Table table = this.tables.get(executeQuery.getString("table_name"));
					if (table != null)
					{
						table.setId(executeQuery.getObject("table_id"));
					}
				}
			}
			catch (SQLException ex)
			{
				System.err.println();
				System.err.println(property);
				throw ex;
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
	}

	private void initIndexIds(final Properties properties) throws SQLException
	{
		final String property = properties.getProperty("selectIndexIdsSql");
		if (property != null)
		{
			PreparedStatement prepareStatement = null;
			ResultSet executeQuery = null;
			try
			{
				prepareStatement = this.prepareStatement(property, null);
				executeQuery = prepareStatement.executeQuery();
				while (executeQuery.next())
				{
					final Table table = this.tables.get(executeQuery.getString("table_name"));
					if (table != null)
					{
						final TableIndex index = table.getIndex(executeQuery.getString("index_name"));
						if (index == null)
						{
							continue;
						}
						index.setId(executeQuery.getObject("index_id"));
					}
				}
			}
			catch (SQLException ex)
			{
				System.err.println();
				System.err.println(property);
				throw ex;
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
	}

	private void initTableComments(final Properties properties) throws SQLException
	{
		final String property = properties.getProperty("selectTableCommentsSql");
		if (property != null)
		{
			PreparedStatement prepareStatement = null;
			ResultSet executeQuery = null;
			try
			{
				prepareStatement = this.prepareStatement(property, null);
				executeQuery = prepareStatement.executeQuery();
				while (executeQuery.next())
				{
					final String string = executeQuery.getString("table_name");
					Table table = this.tables.get(string);
					if (table == null)
					{
						table = this.views.get(string);
					}
					if (table != null)
					{
						table.setComments(executeQuery.getString("comments"));
					}
				}
			}
			catch (SQLException obj)
			{
				System.err.println();
				System.err.println("Failed to retrieve table/view comments: " + obj);
				System.err.println(property);
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
	}

	private void initViewComments(final Properties properties) throws SQLException
	{
		final String property = properties.getProperty("selectViewCommentsSql");
		if (property != null)
		{
			PreparedStatement prepareStatement = null;
			ResultSet executeQuery = null;
			try
			{
				prepareStatement = this.prepareStatement(property, null);
				executeQuery = prepareStatement.executeQuery();
				while (executeQuery.next())
				{
					String s = executeQuery.getString("view_name");
					if (s == null)
					{
						s = executeQuery.getString("table_name");
					}
					final View view = this.views.get(s);
					if (view != null)
					{
						view.setComments(executeQuery.getString("comments"));
					}
				}
			}
			catch (SQLException obj)
			{
				System.err.println();
				System.err.println("Failed to retrieve table/view comments: " + obj);
				System.err.println(property);
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
	}

	private void initTableColumnComments(final Properties properties) throws SQLException
	{
		final String property = properties.getProperty("selectColumnCommentsSql");
		if (property != null)
		{
			PreparedStatement prepareStatement = null;
			ResultSet executeQuery = null;
			try
			{
				prepareStatement = this.prepareStatement(property, null);
				executeQuery = prepareStatement.executeQuery();
				while (executeQuery.next())
				{
					final String string = executeQuery.getString("table_name");
					Table table = this.tables.get(string);
					if (table == null)
					{
						table = this.views.get(string);
					}
					if (table != null)
					{
						final TableColumn column = table.getColumn(executeQuery.getString("column_name"));
						if (column == null)
						{
							continue;
						}
						column.setComments(executeQuery.getString("comments"));
					}
				}
			}
			catch (SQLException obj)
			{
				System.err.println();
				System.err.println("Failed to retrieve column comments: " + obj);
				System.err.println(property);
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
	}

	private void initViewColumnComments(final Properties properties) throws SQLException
	{
		final String property = properties.getProperty("selectViewColumnCommentsSql");
		if (property != null)
		{
			PreparedStatement prepareStatement = null;
			ResultSet executeQuery = null;
			try
			{
				prepareStatement = this.prepareStatement(property, null);
				executeQuery = prepareStatement.executeQuery();
				while (executeQuery.next())
				{
					String s = executeQuery.getString("view_name");
					if (s == null)
					{
						s = executeQuery.getString("table_name");
					}
					final View view = this.views.get(s);
					if (view != null)
					{
						final TableColumn column = view.getColumn(executeQuery.getString("column_name"));
						if (column == null)
						{
							continue;
						}
						column.setComments(executeQuery.getString("comments"));
					}
				}
			}
			catch (SQLException obj)
			{
				System.err.println();
				System.err.println("Failed to retrieve view column comments: " + obj);
				System.err.println(property);
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
	}

	public PreparedStatement prepareStatement(final String str, final String s) throws SQLException
	{
		final StringBuilder sb = new StringBuilder(str);
		final List<String> sqlParams = this.getSqlParams(sb, s);
		final PreparedStatement prepareStatement = this.getConnection().prepareStatement(sb.toString());
		try
		{
			for (int i = 0; i < sqlParams.size(); ++i)
			{
				prepareStatement.setString(i + 1, sqlParams.get(i).toString());
			}
		}
		catch (SQLException ex)
		{
			prepareStatement.close();
			throw ex;
		}
		return prepareStatement;
	}

	public Table addRemoteTable(
			final String str, final String str2, final String s, final Properties properties, final Pattern pattern,
			final Pattern pattern2
	) throws SQLException
	{
		final String string = str + "." + str2;
		Table table = this.remoteTables.get(string);
		if (table == null)
		{
			if (properties != null)
			{
				table = new RemoteTable(this, str, str2, s, properties, pattern, pattern2);
			} else
			{
				table = new ExplicitRemoteTable(this, str, str2, s);
			}
			this.logger.fine("Adding remote table " + string);
			table.connectForeignKeys(this.tables, pattern, pattern2);
			this.remoteTables.put(string, table);
		}
		return table;
	}

	public Set<String> getSqlKeywords() throws SQLException
	{
		if (this.sqlKeywords == null)
		{
			final String[] split = "ADA| C | CATALOG_NAME | CHARACTER_SET_CATALOG | CHARACTER_SET_NAME| CHARACTER_SET_SCHEMA | CLASS_ORIGIN | COBOL | COLLATION_CATALOG| COLLATION_NAME | COLLATION_SCHEMA | COLUMN_NAME | COMMAND_FUNCTION | COMMITTED| CONDITION_NUMBER | CONNECTION_NAME | CONSTRAINT_CATALOG | CONSTRAINT_NAME| CONSTRAINT_SCHEMA | CURSOR_NAME| DATA | DATETIME_INTERVAL_CODE | DATETIME_INTERVAL_PRECISION | DYNAMIC_FUNCTION| FORTRAN| LENGTH| MESSAGE_LENGTH | MESSAGE_OCTET_LENGTH | MESSAGE_TEXT | MORE | MUMPS| NAME | NULLABLE | NUMBER| PASCAL | PLI| REPEATABLE | RETURNED_LENGTH | RETURNED_OCTET_LENGTH | RETURNED_SQLSTATE| ROW_COUNT| SCALE | SCHEMA_NAME | SERIALIZABLE | SERVER_NAME | SUBCLASS_ORIGIN| TABLE_NAME | TYPE| UNCOMMITTED | UNNAMED| ABSOLUTE | ACTION | ADD | ALL | ALLOCATE | ALTER | AND| ANY | ARE | AS | ASC| ASSERTION | AT | AUTHORIZATION | AVG| BEGIN | BETWEEN | BIT | BIT_LENGTH | BOTH | BY| CASCADE | CASCADED | CASE | CAST | CATALOG | CHAR | CHARACTER | CHAR_LENGTH| CHARACTER_LENGTH | CHECK | CLOSE | COALESCE | COLLATE | COLLATION| COLUMN | COMMIT | CONNECT | CONNECTION | CONSTRAINT| CONSTRAINTS | CONTINUE| CONVERT | CORRESPONDING | COUNT | CREATE | CROSS | CURRENT| CURRENT_DATE | CURRENT_TIME | CURRENT_TIMESTAMP | CURRENT_USER | CURSOR| DATE | DAY | DEALLOCATE | DEC | DECIMAL | DECLARE | DEFAULT | DEFERRABLE| DEFERRED | DELETE | DESC | DESCRIBE | DESCRIPTOR | DIAGNOSTICS| DISCONNECT | DISTINCT | DOMAIN | DOUBLE | DROP| ELSE | END | END-EXEC | ESCAPE | EXCEPT | EXCEPTION| EXEC | EXECUTE | EXISTS| EXTERNAL | EXTRACT| FALSE | FETCH | FIRST | FLOAT | FOR | FOREIGN | FOUND | FROM | FULL| GET | GLOBAL | GO | GOTO | GRANT | GROUP| HAVING | HOUR| IDENTITY | IMMEDIATE | IN | INDICATOR | INITIALLY | INNER | INPUT| INSENSITIVE | INSERT | INT | INTEGER | INTERSECT | INTERVAL | INTO | IS| ISOLATION| JOIN| KEY| LANGUAGE | LAST | LEADING | LEFT | LEVEL | LIKE | LOCAL | LOWER| MATCH | MAX | MIN | MINUTE | MODULE | MONTH| NAMES | NATIONAL | NATURAL | NCHAR | NEXT | NO | NOT | NULL| NULLIF | NUMERIC| OCTET_LENGTH | OF | ON | ONLY | OPEN | OPTION | OR| ORDER | OUTER| OUTPUT | OVERLAPS| PAD | PARTIAL | POSITION | PRECISION | PREPARE | PRESERVE | PRIMARY| PRIOR | PRIVILEGES | PROCEDURE | PUBLIC| READ | REAL | REFERENCES | RELATIVE | RESTRICT | REVOKE | RIGHT| ROLLBACK | ROWS| SCHEMA | SCROLL | SECOND | SECTION | SELECT | SESSION | SESSION_USER | SET| SIZE | SMALLINT | SOME | SPACE | SQL | SQLCODE | SQLERROR | SQLSTATE| SUBSTRING | SUM | SYSTEM_USER| TABLE | TEMPORARY | THEN | TIME | TIMESTAMP | TIMEZONE_HOUR | TIMEZONE_MINUTE| TO | TRAILING | TRANSACTION | TRANSLATE | TRANSLATION | TRIM | TRUE| UNION | UNIQUE | UNKNOWN | UPDATE | UPPER | USAGE | USER | USING| VALUE | VALUES | VARCHAR | VARYING | VIEW| WHEN | WHENEVER | WHERE | WITH | WORK | WRITE| YEAR| ZONE"
					.split("|,\\s*");
			final String[] split2 = this.getMetaData().getSQLKeywords().toUpperCase().split(",\\s*");
			(this.sqlKeywords = new HashSet<String>()).addAll(Arrays.asList(split));
			this.sqlKeywords.addAll(Arrays.asList(split2));
		}
		return this.sqlKeywords;
	}

	public String getQuotedIdentifier(final String s) throws SQLException
	{
		if (this.getInvalidIdentifierPattern().matcher(s).find() || this.getSqlKeywords().contains(s.toUpperCase()))
		{
			final String trim = this.getMetaData().getIdentifierQuoteString().trim();
			return trim + s + trim;
		}
		return s;
	}

	private Pattern getInvalidIdentifierPattern() throws SQLException
	{
		if (this.invalidIdentifierPattern == null)
		{
			String str = "a-zA-Z0-9_";
			final String s = "-&^";
			final String extraNameCharacters = this.getMetaData().getExtraNameCharacters();
			for (int i = 0; i < extraNameCharacters.length(); ++i)
			{
				final char char1 = extraNameCharacters.charAt(i);
				if (s.indexOf(char1) >= 0)
				{
					str += "\\";
				}
				str += char1;
			}
			this.invalidIdentifierPattern = Pattern.compile("[^" + str + "]");
		}
		return this.invalidIdentifierPattern;
	}

	private List<String> getSqlParams(final StringBuilder obj, final String s)
	{
		final HashMap<Object, String> hashMap = new HashMap<Object, String>();
		String s2 = this.getSchema();
		if (s2 == null)
		{
			s2 = this.getName();
		}
		hashMap.put(":schema", s2);
		hashMap.put(":owner", s2);
		if (s != null)
		{
			hashMap.put(":table", s);
			hashMap.put(":view", s);
		}
		final ArrayList<String> list = new ArrayList<String>();
		for (int i = obj.indexOf(":"); i != -1; i = obj.indexOf(":", i))
		{
			final String nextToken = new StringTokenizer(obj.substring(i), " ,\"')").nextToken();
			final String s3 = hashMap.get(nextToken);
			if (s3 == null)
			{
				throw new InvalidConfigurationException(
						"Unexpected named parameter '" + nextToken + "' found in SQL '" + (Object) obj + "'"
				);
			}
			list.add(s3);
			obj.replace(i, i + nextToken.length(), "?");
		}
		return list;
	}

	private void updateFromXmlMetadata(final SchemaMeta schemaMeta) throws SQLException
	{
		if (schemaMeta != null)
		{
			final Pattern compile = Pattern.compile("[^.]");
			final Properties properties = new Properties();
			this.description = schemaMeta.getComments();
			for (final TableMeta tableMeta : schemaMeta.getTables())
			{
				Table addRemoteTable;
				if (tableMeta.getRemoteSchema() != null)
				{
					addRemoteTable = this.remoteTables.get(tableMeta.getRemoteSchema() + '.' + tableMeta.getName());
					if (addRemoteTable == null)
					{
						addRemoteTable = this.addRemoteTable(
								tableMeta.getRemoteSchema(), tableMeta.getName(), this.getSchema(), null, compile,
								compile
						);
					}
				} else
				{
					addRemoteTable = this.tables.get(tableMeta.getName());
					if (addRemoteTable == null)
					{
						addRemoteTable = this.views.get(tableMeta.getName());
					}
					if (addRemoteTable == null)
					{
						addRemoteTable = new Table(
								this, this.getSchema(), tableMeta.getName(), null, properties, compile, compile
						);
						this.tables.put(addRemoteTable.getName(), addRemoteTable);
					}
				}
				addRemoteTable.update(tableMeta);
			}
			for (final TableMeta tableMeta2 : schemaMeta.getTables())
			{
				Table table;
				if (tableMeta2.getRemoteSchema() != null)
				{
					table = this.remoteTables.get(tableMeta2.getRemoteSchema() + '.' + tableMeta2.getName());
				} else
				{
					table = this.tables.get(tableMeta2.getName());
					if (table == null)
					{
						table = this.views.get(tableMeta2.getName());
					}
				}
				table.connect(tableMeta2, this.tables, this.remoteTables);
			}
		}
	}

	private void connectTables() throws SQLException
	{
		final Pattern columnExclusions = Config.getInstance().getColumnExclusions();
		final Pattern indirectColumnExclusions = Config.getInstance().getIndirectColumnExclusions();
		final Iterator<Table> iterator = this.tables.values().iterator();
		while (iterator.hasNext())
		{
			iterator.next().connectForeignKeys(this.tables, indirectColumnExclusions, columnExclusions);
		}
	}

	class NameValidator
	{
		private final String clazz;
		private final Pattern include;
		private final Pattern exclude;
		private final Set<String> validTypes;

		NameValidator(final String clazz, final Pattern include, final Pattern exclude, final String[] array)
		{
			this.clazz = clazz;
			this.include = include;
			this.exclude = exclude;
			this.validTypes = new HashSet<String>();
			for (int length = array.length, i = 0; i < length; ++i)
			{
				this.validTypes.add(array[i].toUpperCase());
			}
		}

		boolean isValid(final String s, final String s2)
		{
			if (!this.validTypes.contains(s2.toUpperCase()))
			{
				return false;
			}
			if (s.indexOf("$") != -1)
			{
				if (Database.this.fineEnabled)
				{
					Database.this.logger
							.fine("Excluding " + this.clazz + " " + s + ": embedded $ implies illegal name");
				}
				return false;
			}
			if (this.exclude.matcher(s).matches())
			{
				if (Database.this.fineEnabled)
				{
					Database.this.logger.fine(
							"Excluding " + this.clazz + " " + s + ": matches exclusion pattern \"" + this.exclude + '\"'
					);
				}
				return false;
			}
			final boolean matches = this.include.matcher(s).matches();
			if (Database.this.fineEnabled)
			{
				if (matches)
				{
					Database.this.logger.fine(
							"Including " + this.clazz + " " + s + ": matches inclusion pattern \"" + this.include + '\"'
					);
				} else
				{
					Database.this.logger.fine(
							"Excluding " + this.clazz + " " + s + ": doesn't match inclusion pattern \"" + this.include
									+ '\"'
					);
				}
			}
			return matches;
		}
	}

	private class BasicTableMeta
	{
		final String schema;
		final String name;
		final String type;
		final String remarks;
		final String viewSql;
		final int numRows;

		BasicTableMeta(
				final String schema, final String name, final String type, final String remarks, final String viewSql,
				final int numRows
		)
		{
			this.schema = schema;
			this.name = name;
			this.type = type;
			this.remarks = remarks;
			this.viewSql = viewSql;
			this.numRows = numRows;
		}
	}

	private class TableCreator
	{
		private final Pattern excludeColumns;
		private final Pattern excludeIndirectColumns;

		private TableCreator()
		{
			this.excludeColumns = Config.getInstance().getColumnExclusions();
			this.excludeIndirectColumns = Config.getInstance().getIndirectColumnExclusions();
		}

		void create(final BasicTableMeta basicTableMeta, final Properties properties) throws SQLException
		{
			this.createImpl(basicTableMeta, properties);
		}

		protected void createImpl(final BasicTableMeta basicTableMeta, final Properties properties) throws SQLException
		{
			final Table table = new Table(
					Database.this, basicTableMeta.schema, basicTableMeta.name, basicTableMeta.remarks, properties,
					this.excludeIndirectColumns, this.excludeColumns
			);
			if (basicTableMeta.numRows != -1)
			{
				table.setNumRows(basicTableMeta.numRows);
			}
			synchronized (Database.this.tables)
			{
				Database.this.tables.put(table.getName(), table);
			}
			if (Database.this.logger.isLoggable(Level.FINE))
			{
				Database.this.logger.fine("Found details of table " + table.getName());
			} else
			{
				System.out.print('.');
			}
		}

		void join()
		{
		}
	}

	private class ThreadedTableCreator extends TableCreator
	{
		private final Set<Thread> threads;
		private final int maxThreads;

		ThreadedTableCreator(final int maxThreads)
		{
			this.threads = new HashSet<Thread>();
			this.maxThreads = maxThreads;
		}

		@Override
		void create(final BasicTableMeta basicTableMeta, final Properties properties) throws SQLException
		{
			final Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					try
					{
						ThreadedTableCreator.this.createImpl(basicTableMeta, properties);
					}
					catch (SQLException ex)
					{
						ex.printStackTrace();
						synchronized (ThreadedTableCreator.this.threads)
						{
							ThreadedTableCreator.this.threads.remove(this);
							ThreadedTableCreator.this.threads.notify();
						}
					}
					finally
					{
						synchronized (ThreadedTableCreator.this.threads)
						{
							ThreadedTableCreator.this.threads.remove(this);
							ThreadedTableCreator.this.threads.notify();
						}
					}
				}
			};
			synchronized (this.threads)
			{
				while (this.threads.size() >= this.maxThreads)
				{
					try
					{
						this.threads.wait();
					}
					catch (InterruptedException ex)
					{
					}
				}
				this.threads.add(thread);
			}
			thread.start();
		}

		public void join()
		{
			while (true)
			{
				final Thread thread;
				synchronized (this.threads)
				{
					final Iterator<Thread> iterator = this.threads.iterator();
					if (!iterator.hasNext())
					{
						break;
					}
					thread = iterator.next();
				}
				try
				{
					thread.join();
				}
				catch (InterruptedException ex)
				{
				}
			}
		}
	}
}
