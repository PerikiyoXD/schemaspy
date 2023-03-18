// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy;

import net.sourceforge.schemaspy.model.ConnectionFailure;
import net.sourceforge.schemaspy.model.EmptySchemaException;
import net.sourceforge.schemaspy.model.InvalidConfigurationException;
import net.sourceforge.schemaspy.model.ProcessExecutionException;
import net.sourceforge.schemaspy.ui.MainFrame;

public class Main
{
	public static void main(final String[] arguments) throws Exception
	{
		if (arguments.length == 1 && arguments[0].equals("-gui"))
		{
			new MainFrame().setVisible(true);
			return;
		}

		final SchemaAnalyzer schemaAnalyzer = new SchemaAnalyzer();
		int status = 1;

		try
		{
			status = ((schemaAnalyzer.analyze(new Config(arguments)) == null) ? 1 : 0);
		}
		catch (ConnectionFailure connectionFailure)
		{
			status = 3;
		}
		catch (EmptySchemaException ex4)
		{
			status = 2;
		}
		catch (InvalidConfigurationException ex)
		{
			System.err.println();
			if (ex.getParamName() != null)
			{
				System.err.println("Bad parameter specified for " + ex.getParamName());
			}
			System.err.println(ex.getMessage());
			if (ex.getCause() != null && !ex.getMessage().endsWith(ex.getMessage()))
			{
				System.err.println(" caused by " + ex.getCause().getMessage());
			}
		}
		catch (ProcessExecutionException ex2)
		{
			System.err.println(ex2.getMessage());
		}
		catch (Exception ex3)
		{
			ex3.printStackTrace();
		}

		System.exit(status);
	}
}
