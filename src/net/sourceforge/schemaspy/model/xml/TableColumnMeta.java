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

public class TableColumnMeta
{
	private final String name;
	private final String comments;
	private final boolean isPrimary;
	private final List<ForeignKeyMeta> foreignKeys;
	private final boolean isExcluded;
	private final boolean isAllExcluded;
	private final boolean isImpliedParentsDisabled;
	private final boolean isImpliedChildrenDisabled;
	private static final Logger logger;

	TableColumnMeta(final Node node)
	{
		this.foreignKeys = new ArrayList<ForeignKeyMeta>();
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
		final Node namedItem2 = attributes.getNamedItem("primaryKey");
		if (namedItem2 != null)
		{
			this.isPrimary = this.evalBoolean(namedItem2.getNodeValue());
		} else
		{
			this.isPrimary = false;
		}
		final Node namedItem3 = attributes.getNamedItem("disableImpliedKeys");
		if (namedItem3 != null)
		{
			final String lowerCase = namedItem3.getNodeValue().trim().toLowerCase();
			if (lowerCase.equals("to"))
			{
				this.isImpliedChildrenDisabled = true;
				this.isImpliedParentsDisabled = false;
			} else if (lowerCase.equals("from"))
			{
				this.isImpliedParentsDisabled = true;
				this.isImpliedChildrenDisabled = false;
			} else if (lowerCase.equals("all"))
			{
				final boolean b = true;
				this.isImpliedParentsDisabled = b;
				this.isImpliedChildrenDisabled = b;
			} else
			{
				final boolean b2 = false;
				this.isImpliedParentsDisabled = b2;
				this.isImpliedChildrenDisabled = b2;
			}
		} else
		{
			final boolean b3 = false;
			this.isImpliedParentsDisabled = b3;
			this.isImpliedChildrenDisabled = b3;
		}
		final Node namedItem4 = attributes.getNamedItem("disableDiagramAssociations");
		if (namedItem4 != null)
		{
			final String lowerCase2 = namedItem4.getNodeValue().trim().toLowerCase();
			if (lowerCase2.equals("all"))
			{
				this.isAllExcluded = true;
				this.isExcluded = true;
			} else if (lowerCase2.equals("exceptdirect"))
			{
				this.isAllExcluded = false;
				this.isExcluded = true;
			} else
			{
				this.isAllExcluded = false;
				this.isExcluded = false;
			}
		} else
		{
			this.isAllExcluded = false;
			this.isExcluded = false;
		}
		TableColumnMeta.logger.finer(
				"Found XML column metadata for " + this.name + " isPrimaryKey: " + this.isPrimary + " comments: "
						+ this.comments
		);
		final NodeList elementsByTagName = ((Element) node.getChildNodes()).getElementsByTagName("foreignKey");
		for (int i = 0; i < elementsByTagName.getLength(); ++i)
		{
			this.foreignKeys.add(new ForeignKeyMeta(elementsByTagName.item(i)));
		}
	}

	private boolean evalBoolean(String lowerCase)
	{
		if (lowerCase == null)
		{
			return false;
		}
		lowerCase = lowerCase.trim().toLowerCase();
		return lowerCase.equals("true") || lowerCase.equals("yes") || lowerCase.equals("1");
	}

	public String getName()
	{
		return this.name;
	}

	public String getComments()
	{
		return this.comments;
	}

	public boolean isPrimary()
	{
		return this.isPrimary;
	}

	public List<ForeignKeyMeta> getForeignKeys()
	{
		return this.foreignKeys;
	}

	public boolean isExcluded()
	{
		return this.isExcluded;
	}

	public boolean isAllExcluded()
	{
		return this.isAllExcluded;
	}

	public boolean isImpliedParentsDisabled()
	{
		return this.isImpliedParentsDisabled;
	}

	public boolean isImpliedChildrenDisabled()
	{
		return this.isImpliedChildrenDisabled;
	}

	static
	{
		logger = Logger.getLogger(TableColumnMeta.class.getName());
	}
}
