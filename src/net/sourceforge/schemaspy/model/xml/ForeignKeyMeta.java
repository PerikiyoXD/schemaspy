// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model.xml;

import java.util.logging.Logger;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ForeignKeyMeta
{
	private final String tableName;
	private final String columnName;
	private final String remoteSchema;
	private static final Logger logger;

	ForeignKeyMeta(final Node node)
	{
		final NamedNodeMap attributes = node.getAttributes();
		final Node namedItem = attributes.getNamedItem("table");
		if (namedItem == null)
		{
			throw new IllegalStateException("XML foreignKey definition requires 'table' attribute");
		}
		this.tableName = namedItem.getNodeValue();
		final Node namedItem2 = attributes.getNamedItem("column");
		if (namedItem2 == null)
		{
			throw new IllegalStateException("XML foreignKey definition requires 'column' attribute");
		}
		this.columnName = namedItem2.getNodeValue();
		final Node namedItem3 = attributes.getNamedItem("remoteSchema");
		if (namedItem3 != null)
		{
			this.remoteSchema = namedItem3.getNodeValue();
		} else
		{
			this.remoteSchema = null;
		}
		ForeignKeyMeta.logger.finer(
				"Found XML FK metadata for " + this.tableName + "." + this.columnName + " remoteSchema: "
						+ this.remoteSchema
		);
	}

	public String getTableName()
	{
		return this.tableName;
	}

	public String getColumnName()
	{
		return this.columnName;
	}

	public String getRemoteSchema()
	{
		return this.remoteSchema;
	}

	@Override
	public String toString()
	{
		return this.tableName + '.' + this.columnName;
	}

	static
	{
		logger = Logger.getLogger(ForeignKeyMeta.class.getName());
	}
}
