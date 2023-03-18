// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.util.HashMap;
import java.util.Map;

public class CaseInsensitiveMap<V> extends HashMap<String, V>
{
	private static final long serialVersionUID = 1L;

	@Override
	public V get(final Object o)
	{
		return super.get(((String) o).toUpperCase());
	}

	@Override
	public V put(final String s, final V value)
	{
		return super.put(s.toUpperCase(), value);
	}

	@Override
	public void putAll(final Map<? extends String, ? extends V> map)
	{
		for (final Map.Entry<? extends String, ? extends V> entry : map.entrySet())
		{
			this.put((String) entry.getKey(), (V) entry.getValue());
		}
	}

	@Override
	public V remove(final Object o)
	{
		return super.remove(((String) o).toUpperCase());
	}

	@Override
	public boolean containsKey(final Object o)
	{
		return super.containsKey(((String) o).toUpperCase());
	}
}
