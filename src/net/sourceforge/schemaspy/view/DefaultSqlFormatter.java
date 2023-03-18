// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.util.CaseInsensitiveMap;
import net.sourceforge.schemaspy.util.HtmlEncoder;

public class DefaultSqlFormatter implements SqlFormatter
{
	private Set<String> keywords;
	private Map<String, Table> tablesByPossibleNames;
	private static String TOKENS;

	public String format(final String str, final Database database, final Set<Table> set)
	{
		final StringBuilder sb = new StringBuilder(str.length() * 2);
		if (str.contains("\n") || str.contains("\r"))
		{
			sb.append("<div class='viewDefinition preFormatted'>");
			for (int length = str.length(), i = 0; i < length; ++i)
			{
				final char char1 = str.charAt(i);
				if (Character.isWhitespace(char1))
				{
					sb.append(char1);
				} else
				{
					sb.append(HtmlEncoder.encodeToken(char1));
				}
			}
		} else
		{
			sb.append("  <div class='viewDefinition'>");
			final Set<String> keywords = this.getKeywords(database.getMetaData());
			final StringTokenizer stringTokenizer = new StringTokenizer(str, DefaultSqlFormatter.TOKENS, true);
			while (stringTokenizer.hasMoreTokens())
			{
				final String nextToken = stringTokenizer.nextToken();
				if (keywords.contains(nextToken.toUpperCase()))
				{
					sb.append("<b>");
					sb.append(nextToken);
					sb.append("</b>");
				} else
				{
					sb.append(HtmlEncoder.encodeToken(nextToken));
				}
			}
		}
		sb.append("</div>");
		set.addAll(this.getReferencedTables(str, database));
		return sb.toString();
	}

	protected Set<Table> getReferencedTables(final String str, final Database database)
	{
		final HashSet<Table> set = new HashSet<Table>();
		final Map<String, Table> tableMap = this.getTableMap(database);
		final Set<String> keywords = this.getKeywords(database.getMetaData());
		final StringTokenizer stringTokenizer = new StringTokenizer(str, DefaultSqlFormatter.TOKENS, true);
		while (stringTokenizer.hasMoreTokens())
		{
			final String nextToken = stringTokenizer.nextToken();
			if (!keywords.contains(nextToken.toUpperCase()))
			{
				Table table = tableMap.get(nextToken);
				if (table == null)
				{
					final int lastIndex = nextToken.lastIndexOf(46);
					if (lastIndex != -1)
					{
						table = tableMap.get(nextToken.substring(0, lastIndex));
					}
				}
				if (table == null)
				{
					continue;
				}
				set.add(table);
			}
		}
		return set;
	}

	protected Map<String, Table> getTableMap(final Database database)
	{
		if (this.tablesByPossibleNames == null)
		{
			(this.tablesByPossibleNames = new CaseInsensitiveMap<Table>())
					.putAll(this.getTableMap(database.getTables(), database.getName()));
			this.tablesByPossibleNames.putAll(this.getTableMap(database.getViews(), database.getName()));
		}
		return this.tablesByPossibleNames;
	}

	protected Map<String, Table> getTableMap(final Collection<? extends Table> collection, final String s)
	{
		final CaseInsensitiveMap<Table> caseInsensitiveMap = new CaseInsensitiveMap<Table>();
		for (final Table table : collection)
		{
			final String name = table.getName();
			String schema = table.getSchema();
			if (schema == null)
			{
				schema = s;
			}
			caseInsensitiveMap.put(name, table);
			caseInsensitiveMap.put("`" + name + "`", table);
			caseInsensitiveMap.put("'" + name + "'", table);
			caseInsensitiveMap.put("\"" + name + "\"", table);
			caseInsensitiveMap.put(schema + "." + name, table);
			caseInsensitiveMap.put("`" + schema + "`.`" + name + "`", table);
			caseInsensitiveMap.put("'" + schema + "'.'" + name + "'", table);
			caseInsensitiveMap.put("\"" + schema + "\".\"" + name + "\"", table);
			caseInsensitiveMap.put("`" + schema + '.' + name + "`", table);
			caseInsensitiveMap.put("'" + schema + '.' + name + "'", table);
			caseInsensitiveMap.put("\"" + schema + '.' + name + "\"", table);
		}
		return (Map<String, Table>) caseInsensitiveMap;
	}

	public Set<String> getKeywords(final DatabaseMetaData databaseMetaData)
	{
		if (this.keywords == null)
		{
			this.keywords = new HashSet<String>(
					Arrays.asList(
							"ABSOLUTE", "ACTION", "ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS", "ASC",
							"ASSERTION", "AT", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIT", "BIT_LENGTH", "BOTH",
							"BY", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHAR_LENGTH",
							"CHARACTER_LENGTH", "CHECK", "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN",
							"COMMIT", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT",
							"CORRESPONDING", "COUNT", "CREATE", "CROSS", "CURRENT", "CURRENT_DATE", "CURRENT_TIME",
							"CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATE", "DAY", "DEALLOCATE", "DEC",
							"DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DESCRIBE",
							"DESCRIPTOR", "DIAGNOSTICS", "DISCONNECT", "DISTINCT", "DOMAIN", "DOUBLE", "DROP", "ELSE",
							"END", "END - EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS",
							"EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND",
							"FROM", "FULL", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "HAVING", "HOUR",
							"IDENTITY", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INPUT", "INSENSITIVE",
							"INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION", "JOIN",
							"KEY", "LANGUAGE", "LAST", "LEADING", "LEFT", "LEVEL", "LIKE", "LOCAL", "LOWER", "MATCH",
							"MAX", "MIN", "MINUTE", "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NEXT",
							"NO", "NOT", "NULL", "NULLIF", "NUMERIC", "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN",
							"OPTION", "OR", "ORDER", "OUTER", "OUTPUT", "OVERLAPS", "PAD", "PARTIAL", "POSITION",
							"PRECISION", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC",
							"READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", "ROLLBACK", "ROWS",
							"SCHEMA", "SCROLL", "SECOND", "SECTION", "SELECT", "SESSION", "SESSION_USER", "SET", "SIZE",
							"SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE", "SUBSTRING", "SUM",
							"SYSTEM_USER", "TABLE", "TEMPORARY", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR",
							"TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION", "TRANSLATE", "TRANSLATION", "TRIM",
							"TRUE", "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE",
							"VALUES", "VARCHAR", "VARYING", "VIEW", "WHEN", "WHENEVER", "WHERE", "WITH", "WORK",
							"WRITE", "YEAR", "ZONE"
					)
			);
			try
			{
				final String[] array =
				{ databaseMetaData.getSQLKeywords(), databaseMetaData.getSystemFunctions(),
						databaseMetaData.getNumericFunctions(), databaseMetaData.getStringFunctions(),
						databaseMetaData.getTimeDateFunctions() };
				for (int i = 0; i < array.length; ++i)
				{
					final StringTokenizer stringTokenizer = new StringTokenizer(array[i].toUpperCase(), ",");
					while (stringTokenizer.hasMoreTokens())
					{
						this.keywords.add(stringTokenizer.nextToken().trim());
					}
				}
			}
			catch (Exception x)
			{
				System.err.println(x);
			}
		}
		return this.keywords;
	}

	static
	{
		DefaultSqlFormatter.TOKENS = " \t\n\r\f()<>|,";
	}
}
