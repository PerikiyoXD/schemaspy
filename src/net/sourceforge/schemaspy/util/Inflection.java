// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class Inflection
{
	private static final List<Inflection> plural;
	private static final List<Inflection> singular;
	private static final List<String> uncountable;
	private final String pattern;
	private final String replacement;
	private final boolean ignoreCase;

	public Inflection(final String s)
	{
		this(s, null, true);
	}

	public Inflection(final String s, final String s2)
	{
		this(s, s2, true);
	}

	public Inflection(final String pattern, final String replacement, final boolean ignoreCase)
	{
		this.pattern = pattern;
		this.replacement = replacement;
		this.ignoreCase = ignoreCase;
	}

	private static void plural(final String s, final String s2)
	{
		Inflection.plural.add(0, new Inflection(s, s2));
	}

	private static void singular(final String s, final String s2)
	{
		Inflection.singular.add(0, new Inflection(s, s2));
	}

	private static void irregular(final String s, final String s2)
	{
		plural("(" + s.substring(0, 1) + ")" + s.substring(1) + "$", "$1" + s2.substring(1));
		singular("(" + s2.substring(0, 1) + ")" + s2.substring(1) + "$", "$1" + s.substring(1));
	}

	private static void uncountable(final String s)
	{
		Inflection.uncountable.add(s);
	}

	public boolean match(final String input)
	{
		int flags = 0;
		if (this.ignoreCase)
		{
			flags |= 0x2;
		}
		return Pattern.compile(this.pattern, flags).matcher(input).find();
	}

	public String replace(final String input)
	{
		int flags = 0;
		if (this.ignoreCase)
		{
			flags |= 0x2;
		}
		return Pattern.compile(this.pattern, flags).matcher(input).replaceAll(this.replacement);
	}

	public static String pluralize(final String s)
	{
		if (isUncountable(s))
		{
			return s;
		}
		for (final Inflection inflection : Inflection.plural)
		{
			if (inflection.match(s))
			{
				return inflection.replace(s);
			}
		}
		return s;
	}

	public static String singularize(final String s)
	{
		if (isUncountable(s))
		{
			return s;
		}
		for (final Inflection inflection : Inflection.singular)
		{
			if (inflection.match(s))
			{
				return inflection.replace(s);
			}
		}
		return s;
	}

	public static boolean isUncountable(final String anotherString)
	{
		final Iterator<String> iterator = Inflection.uncountable.iterator();
		while (iterator.hasNext())
		{
			if (iterator.next().equalsIgnoreCase(anotherString))
			{
				return true;
			}
		}
		return false;
	}

	static
	{
		plural = new ArrayList<Inflection>();
		singular = new ArrayList<Inflection>();
		uncountable = new ArrayList<String>();
		plural("$", "s");
		plural("s$", "s");
		plural("(ax|test)is$", "$1es");
		plural("(octop|vir)us$", "$1i");
		plural("(alias|status)$", "$1es");
		plural("(bu)s$", "$1ses");
		plural("(buffal|tomat)o$", "$1oes");
		plural("([ti])um$", "$1a");
		plural("sis$", "ses");
		plural("(?:([^f])fe|([lr])f)$", "$1$2ves");
		plural("(hive)$", "$1s");
		plural("([^aeiouy]|qu)y$", "$1ies");
		plural("(x|ch|ss|sh)$", "$1es");
		plural("(matr|vert|ind)ix|ex$", "$1ices");
		plural("([m|l])ouse$", "$1ice");
		plural("^(ox)$", "$1en");
		plural("(quiz)$", "$1zes");
		singular("s$", "");
		singular("(n)ews$", "$1ews");
		singular("([ti])a$", "$1um");
		singular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1$2sis");
		singular("(^analy)ses$", "$1sis");
		singular("([^f])ves$", "$1fe");
		singular("(hive)s$", "$1");
		singular("(tive)s$", "$1");
		singular("([lr])ves$", "$1f");
		singular("([^aeiouy]|qu)ies$", "$1y");
		singular("(s)eries$", "$1eries");
		singular("(m)ovies$", "$1ovie");
		singular("(x|ch|ss|sh)es$", "$1");
		singular("([m|l])ice$", "$1ouse");
		singular("(bus)es$", "$1");
		singular("(o)es$", "$1");
		singular("(shoe)s$", "$1");
		singular("(cris|ax|test)es$", "$1is");
		singular("(octop|vir)i$", "$1us");
		singular("(alias|status)es$", "$1");
		singular("^(ox)en", "$1");
		singular("(vert|ind)ices$", "$1ex");
		singular("(matr)ices$", "$1ix");
		singular("(quiz)zes$", "$1");
		irregular("person", "people");
		irregular("man", "men");
		irregular("child", "children");
		irregular("sex", "sexes");
		irregular("move", "moves");
		uncountable("equipment");
		uncountable("information");
		uncountable("rice");
		uncountable("money");
		uncountable("species");
		uncountable("series");
		uncountable("fish");
		uncountable("sheep");
	}
}
