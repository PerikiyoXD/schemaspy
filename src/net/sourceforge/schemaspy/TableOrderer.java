// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.Table;

public class TableOrderer
{
	public List<Table> getTablesOrderedByRI(
			final Collection<Table> c, final Collection<ForeignKeyConstraint> collection
	)
	{
		final ArrayList<Table> list = new ArrayList<Table>();
		final ArrayList<Table> list2 = new ArrayList<Table>();
		final ArrayList<Table> list3 = new ArrayList<Table>(c);
		final ArrayList<Table> list4 = new ArrayList<Table>();
		final Iterator<Table> iterator = list3.iterator();
		while (iterator.hasNext())
		{
			final Table table = iterator.next();
			if (table.isRemote())
			{
				table.unlinkParents();
				table.unlinkChildren();
				iterator.remove();
			} else
			{
				if (!table.isLeaf() || !table.isRoot())
				{
					continue;
				}
				list4.add(table);
				iterator.remove();
			}
		}
		final List<Table> sortTrimmedLevel = sortTrimmedLevel(list4);
		int n = 0;
		while (!list3.isEmpty())
		{
			final int size = list3.size();
			list2.addAll(0, trimLeaves((List<Table>) list3));
			list.addAll(trimRoots((List<Table>) list3));
			if (size == list3.size())
			{
				if (n == 0)
				{
					final Iterator<Table> iterator2 = list3.iterator();
					while (iterator2.hasNext())
					{
						iterator2.next().removeNonRealForeignKeys();
					}
					n = 1;
				} else
				{
					boolean b = false;
					final Iterator<Table> iterator3 = list3.iterator();
					while (iterator3.hasNext())
					{
						final ForeignKeyConstraint removeSelfReferencingConstraint = iterator3.next()
								.removeSelfReferencingConstraint();
						if (removeSelfReferencingConstraint != null)
						{
							collection.add(removeSelfReferencingConstraint);
							b = true;
						}
					}
					if (b)
					{
						continue;
					}
					final TreeSet<Table> set = new TreeSet<Table>(new Comparator<Table>()
					{
						public int compare(final Table table, final Table table2)
						{
							int compareTo = Math.abs(table2.getNumChildren() - table2.getNumParents())
									- Math.abs(table.getNumChildren() - table.getNumParents());
							if (compareTo == 0)
							{
								compareTo = table.compareTo(table2);
							}
							return compareTo;
						}
					});
					set.addAll(list3);
					collection.add(set.iterator().next().removeAForeignKeyConstraint());
				}
			}
		}
		final ArrayList<Table> list5 = new ArrayList<Table>(list.size() + list2.size());
		list5.addAll(list);
		list5.addAll(list2);
		list5.addAll((Collection<Table>) sortTrimmedLevel);
		return (List<Table>) list5;
	}

	private static List<Table> trimRoots(final List<Table> list)
	{
		final ArrayList<Table> list2 = new ArrayList<Table>();
		final Iterator<Table> iterator = list.iterator();
		while (iterator.hasNext())
		{
			final Table table = iterator.next();
			if (table.isRoot())
			{
				list2.add(table);
				iterator.remove();
			}
		}
		final List<Table> sortTrimmedLevel = sortTrimmedLevel(list2);
		final Iterator<Table> iterator2 = sortTrimmedLevel.iterator();
		while (iterator2.hasNext())
		{
			iterator2.next().unlinkChildren();
		}
		return sortTrimmedLevel;
	}

	private static List<Table> trimLeaves(final List<Table> list)
	{
		final ArrayList<Table> list2 = new ArrayList<Table>();
		final Iterator<Table> iterator = list.iterator();
		while (iterator.hasNext())
		{
			final Table table = iterator.next();
			if (table.isLeaf())
			{
				list2.add(table);
				iterator.remove();
			}
		}
		final List<Table> sortTrimmedLevel = sortTrimmedLevel(list2);
		final Iterator<Table> iterator2 = sortTrimmedLevel.iterator();
		while (iterator2.hasNext())
		{
			iterator2.next().unlinkParents();
		}
		return sortTrimmedLevel;
	}

	private static List<Table> sortTrimmedLevel(final List<Table> list)
	{
		final class TrimComparator implements Comparator<Table>
		{
			public int compare(final Table table, final Table table2)
			{
				int compareTo = table2.getMaxChildren() - table.getMaxChildren();
				if (compareTo == 0)
				{
					compareTo = table.getMaxParents() - table2.getMaxParents();
				}
				if (compareTo == 0)
				{
					compareTo = table.compareTo(table2);
				}
				return compareTo;
			}
		}
		final TreeSet<Table> c = new TreeSet<Table>(new TrimComparator());
		c.addAll(list);
		return new ArrayList<Table>(c);
	}
}
