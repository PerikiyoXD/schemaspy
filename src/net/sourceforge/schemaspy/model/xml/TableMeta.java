// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TableMeta
{
	private final String name;
	private final String comments;
	private final List<TableColumnMeta> columns;
	private final String remoteSchema;
	private static final Logger logger;

	TableMeta(final Node node)
	{
		this.columns = new ArrayList<TableColumnMeta>();
		final NamedNodeMap attributes = node.getAttributes();
		this.name = attributes.getNamedItem("name").getNodeValue();
		final Node namedItem = attributes.getNamedItem("comments");
		if (namedItem != null)
		{
			final String trim = namedItem.getNodeValue().trim();
			this.comments = ((trim.length() == 0) ? null : trim);
		} else
		{
			this.comments = null;
		}
		final Node namedItem2 = attributes.getNamedItem("remoteSchema");
		if (namedItem2 != null)
		{
			this.remoteSchema = namedItem2.getNodeValue().trim();
		} else
		{
			this.remoteSchema = null;
		}
		TableMeta.logger.fine(
				"Found XML table metadata for " + this.name + " remoteSchema: " + this.remoteSchema + " comments: "
						+ this.comments
		);
		final NodeList elementsByTagName = ((Element) node.getChildNodes()).getElementsByTagName("column");
		for (int i = 0; i < elementsByTagName.getLength(); ++i)
		{
			this.columns.add(new TableColumnMeta(elementsByTagName.item(i)));
		}
	}

	public String getName()
	{
		return this.name;
	}

	public String getComments()
	{
		return this.comments;
	}

	public List<TableColumnMeta> getColumns()
	{
		return this.columns;
	}

	public String getRemoteSchema()
	{
		return this.remoteSchema;
	}

	static
	{
		logger = Logger.getLogger(TableMeta.class.getName());
	}
}
