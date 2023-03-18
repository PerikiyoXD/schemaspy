// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy.view;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.model.TableIndex;
import net.sourceforge.schemaspy.model.View;
import net.sourceforge.schemaspy.util.CaseInsensitiveMap;
import net.sourceforge.schemaspy.util.HtmlEncoder;
import net.sourceforge.schemaspy.util.LineWriter;

public class HtmlTablePage extends HtmlFormatter
{
	private static final HtmlTablePage instance;
	private int columnCounter;
	private final Map<String, String> defaultValueAliases;

	private HtmlTablePage()
	{
		this.columnCounter = 0;
		(this.defaultValueAliases = new HashMap<String, String>()).put("CURRENT TIMESTAMP", "now");
		this.defaultValueAliases.put("CURRENT TIME", "now");
		this.defaultValueAliases.put("CURRENT DATE", "now");
		this.defaultValueAliases.put("SYSDATE", "now");
		this.defaultValueAliases.put("CURRENT_DATE", "now");
	}

	public static HtmlTablePage getInstance()
	{
		return HtmlTablePage.instance;
	}

	public WriteStats write(
			final Database database, final Table table, final boolean b, final File parent, final WriteStats writeStats,
			final LineWriter lineWriter
	) throws IOException
	{
		final File file = new File(parent, "diagrams");
		final boolean generateDots = this.generateDots(table, file, writeStats);
		this.writeHeader(database, table, null, b, lineWriter);
		lineWriter.writeln("<table width='100%' border='0'>");
		lineWriter.writeln("<tr valign='top'><td class='container' align='left' valign='top'>");
		this.writeHeader(table, generateDots, lineWriter);
		lineWriter.writeln("</td><td class='container' rowspan='2' align='right' valign='top'>");
		this.writeLegend(true, lineWriter);
		lineWriter.writeln("</td><tr valign='top'><td class='container' align='left' valign='top'>");
		this.writeMainTable(table, lineWriter);
		this.writeNumRows(database, table, lineWriter);
		lineWriter.writeln("</td></tr></table>");
		this.writeCheckConstraints(table, lineWriter);
		this.writeIndexes(table, lineWriter);
		this.writeView(table, database, lineWriter);
		this.writeDiagram(table, writeStats, file, lineWriter);
		this.writeFooter(lineWriter);
		return writeStats;
	}

	private void writeHeader(final Table table, final boolean b, final LineWriter lineWriter) throws IOException
	{
		lineWriter.writeln("<form name='options' action=''>");
		if (b)
		{
			lineWriter.write(" <label for='implied'><input type=checkbox id='implied'");
			if (table.isOrphan(false))
			{
				lineWriter.write(" checked");
			}
			lineWriter.writeln(">Implied relationships</label>");
		}
		boolean b2 = false;
		final Iterator<TableColumn> iterator = table.getColumns().iterator();
		while (iterator.hasNext())
		{
			if (iterator.next().getComments() != null)
			{
				b2 = true;
				break;
			}
		}
		lineWriter.writeln(
				" <label for='showRelatedCols'><input type=checkbox id='showRelatedCols'>Related columns</label>"
		);
		lineWriter.writeln(" <label for='showConstNames'><input type=checkbox id='showConstNames'>Constraints</label>");
		lineWriter.writeln(
				" <label for='showComments'><input type=checkbox " + (b2 ? "checked " : "")
						+ "id='showComments'>Comments</label>"
		);
		lineWriter.writeln(" <label for='showLegend'><input type=checkbox checked id='showLegend'>Legend</label>");
		lineWriter.writeln("</form>");
	}

	public void writeMainTable(final Table table, final LineWriter lineWriter) throws IOException
	{
		HtmlColumnsPage.getInstance().writeMainTableHeader(table.getId() != null, null, lineWriter);
		lineWriter.writeln("<tbody valign='top'>");
		final HashSet<TableColumn> set = new HashSet<TableColumn>(table.getPrimaryColumns());
		final HashSet<TableColumn> set2 = new HashSet<TableColumn>();
		final Iterator<TableIndex> iterator = table.getIndexes().iterator();
		while (iterator.hasNext())
		{
			set2.addAll(iterator.next().getColumns());
		}
		final boolean b = table.getId() != null;
		final Iterator<TableColumn> iterator2 = table.getColumns().iterator();
		while (iterator2.hasNext())
		{
			this.writeColumn(iterator2.next(), null, set, set2, false, b, lineWriter);
		}
		lineWriter.writeln("</table>");
	}

	public void writeColumn(
			final TableColumn tableColumn, final String s, final Set<TableColumn> set, final Set<TableColumn> set2,
			final boolean b, final boolean b2, final LineWriter lineWriter
	) throws IOException
	{
		final boolean b3 = this.columnCounter++ % 2 == 0;
		if (b3)
		{
			lineWriter.writeln("<tr class='even'>");
		} else
		{
			lineWriter.writeln("<tr class='odd'>");
		}
		if (b2)
		{
			lineWriter.write(" <td class='detail' align='right'>");
			lineWriter.write(String.valueOf(tableColumn.getId()));
			lineWriter.writeln("</td>");
		}
		if (s != null)
		{
			lineWriter.write(" <td class='detail'><a href='tables/");
			lineWriter.write(s);
			lineWriter.write(".html'>");
			lineWriter.write(s);
			lineWriter.writeln("</a></td>");
		}
		if (set.contains(tableColumn))
		{
			lineWriter.write(" <td class='primaryKey' title='Primary Key'>");
		} else if (set2.contains(tableColumn))
		{
			lineWriter.write(" <td class='indexedColumn' title='Indexed'>");
		} else
		{
			lineWriter.write(" <td class='detail'>");
		}
		lineWriter.write(tableColumn.getName());
		lineWriter.writeln("</td>");
		lineWriter.write(" <td class='detail'>");
		lineWriter.write(tableColumn.getType().toLowerCase());
		lineWriter.writeln("</td>");
		lineWriter.write(" <td class='detail' align='right'>");
		lineWriter.write(tableColumn.getDetailedSize());
		lineWriter.writeln("</td>");
		lineWriter.write(" <td class='detail' align='center'");
		if (tableColumn.isNullable())
		{
			lineWriter.write(" title='nullable'>&nbsp;&radic;&nbsp;");
		} else
		{
			lineWriter.write(">");
		}
		lineWriter.writeln("</td>");
		lineWriter.write(" <td class='detail' align='center'");
		if (tableColumn.isAutoUpdated())
		{
			lineWriter.write(" title='Automatically updated by the database'>&nbsp;&radic;&nbsp;");
		} else
		{
			lineWriter.write(">");
		}
		lineWriter.writeln("</td>");
		final Object defaultValue = tableColumn.getDefaultValue();
		if (defaultValue != null || tableColumn.isNullable())
		{
			final String value = this.defaultValueAliases.get(String.valueOf(defaultValue).trim());
			if (value != null)
			{
				lineWriter.write(" <td class='detail' align='right' title='");
				lineWriter.write(String.valueOf(defaultValue));
				lineWriter.write("'><i>");
				lineWriter.write(value.toString());
				lineWriter.writeln("</i></td>");
			} else
			{
				lineWriter.write(" <td class='detail' align='right'>");
				lineWriter.write(String.valueOf(defaultValue));
				lineWriter.writeln("</td>");
			}
		} else
		{
			lineWriter.writeln(" <td class='detail'></td>");
		}
		if (!b)
		{
			lineWriter.write(" <td class='detail'>");
			final String s2 = (s == null) ? "" : "tables/";
			this.writeRelatives(tableColumn, false, s2, b3, lineWriter);
			lineWriter.writeln("</td>");
			lineWriter.write(" <td class='detail'>");
			this.writeRelatives(tableColumn, true, s2, b3, lineWriter);
			lineWriter.writeln(" </td>");
		}
		lineWriter.write(" <td class='comment detail'>");
		final String comments = tableColumn.getComments();
		if (comments != null)
		{
			if (this.encodeComments)
			{
				for (int i = 0; i < comments.length(); ++i)
				{
					lineWriter.write(HtmlEncoder.encodeToken(comments.charAt(i)));
				}
			} else
			{
				lineWriter.write(comments);
			}
		}
		lineWriter.writeln("</td>");
		lineWriter.writeln("</tr>");
	}

	private void writeRelatives(
			final TableColumn tableColumn, final boolean b, final String str, final boolean b2,
			final LineWriter lineWriter
	) throws IOException
	{
		final Set<TableColumn> set = b ? tableColumn.getParents() : tableColumn.getChildren();
		final int size = set.size();
		final String s = b2 ? "even" : "odd";
		if (size > 0)
		{
			lineWriter.newLine();
			lineWriter.writeln("  <table border='0' width='100%' cellspacing='0' cellpadding='0'>");
		}
		for (final TableColumn tableColumn2 : set)
		{
			final String name = tableColumn2.getTable().getName();
			final ForeignKeyConstraint foreignKeyConstraint = b ? tableColumn2.getChildConstraint(tableColumn)
					: tableColumn2.getParentConstraint(tableColumn);
			if (foreignKeyConstraint.isImplied())
			{
				lineWriter.writeln("   <tr class='impliedRelationship relative " + s + "' valign='top'>");
			} else
			{
				lineWriter.writeln("   <tr class='relative " + s + "' valign='top'>");
			}
			lineWriter.write("    <td class='relatedTable detail' title=\"");
			lineWriter.write(foreignKeyConstraint.toString());
			lineWriter.write("\">");
			lineWriter.write("<a href='");
			if (!tableColumn2.getTable().isRemote() || Config.getInstance().isOneOfMultipleSchemas())
			{
				lineWriter.write(str);
				if (tableColumn2.getTable().isRemote())
				{
					lineWriter.write("../../" + tableColumn2.getTable().getSchema() + "/tables/");
				}
				lineWriter.write(name);
				lineWriter.write(".html");
			}
			lineWriter.write("'>");
			lineWriter.write(name);
			lineWriter.write("</a>");
			lineWriter.write("<span class='relatedKey'>.");
			lineWriter.write(tableColumn2.getName());
			lineWriter.writeln("</span>");
			lineWriter.writeln("    </td>");
			lineWriter.write("    <td class='constraint detail'>");
			lineWriter.write(foreignKeyConstraint.getName());
			final String deleteRuleDescription = foreignKeyConstraint.getDeleteRuleDescription();
			if (deleteRuleDescription.length() > 0)
			{
				lineWriter.write(
						"<span title='" + deleteRuleDescription + "'>&nbsp;" + foreignKeyConstraint.getDeleteRuleAlias()
								+ "</span>"
				);
			}
			lineWriter.writeln("</td>");
			lineWriter.writeln("   </tr>");
		}
		if (size > 0)
		{
			lineWriter.writeln("  </table>");
		}
	}

	private void writeNumRows(final Database database, final Table table, final LineWriter lineWriter)
			throws IOException
	{
		lineWriter.write("<p title='" + table.getColumns().size() + " columns'>");
		if (this.displayNumRows && !table.isView())
		{
			lineWriter.write(
					"Table contained " + NumberFormat.getIntegerInstance().format(table.getNumRows()) + " rows at "
			);
		} else
		{
			lineWriter.write("Analyzed at ");
		}
		lineWriter.write(database.getConnectTime());
		lineWriter.writeln("<p/>");
	}

	private void writeCheckConstraints(final Table table, final LineWriter lineWriter) throws IOException
	{
		final Map<String, String> checkConstraints = table.getCheckConstraints();
		if (checkConstraints != null && !checkConstraints.isEmpty())
		{
			lineWriter.writeln("<div class='indent'>");
			lineWriter.writeln("<b>Requirements (check constraints):</b>");
			lineWriter.writeln("<table class='dataTable' border='1' rules='groups'><colgroup><colgroup>");
			lineWriter.writeln("<thead>");
			lineWriter.writeln(" <tr>");
			lineWriter.writeln("  <th>Constraint</th>");
			lineWriter.writeln("  <th class='constraint' style='text-align:left;'>Constraint Name</th>");
			lineWriter.writeln(" </tr>");
			lineWriter.writeln("</thead>");
			lineWriter.writeln("<tbody>");
			for (final String str : checkConstraints.keySet())
			{
				lineWriter.writeln(" <tr>");
				lineWriter.write("  <td class='detail'>");
				lineWriter.write(HtmlEncoder.encodeString(checkConstraints.get(str).toString()));
				lineWriter.writeln("</td>");
				lineWriter.write("  <td class='constraint' style='text-align:left;'>");
				lineWriter.write(str);
				lineWriter.writeln("</td>");
				lineWriter.writeln(" </tr>");
			}
			lineWriter.writeln("</table></div><p>");
		}
	}

	private void writeIndexes(final Table table, final LineWriter lineWriter) throws IOException
	{
		final boolean b = table.getId() != null;
		final Set<TableIndex> indexes = table.getIndexes();
		if (indexes != null && !indexes.isEmpty())
		{
			boolean uniqueNullable = false;
			final Iterator<TableIndex> iterator = indexes.iterator();
			while (iterator.hasNext())
			{
				uniqueNullable = iterator.next().isUniqueNullable();
				if (uniqueNullable)
				{
					break;
				}
			}
			lineWriter.writeln("<div class='indent'>");
			lineWriter.writeln("<b>Indexes:</b>");
			lineWriter.writeln(
					"<table class='dataTable' border='1' rules='groups'><colgroup><colgroup><colgroup><colgroup>"
							+ (b ? "<colgroup>" : "") + (uniqueNullable ? "<colgroup>" : "")
			);
			lineWriter.writeln("<thead>");
			lineWriter.writeln(" <tr>");
			if (b)
			{
				lineWriter.writeln("  <th>ID</th>");
			}
			lineWriter.writeln("  <th>Column(s)</th>");
			lineWriter.writeln("  <th>Type</th>");
			lineWriter.writeln("  <th>Sort</th>");
			lineWriter.writeln("  <th class='constraint' style='text-align:left;'>Constraint Name</th>");
			if (uniqueNullable)
			{
				lineWriter.writeln("  <th>Anomalies</th>");
			}
			lineWriter.writeln(" </tr>");
			lineWriter.writeln("</thead>");
			lineWriter.writeln("<tbody>");
			for (final TableIndex tableIndex : new TreeSet<TableIndex>(indexes))
			{
				lineWriter.writeln(" <tr>");
				if (b)
				{
					lineWriter.write("  <td class='detail' align='right'>");
					lineWriter.write(String.valueOf(tableIndex.getId()));
					lineWriter.writeln("</td>");
				}
				if (tableIndex.isPrimaryKey())
				{
					lineWriter.write("  <td class='primaryKey'>");
				} else
				{
					lineWriter.write("  <td class='indexedColumn'>");
				}
				String str = tableIndex.getColumnsAsString();
				if (str.startsWith("+"))
				{
					str = str.substring(1);
				}
				lineWriter.write(str);
				lineWriter.writeln("</td>");
				lineWriter.write("  <td class='detail'>");
				lineWriter.write(tableIndex.getType());
				lineWriter.writeln("</td>");
				lineWriter.write("  <td class='detail' style='text-align:left;'>");
				final Iterator<TableColumn> iterator3 = tableIndex.getColumns().iterator();
				while (iterator3.hasNext())
				{
					if (tableIndex.isAscending(iterator3.next()))
					{
						lineWriter.write("<span title='Ascending'>Asc</span>");
					} else
					{
						lineWriter.write("<span title='Descending'>Desc</span>");
					}
					if (iterator3.hasNext())
					{
						lineWriter.write("/");
					}
				}
				lineWriter.writeln("</td>");
				lineWriter.write("  <td class='constraint' style='text-align:left;'>");
				lineWriter.write(tableIndex.getName());
				lineWriter.writeln("</td>");
				if (tableIndex.isUniqueNullable())
				{
					if (tableIndex.getColumns().size() == 1)
					{
						lineWriter.writeln("  <td class='detail'>This unique column is also nullable</td>");
					} else
					{
						lineWriter.writeln("  <td class='detail'>These unique columns are also nullable</td>");
					}
				} else if (uniqueNullable)
				{
					lineWriter.writeln("  <td>&nbsp;</td>");
				}
				lineWriter.writeln(" </tr>");
			}
			lineWriter.writeln("</table>");
			lineWriter.writeln("</div>");
		}
	}

	private void writeView(final Table table, final Database database, final LineWriter lineWriter) throws IOException
	{
		final String viewSql;
		if (table.isView() && (viewSql = table.getViewSql()) != null)
		{
			final CaseInsensitiveMap<View> caseInsensitiveMap = new CaseInsensitiveMap<View>();
			for (final Table table2 : database.getTables())
			{
				caseInsensitiveMap.put(table2.getName(), (View) table2);
			}
			for (final View view : database.getViews())
			{
				caseInsensitiveMap.put(view.getName(), view);
			}
			final TreeSet<Table> set = new TreeSet<Table>();
			final String format = Config.getInstance().getSqlFormatter().format(viewSql, database, set);
			lineWriter.writeln("<div class='indent spacer'>");
			lineWriter.writeln("  View Definition:");
			lineWriter.writeln(format);
			lineWriter.writeln("</div>");
			lineWriter.writeln("<div class='spacer'>&nbsp;</div>");
			if (!set.isEmpty())
			{
				lineWriter.writeln("<div class='indent'>");
				lineWriter.writeln("  Possibly Referenced Tables/Views:");
				lineWriter.writeln("  <div class='viewReferences'>");
				lineWriter.write("  ");
				for (final Table table3 : set)
				{
					lineWriter.write("<a href='");
					lineWriter.write(table3.getName());
					lineWriter.write(".html'>");
					lineWriter.write(table3.getName());
					lineWriter.write("</a>&nbsp;");
				}
				lineWriter.writeln("  </div>");
				lineWriter.writeln("</div><p/>");
			}
		}
	}

	private boolean generateDots(final Table table, final File file, final WriteStats writeStats) throws IOException
	{
		final File file2 = new File(file, table.getName() + ".1degree.dot");
		final File file3 = new File(file, table.getName() + ".1degree.svg");
		final File file4 = new File(file, table.getName() + ".2degrees.dot");
		final File file5 = new File(file, table.getName() + ".2degrees.svg");
		final File file6 = new File(file, table.getName() + ".implied2degrees.dot");
		final File file7 = new File(file, table.getName() + ".implied2degrees.svg");
		file2.delete();
		file3.delete();
		file4.delete();
		file5.delete();
		file6.delete();
		file7.delete();
		if (table.getMaxChildren() + table.getMaxParents() > 0)
		{
			final DotFormatter instance = DotFormatter.getInstance();
			final LineWriter lineWriter = new LineWriter(file2, "UTF-8");
			final WriteStats writeStats2 = new WriteStats(writeStats);
			instance.writeRealRelationships(table, false, writeStats2, lineWriter);
			lineWriter.close();
			final LineWriter lineWriter2 = new LineWriter(file4, "UTF-8");
			final WriteStats writeStats3 = new WriteStats(writeStats);
			final Set<ForeignKeyConstraint> writeRealRelationships = instance
					.writeRealRelationships(table, true, writeStats3, lineWriter2);
			lineWriter2.close();
			if (
				writeStats2.getNumTablesWritten() + writeStats2.getNumViewsWritten() == writeStats3
						.getNumTablesWritten() + writeStats3.getNumViewsWritten()
			)
			{
				file4.delete();
			}
			if (!writeRealRelationships.isEmpty())
			{
				final LineWriter lineWriter3 = new LineWriter(file6, "UTF-8");
				instance.writeAllRelationships(table, true, writeStats, lineWriter3);
				lineWriter3.close();
				return true;
			}
		}
		return false;
	}

	private void writeDiagram(
			final Table table, final WriteStats writeStats, final File file, final LineWriter lineWriter
	) throws IOException
	{
		if (table.getMaxChildren() + table.getMaxParents() > 0)
		{
			lineWriter.writeln("<table width='100%' border='0'><tr><td class='container'>");
			if (HtmlTableDiagrammer.getInstance().write(table, file, lineWriter))
			{
				lineWriter.writeln("</td></tr></table>");
				this.writeExcludedColumns(writeStats.getExcludedColumns(), table, lineWriter);
			} else
			{
				lineWriter.writeln("</td></tr></table><p>");
				this.writeInvalidGraphvizInstallation(lineWriter);
			}
		}
	}

	@Override
	protected String getPathToRoot()
	{
		return "../";
	}

	static
	{
		instance = new HtmlTablePage();
	}
}
