// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.util.Set;

import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.Table;

public interface SqlFormatter
{
	String format(final String p0, final Database p1, final Set<Table> p2);
}
