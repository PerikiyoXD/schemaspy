// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.util.DbSpecificConfig;

public class DbTypeSelectorModel extends AbstractListModel implements ComboBoxModel
{
	private static final long serialVersionUID = 1L;
	private final List<DbSpecificConfig> dbConfigs;
	private Object selected;

	public DbTypeSelectorModel(final String str)
	{
		this.dbConfigs = new ArrayList<DbSpecificConfig>();
		final Pattern compile = Pattern.compile(".*/" + str);
		for (final String input : new TreeSet<String>(Config.getBuiltInDatabaseTypes(Config.getLoadedFromJar())))
		{
			final DbSpecificConfig selectedItem = new DbSpecificConfig(input);
			this.dbConfigs.add(selectedItem);
			if (compile.matcher(input).matches())
			{
				this.setSelectedItem(selectedItem);
			}
		}
		if (this.getSelectedItem() == null && this.dbConfigs.size() > 0)
		{
			this.setSelectedItem(this.dbConfigs.get(0));
		}
	}

	public Object getSelectedItem()
	{
		return this.selected;
	}

	public void setSelectedItem(final Object selected)
	{
		this.selected = selected;
	}

	public Object getElementAt(final int n)
	{
		return this.dbConfigs.get(n);
	}

	public int getSize()
	{
		return this.dbConfigs.size();
	}
}
