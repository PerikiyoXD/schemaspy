// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

public class View extends Table
{
	private String viewSql;

	public View(
			final Database database, final String s, final String s2, final String s3, String fetchViewSql,
			final Properties properties, final Pattern pattern, final Pattern pattern2
	) throws SQLException
	{
		super(database, s, s2, s3, properties, pattern, pattern2);
		if (fetchViewSql == null)
		{
			fetchViewSql = this.fetchViewSql();
		}
		if (fetchViewSql != null && fetchViewSql.trim().length() > 0)
		{
			this.viewSql = fetchViewSql;
		}
	}

	@Override
	public boolean isView()
	{
		return true;
	}

	@Override
	public String getViewSql()
	{
		return this.viewSql;
	}

	@Override
	protected int fetchNumRows()
	{
		return 0;
	}

	private String fetchViewSql() throws SQLException
	{
		final String property = this.properties.getProperty("selectViewSql");
		if (property == null)
		{
			return null;
		}
		PreparedStatement prepareStatement = null;
		ResultSet executeQuery = null;
		try
		{
			prepareStatement = this.db.prepareStatement(property, this.getName());
			executeQuery = prepareStatement.executeQuery();
			if (executeQuery.next())
			{
				try
				{
					return executeQuery.getString("view_definition");
				}
				catch (SQLException ex2)
				{
					return executeQuery.getString("text");
				}
			}
			return null;
		}
		catch (SQLException ex)
		{
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
