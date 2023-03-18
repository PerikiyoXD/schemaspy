// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.model.InvalidConfigurationException;

public class SchemaMeta
{
	private final List<TableMeta> tables;
	private final String comments;
	private final File metaFile;
	private final Logger logger;

	public SchemaMeta(final String s, final String s2, final String s3) throws InvalidConfigurationException
	{
		this.tables = new ArrayList<TableMeta>();
		this.logger = Logger.getLogger(this.getClass().getName());
		File file = new File(s);
		if (file.isDirectory())
		{
			final String string = ((s3 == null) ? s2 : s3) + ".meta.xml";
			file = new File(file, string);
			if (!file.exists())
			{
				if (Config.getInstance().isOneOfMultipleSchemas())
				{
					this.logger.info("Meta directory \"" + s + "\" should contain a file named \"" + string + '\"');
					this.comments = null;
					this.metaFile = null;
					return;
				}
				throw new InvalidConfigurationException(
						"Meta directory \"" + s + "\" must contain a file named \"" + string + '\"'
				);
			}
		} else if (!file.exists())
		{
			throw new InvalidConfigurationException("Specified meta file \"" + s + "\" does not exist");
		}
		this.metaFile = file;
		final Document parse = this.parse(this.metaFile);
		final NodeList elementsByTagName = parse.getElementsByTagName("comments");
		if (elementsByTagName != null && elementsByTagName.getLength() > 0)
		{
			this.comments = elementsByTagName.item(0).getTextContent();
		} else
		{
			this.comments = null;
		}
		final NodeList elementsByTagName2 = parse.getElementsByTagName("tables");
		if (elementsByTagName2 != null)
		{
			final NodeList elementsByTagName3 = ((Element) elementsByTagName2.item(0)).getElementsByTagName("table");
			for (int i = 0; i < elementsByTagName3.getLength(); ++i)
			{
				this.tables.add(new TableMeta(elementsByTagName3.item(i)));
			}
		}
	}

	public String getComments()
	{
		return this.comments;
	}

	public File getFile()
	{
		return this.metaFile;
	}

	public List<TableMeta> getTables()
	{
		return this.tables;
	}

	private void validate(final Document n) throws SAXException, IOException
	{
		SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
				.newSchema(new StreamSource(this.getClass().getResourceAsStream("/schemaspy.meta.xsd"))).newValidator()
				.validate(new DOMSource(n));
	}

	private Document parse(final File obj) throws InvalidConfigurationException
	{
		final DocumentBuilderFactory instance = DocumentBuilderFactory.newInstance();
		instance.setNamespaceAware(true);
		instance.setIgnoringElementContentWhitespace(true);
		instance.setIgnoringComments(true);
		DocumentBuilder documentBuilder;
		try
		{
			documentBuilder = instance.newDocumentBuilder();
		}
		catch (ParserConfigurationException ex)
		{
			throw new InvalidConfigurationException("Invalid XML parser configuration", ex);
		}
		Document parse;
		try
		{
			parse = documentBuilder.parse(obj);
			this.validate(parse);
		}
		catch (SAXException ex2)
		{
			throw new InvalidConfigurationException(obj + " failed XML validation:", ex2);
		}
		catch (IOException ex3)
		{
			throw new InvalidConfigurationException("Could not read " + obj + ":", ex3);
		}
		return parse;
	}
}
