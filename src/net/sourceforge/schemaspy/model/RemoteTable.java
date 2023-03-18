// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import net.sourceforge.schemaspy.Config;

public class RemoteTable extends Table
{
	private final String baseSchema;

	public RemoteTable(
			final Database database, final String s, final String s2, final String baseSchema,
			final Properties properties, final Pattern pattern, final Pattern pattern2
	) throws SQLException
	{
		super(database, s, s2, null, properties, pattern, pattern2);
		this.baseSchema = baseSchema;
	}

	@Override
	public void connectForeignKeys(final Map<String, Table> map, final Pattern pattern, final Pattern pattern2)
			throws SQLException
	{
		ResultSet importedKeys = null;
		try
		{
			importedKeys = this.db.getMetaData().getImportedKeys(null, this.getSchema(), this.getName());
			while (importedKeys.next())
			{
				final String string = importedKeys.getString("PKTABLE_SCHEM");
				if (string != null && string.equals(this.baseSchema))
				{
					this.addForeignKey(
							importedKeys.getString("FK_NAME"), importedKeys.getString("FKCOLUMN_NAME"),
							importedKeys.getString("PKTABLE_SCHEM"), importedKeys.getString("PKTABLE_NAME"),
							importedKeys.getString("PKCOLUMN_NAME"), importedKeys.getInt("UPDATE_RULE"),
							importedKeys.getInt("DELETE_RULE"), map, pattern, pattern2
					);
				}
			}
		}
		catch (SQLException obj)
		{
			if (Config.getInstance().isOneOfMultipleSchemas())
			{
				throw obj;
			}
			System.err.println(
					"Couldn't resolve foreign keys for remote table " + this.getSchema() + "." + this.getName() + ": "
							+ obj
			);
		}
		finally
		{
			if (importedKeys != null)
			{
				importedKeys.close();
			}
		}
	}

	@Override
	public boolean isRemote()
	{
		return true;
	}
}
