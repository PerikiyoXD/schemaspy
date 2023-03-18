// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceWriter
{
	private static ResourceWriter instance;

	protected ResourceWriter()
	{
	}

	public static ResourceWriter getInstance()
	{
		return ResourceWriter.instance;
	}

	public void writeResource(final String s, final File file) throws IOException
	{
		file.getParentFile().mkdirs();
		final InputStream resourceAsStream = this.getClass().getResourceAsStream(s);
		if (resourceAsStream == null)
		{
			throw new IOException("Resource \"" + s + "\" not found");
		}
		final byte[] array = new byte[4096];
		final FileOutputStream fileOutputStream = new FileOutputStream(file);
		int read;
		while ((read = resourceAsStream.read(array)) != -1)
		{
			fileOutputStream.write(array, 0, read);
		}
		resourceAsStream.close();
		fileOutputStream.close();
	}

	static
	{
		ResourceWriter.instance = new ResourceWriter();
	}
}
