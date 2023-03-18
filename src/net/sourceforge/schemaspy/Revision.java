// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Revision
{
	private static String rev;
	private static final String resourceName = "/schemaSpy.rev";

	private static void initialize()
	{
		InputStream resourceAsStream = null;
		BufferedReader bufferedReader = null;

		try
		{
			resourceAsStream = Revision.class.getResourceAsStream(resourceName);
			if (resourceAsStream != null)
			{
				bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
				try
				{
					Revision.rev = bufferedReader.readLine();
				}
				catch (IOException ex)
				{
				}
			}
		}
		finally
		{
			try
			{
				if (bufferedReader != null)
				{
					bufferedReader.close();
				} else if (resourceAsStream != null)
				{
					resourceAsStream.close();
				}
			}
			catch (IOException ex2)
			{
			}
		}
	}

	@Override
	public String toString()
	{
		return Revision.rev;
	}

	public static void main(final String[] array) throws IOException
	{
		final BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(".svn", "entries")));
		bufferedReader.readLine();
		bufferedReader.readLine();
		bufferedReader.readLine();
		final String line = bufferedReader.readLine();
		bufferedReader.close();
		String parent = "output";
		if (array.length < 1)
		{
			parent = array[0];
		}
		final FileWriter fileWriter = new FileWriter(new File(parent, resourceName));
		fileWriter.write(line);
		fileWriter.close();
		initialize();
		System.out.println("Subversion revision " + new Revision());
	}

	static
	{
		Revision.rev = "Unknown";
		initialize();
	}
}
