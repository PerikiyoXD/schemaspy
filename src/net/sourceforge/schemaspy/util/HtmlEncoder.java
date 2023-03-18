// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.util.HashMap;
import java.util.Map;

public class HtmlEncoder
{
	private static final Map<String, String> map;

	private HtmlEncoder()
	{
	}

	public static String encodeToken(final char c)
	{
		return encodeToken(String.valueOf(c));
	}

	public static String encodeToken(final String s)
	{
		final String s2 = HtmlEncoder.map.get(s);
		return (s2 == null) ? s : s2;
	}

	public static String encodeString(final String s)
	{
		final int length = s.length();
		final StringBuilder sb = new StringBuilder(length * 2);
		for (int i = 0; i < length; ++i)
		{
			sb.append(encodeToken(s.charAt(i)));
		}
		return sb.toString();
	}

	static
	{
		(map = new HashMap<String, String>()).put("<", "&lt;");
		HtmlEncoder.map.put(">", "&gt;");
		HtmlEncoder.map.put("\n", "<br>" + System.getProperty("line.separator"));
		HtmlEncoder.map.put("\r", "");
	}
}
