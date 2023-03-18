// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.ui;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.util.DbSpecificConfig;
import net.sourceforge.schemaspy.util.DbSpecificOption;

public class DbConfigTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	private final List<PropertyDescriptor> options;
	private Config config;

	public DbConfigTableModel()
	{
		this.options = new ArrayList<PropertyDescriptor>();
		this.config = Config.getInstance();
		final PropertyDescriptor[] configProps = this.getConfigProps();
		this.options.add(this.getDescriptor("outputDir", "Directory to generate HTML output to", configProps));
		this.options.add(this.getDescriptor("schema", "Schema to evaluate", configProps));
		this.options.add(this.getDescriptor("user", "User ID to connect with", configProps));
		this.options.add(this.getDescriptor("password", "Password associated with user id", configProps));
		this.options.add(this.getDescriptor("impliedConstraintsEnabled", "XXXX", configProps));
	}

	public void setDbSpecificConfig(final DbSpecificConfig dbSpecificConfig)
	{
		Config.setInstance(this.config = dbSpecificConfig.getConfig());
		final PropertyDescriptor[] configProps = this.getConfigProps();
		this.removeDbSpecificOptions();
		for (final DbSpecificOption dbSpecificOption : dbSpecificConfig.getOptions())
		{
			final PropertyDescriptor descriptor = this
					.getDescriptor(dbSpecificOption.getName(), dbSpecificOption.getDescription(), configProps);
			descriptor.setValue("dbSpecific", Boolean.TRUE);
			this.options.add(descriptor);
		}
		this.fireTableDataChanged();
	}

	@Override
	public String getColumnName(final int n)
	{
		switch (n)
		{
			case 0:
			{
				return "Option";
			}
			default:
			{
				return "Value";
			}
		}
	}

	private PropertyDescriptor getDescriptor(
			final String s, final String shortDescription, PropertyDescriptor[] configProps
	)
	{
		if (configProps == null)
		{
			configProps = this.getConfigProps();
		}
		for (int i = 0; i < configProps.length; ++i)
		{
			final PropertyDescriptor propertyDescriptor = configProps[i];
			if (propertyDescriptor.getName().equalsIgnoreCase(s))
			{
				propertyDescriptor.setShortDescription(shortDescription);
				return propertyDescriptor;
			}
		}
		throw new IllegalArgumentException(s + " is not a valid configuration item");
	}

	private PropertyDescriptor[] getConfigProps() throws RuntimeException
	{
		BeanInfo beanInfo;
		try
		{
			beanInfo = Introspector.getBeanInfo(Config.class);
		}
		catch (IntrospectionException cause)
		{
			throw new RuntimeException(cause);
		}
		return beanInfo.getPropertyDescriptors();
	}

	private void removeDbSpecificOptions()
	{
		final Iterator<PropertyDescriptor> iterator = this.options.iterator();
		while (iterator.hasNext())
		{
			if (iterator.next().getValue("dbSpecific") != null)
			{
				iterator.remove();
			}
		}
	}

	public int getColumnCount()
	{
		return 2;
	}

	public int getRowCount()
	{
		return this.options.size();
	}

	@Override
	public boolean isCellEditable(final int n, final int n2)
	{
		return n2 == 1 && this.options.get(n).getWriteMethod() != null;
	}

	public Object getValueAt(final int n, final int n2)
	{
		final PropertyDescriptor propertyDescriptor = this.options.get(n);
		switch (n2)
		{
			case 0:
			{
				return propertyDescriptor.getName();
			}
			case 1:
			{
				try
				{
					return propertyDescriptor.getReadMethod().invoke(this.config, (Object[]) null);
				}
				catch (InvocationTargetException cause)
				{
					if (cause.getCause() instanceof Config.MissingRequiredParameterException)
					{
						return null;
					}
					throw new RuntimeException(cause);
				}
				catch (Exception cause2)
				{
					throw new RuntimeException(cause2);
				}
			}
		}
		return null;
	}

	@Override
	public void setValueAt(Object value, final int row, final int column)
	{
		final Object value2 = this.getValueAt(row, column);
		if (value2 != value && (value == null || value2 == null || !value.equals(value2)))
		{
			final PropertyDescriptor propertyDescriptor = this.options.get(row);
			try
			{
				if (value instanceof String && propertyDescriptor.getPropertyType().isAssignableFrom(Integer.class))
				{
					try
					{
						value = Integer.valueOf((String) value);
					}
					catch (NumberFormatException ex)
					{
						value = value2;
					}
				}
				propertyDescriptor.getWriteMethod().invoke(this.config, value);
			}
			catch (Exception cause)
			{
				throw new RuntimeException(cause);
			}
			this.fireTableCellUpdated(row, column);
		}
	}

	public Class<?> getClass(final int n)
	{
		return this.options.get(n).getPropertyType();
	}

	public String getDescription(final int n)
	{
		return this.options.get(n).getShortDescription();
	}
}
