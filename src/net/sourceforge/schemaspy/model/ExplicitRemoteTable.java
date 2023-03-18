// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Pattern;

public class ExplicitRemoteTable extends RemoteTable
{
	private static final Pattern excludeNone;

	public ExplicitRemoteTable(final Database database, final String s, final String s2, final String s3)
			throws SQLException
	{
		super(database, s, s2, s3, null, ExplicitRemoteTable.excludeNone, ExplicitRemoteTable.excludeNone);
	}

	@Override
	public void connectForeignKeys(final Map<String, Table> map, final Pattern pattern, final Pattern pattern2)
			throws SQLException
	{
		try
		{
			super.connectForeignKeys(map, pattern, pattern2);
		}
		catch (SQLException ex)
		{
		}
	}

	static
	{
		excludeNone = Pattern.compile("[^.]");
	}
}
