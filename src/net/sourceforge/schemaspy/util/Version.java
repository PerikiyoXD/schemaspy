// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Version implements Comparable<Version>
{
	private final List<Integer> segments;
	private final String asString;
	private final int hashCode;

	public Version(final String s)
	{
		this.segments = new ArrayList<Integer>();
		this.asString = s;
		int hashCode = 0;
		if (s != null)
		{
			final StringTokenizer stringTokenizer = new StringTokenizer(s, ". -_");
			while (stringTokenizer.hasMoreTokens())
			{
				final Integer n = Integer.valueOf(stringTokenizer.nextToken());

				this.segments.add(n);
				hashCode += n;
			}
		}
		this.hashCode = hashCode;
	}

	public int compareTo(final Version version)
	{
		for (int min = Math.min(this.segments.size(), version.segments.size()), i = 0; i < min; ++i)
		{
			final int compareTo = this.segments.get(i).compareTo(version.segments.get(i));
			if (compareTo != 0)
			{
				return compareTo;
			}
		}
		if (this.segments.size() == version.segments.size())
		{
			return 0;
		}
		if (this.segments.size() > version.segments.size())
		{
			return 1;
		}
		return -1;
	}

	@Override
	public boolean equals(final Object o)
	{
		return o != null && o instanceof Version && this.compareTo((Version) o) == 0;
	}

	@Override
	public int hashCode()
	{
		return this.hashCode;
	}

	@Override
	public String toString()
	{
		return this.asString;
	}
}
