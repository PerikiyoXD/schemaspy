// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

public class RailsForeignKeyConstraint extends ForeignKeyConstraint
{
	public RailsForeignKeyConstraint(final TableColumn tableColumn, final TableColumn tableColumn2)
	{
		super(tableColumn, tableColumn2);
	}

	@Override
	public String getName()
	{
		return "ByRailsConventionConstraint";
	}
}
