// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.model;

public class ImpliedForeignKeyConstraint extends ForeignKeyConstraint
{
	public ImpliedForeignKeyConstraint(final TableColumn tableColumn, final TableColumn tableColumn2)
	{
		super(tableColumn, tableColumn2);
	}

	@Override
	public String getName()
	{
		return "Implied Constraint";
	}

	@Override
	public boolean isImplied()
	{
		return true;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getChildTable());
		sb.append(".");
		sb.append(ForeignKeyConstraint.toString(this.getChildColumns()));
		sb.append("'s name implies that it's a child of ");
		sb.append(this.getParentTable());
		sb.append(".");
		sb.append(ForeignKeyConstraint.toString(this.getParentColumns()));
		sb.append(", but it doesn't reference that column.");
		return sb.toString();
	}
}
