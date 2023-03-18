// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

public class DOMUtil
{
	public static void printDOM(final Node n, final LineWriter writer) throws TransformerException
	{
		final TransformerFactory instance = TransformerFactory.newInstance();
		boolean b = false;
		try
		{
			instance.setAttribute("indent-number", (int) 3);
			b = true;
		}
		catch (IllegalArgumentException ex)
		{
		}
		final Transformer transformer = instance.newTransformer();
		transformer.setOutputProperty("indent", "yes");
		if (!b)
		{
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
		}
		transformer.transform(new DOMSource(n), new StreamResult(writer));
	}

	public static void appendAttribute(final Node node, final String s, final String nodeValue)
	{
		final Attr attribute = node.getOwnerDocument().createAttribute(s);
		attribute.setNodeValue(nodeValue);
		node.getAttributes().setNamedItem(attribute);
	}
}
