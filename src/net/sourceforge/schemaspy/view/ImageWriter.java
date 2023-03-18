// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.File;
import java.io.IOException;

import net.sourceforge.schemaspy.util.ResourceWriter;

public class ImageWriter extends ResourceWriter
{
	private static ImageWriter instance;

	private ImageWriter()
	{
	}

	public static ImageWriter getInstance()
	{
		return ImageWriter.instance;
	}

	public void writeImages(final File file) throws IOException
	{
		new File(file, "images").mkdir();
		this.writeResource("/images/tabLeft.gif", new File(file, "/images/tabLeft.gif"));
		this.writeResource("/images/tabRight.gif", new File(file, "/images/tabRight.gif"));
		this.writeResource("/images/background.gif", new File(file, "/images/background.gif"));
	}

	static
	{
		ImageWriter.instance = new ImageWriter();
	}
}
