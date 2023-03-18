// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class LineWriter extends BufferedWriter
{
	private final Writer out;

	public LineWriter(final String name, final String s) throws UnsupportedEncodingException, FileNotFoundException
	{
		this(new FileOutputStream(name), s);
	}

	public LineWriter(final String name, final int n, final String s)
			throws UnsupportedEncodingException, FileNotFoundException
	{
		this(new FileOutputStream(name), n, s);
	}

	public LineWriter(final File file, final String s) throws UnsupportedEncodingException, FileNotFoundException
	{
		this(new FileOutputStream(file), s);
	}

	public LineWriter(final File file, final int n, final String s) throws UnsupportedEncodingException, IOException
	{
		this(new FileOutputStream(file), n, s);
	}

	public LineWriter(final OutputStream out, final String charsetName) throws UnsupportedEncodingException
	{
		this(new OutputStreamWriter(out, charsetName), 8192);
	}

	public LineWriter(final OutputStream out, final int n, final String charsetName) throws UnsupportedEncodingException
	{
		this(new OutputStreamWriter(out, charsetName), n);
	}

	private LineWriter(final Writer writer, final int sz)
	{
		super(writer, sz);
		this.out = writer;
	}

	public void writeln(final String str) throws IOException
	{
		this.write(str);
		this.newLine();
	}

	public void writeln() throws IOException
	{
		this.newLine();
	}

	@Override
	public String toString()
	{
		try
		{
			this.flush();
		}
		catch (IOException cause)
		{
			throw new RuntimeException(cause);
		}
		return this.out.toString();
	}
}
