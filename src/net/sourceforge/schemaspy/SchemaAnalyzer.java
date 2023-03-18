// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sourceforge.schemaspy.model.ConnectionFailure;
import net.sourceforge.schemaspy.model.Database;
import net.sourceforge.schemaspy.model.EmptySchemaException;
import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.ImpliedForeignKeyConstraint;
import net.sourceforge.schemaspy.model.InvalidConfigurationException;
import net.sourceforge.schemaspy.model.Table;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.model.xml.SchemaMeta;
import net.sourceforge.schemaspy.util.ConnectionURLBuilder;
import net.sourceforge.schemaspy.util.DOMUtil;
import net.sourceforge.schemaspy.util.DbSpecificOption;
import net.sourceforge.schemaspy.util.Dot;
import net.sourceforge.schemaspy.util.LineWriter;
import net.sourceforge.schemaspy.util.LogFormatter;
import net.sourceforge.schemaspy.util.PasswordReader;
import net.sourceforge.schemaspy.util.ResourceWriter;
import net.sourceforge.schemaspy.view.DotFormatter;
import net.sourceforge.schemaspy.view.HtmlAnomaliesPage;
import net.sourceforge.schemaspy.view.HtmlColumnsPage;
import net.sourceforge.schemaspy.view.HtmlConstraintsPage;
import net.sourceforge.schemaspy.view.HtmlMainIndexPage;
import net.sourceforge.schemaspy.view.HtmlOrphansPage;
import net.sourceforge.schemaspy.view.HtmlRelationshipsPage;
import net.sourceforge.schemaspy.view.HtmlTablePage;
import net.sourceforge.schemaspy.view.ImageWriter;
import net.sourceforge.schemaspy.view.StyleSheet;
import net.sourceforge.schemaspy.view.TextFormatter;
import net.sourceforge.schemaspy.view.WriteStats;
import net.sourceforge.schemaspy.view.XmlTableFormatter;

public class SchemaAnalyzer
{
	private final Logger logger;
	private boolean fineEnabled;

	public SchemaAnalyzer()
	{
		this.logger = Logger.getLogger(this.getClass().getName());
	}

	public Database analyze(final Config config) throws Exception
	{
		try
		{
			if (config.isHelpRequired())
			{
				config.dumpUsage(null, false);
				return null;
			}
			if (config.isDbHelpRequired())
			{
				config.dumpUsage(null, true);
				return null;
			}
			Logger.getLogger("").setLevel(config.getLogLevel());
			for (final Handler handler : Logger.getLogger("").getHandlers())
			{
				if (handler instanceof ConsoleHandler)
				{
					((ConsoleHandler) handler).setFormatter(new LogFormatter());
					handler.setLevel(config.getLogLevel());
				}
			}
			this.fineEnabled = this.logger.isLoggable(Level.FINE);
			this.logger.info("Starting schema analysis");
			long n2;
			final long n = n2 = System.currentTimeMillis();
			final File outputDir = config.getOutputDir();
			if (!outputDir.isDirectory() && !outputDir.mkdirs())
			{
				throw new IOException("Failed to create directory '" + outputDir + "'");
			}
			final List<String> schemas = config.getSchemas();
			if (schemas != null)
			{
				final List<String> list = config.asList();
				yankParam(list, "-o");
				yankParam(list, "-s");
				list.remove("-all");
				list.remove("-schemas");
				list.remove("-schemata");
				MultipleSchemaAnalyzer.getInstance().analyze(
						config.getDb(), schemas, list, config.getUser(), outputDir, config.getCharset(),
						Config.getLoadedFromJar()
				);
				return null;
			}
			final Properties dbProperties = config.getDbProperties(config.getDbType());
			final ConnectionURLBuilder connectionURLBuilder = new ConnectionURLBuilder(config, dbProperties);
			if (config.getDb() == null)
			{
				config.setDb(connectionURLBuilder.getConnectionURL());
			}
			if (config.getRemainingParameters().size() != 0)
			{
				final StringBuilder sb = new StringBuilder("Unrecognized option(s):");
				final Iterator<String> iterator = config.getRemainingParameters().iterator();
				while (iterator.hasNext())
				{
					sb.append(" " + iterator.next());
				}
				this.logger.warning(sb.toString());
			}
			final String property = dbProperties.getProperty("driver");
			String str = dbProperties.getProperty("driverPath");
			if (str == null)
			{
				str = "";
			}
			if (config.getDriverPath() != null)
			{
				str = config.getDriverPath() + File.pathSeparator + str;
			}
			final Connection connection = this
					.getConnection(config, connectionURLBuilder.getConnectionURL(), property, str);
			final DatabaseMetaData metaData = connection.getMetaData();
			final String db = config.getDb();
			String s = config.getSchema();
			if (config.isEvaluateAllEnabled())
			{
				final List<String> list2 = config.asList();
				for (final DbSpecificOption dbSpecificOption : connectionURLBuilder.getOptions())
				{
					if (!list2.contains("-" + dbSpecificOption.getName()))
					{
						list2.add("-" + dbSpecificOption.getName());
						list2.add(dbSpecificOption.getValue().toString());
					}
				}
				yankParam(list2, "-o");
				yankParam(list2, "-s");
				list2.remove("-all");
				String s2 = config.getSchemaSpec();
				if (s2 == null)
				{
					s2 = dbProperties.getProperty("schemaSpec", ".*");
				}
				MultipleSchemaAnalyzer.getInstance().analyze(
						db, metaData, s2, null, list2, config.getUser(), outputDir, config.getCharset(),
						Config.getLoadedFromJar()
				);
				return null;
			}
			if (s == null && metaData.supportsSchemasInTableDefinitions() && !config.isSchemaDisabled())
			{
				s = config.getUser();
				if (s == null)
				{
					throw new InvalidConfigurationException(
							"Either a schema ('-s') or a user ('-u') must be specified"
					);
				}
				config.setSchema(s);
			}
			final SchemaMeta schemaMeta = (config.getMeta() == null) ? null : new SchemaMeta(config.getMeta(), db, s);
			if (config.isHtmlGenerationEnabled())
			{
				new File(outputDir, "tables").mkdirs();
				new File(outputDir, "diagrams/summary").mkdirs();
				this.logger.info(
						"Connected to " + metaData.getDatabaseProductName() + " - "
								+ metaData.getDatabaseProductVersion()
				);
				if (schemaMeta != null && schemaMeta.getFile() != null)
				{
					this.logger.info("Using additional metadata from " + schemaMeta.getFile());
				}
				this.logger.info("Gathering schema details");
				if (!this.fineEnabled)
				{
					System.out.print("Gathering schema details...");
				}
			}
			final Database database = new Database(config, connection, metaData, db, s, dbProperties, schemaMeta);
			final ArrayList<Table> list3 = new ArrayList<Table>(database.getTables());
			list3.addAll(database.getViews());
			if (list3.isEmpty())
			{
				dumpNoTablesMessage(s, config.getUser(), metaData, config.getTableInclusions() != null);
				if (!config.isOneOfMultipleSchemas())
				{
					throw new EmptySchemaException();
				}
			}
			final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			final Element element = document.createElement("database");
			document.appendChild(element);
			DOMUtil.appendAttribute(element, "name", db);
			if (s != null)
			{
				DOMUtil.appendAttribute(element, "schema", s);
			}
			DOMUtil.appendAttribute(element, "type", database.getDatabaseProduct());
			if (config.isHtmlGenerationEnabled())
			{
				final long currentTimeMillis = System.currentTimeMillis();
				if (!this.fineEnabled)
				{
					System.out.println("(" + (currentTimeMillis - n) / 1000L + "sec)");
				}
				this.logger.info("Gathered schema details in " + (currentTimeMillis - n) / 1000L + " seconds");
				this.logger.info("Writing/graphing summary");
				System.err.flush();
				System.out.flush();
				if (!this.fineEnabled)
				{
					System.out.print("Writing/graphing summary");
					System.out.print(".");
				}
				ImageWriter.getInstance().writeImages(outputDir);
				ResourceWriter.getInstance().writeResource("/jquery.js", new File(outputDir, "/jquery.js"));
				ResourceWriter.getInstance().writeResource("/schemaSpy.js", new File(outputDir, "/schemaSpy.js"));
				if (!this.fineEnabled)
				{
					System.out.print(".");
				}
				final boolean b = list3.size() <= config.getMaxDetailedTables();
				final boolean impliedConstraintsEnabled = config.isImpliedConstraintsEnabled();
				if (config.isRailsEnabled())
				{
					DbAnalyzer.getRailsConstraints(database.getTablesByName());
				}
				final File file = new File(outputDir, "diagrams/summary");
				final String str2 = "relationships";
				final LineWriter lineWriter = new LineWriter(new File(file, str2 + ".real.compact.dot"), "UTF-8");
				final WriteStats writeStats = new WriteStats((Collection<Table>) list3);
				DotFormatter.getInstance()
						.writeRealRelationships(database, (Collection<Table>) list3, true, b, writeStats, lineWriter);
				final boolean b2 = writeStats.getNumTablesWritten() > 0 || writeStats.getNumViewsWritten() > 0;
				lineWriter.close();
				if (b2)
				{
					if (!this.fineEnabled)
					{
						System.out.print(".");
					}
					final LineWriter lineWriter2 = new LineWriter(new File(file, str2 + ".real.large.dot"), "UTF-8");
					DotFormatter.getInstance().writeRealRelationships(
							database, (Collection<Table>) list3, false, b, writeStats, lineWriter2
					);
					lineWriter2.close();
				}
				List<ImpliedForeignKeyConstraint> impliedConstraints;
				if (impliedConstraintsEnabled)
				{
					impliedConstraints = DbAnalyzer.getImpliedConstraints((Collection<Table>) list3);
				} else
				{
					impliedConstraints = new ArrayList<ImpliedForeignKeyConstraint>();
				}
				final List<Table> orphans = DbAnalyzer.getOrphans((Collection<Table>) list3);
				final boolean b3 = !orphans.isEmpty() && Dot.getInstance().isValid();
				if (!this.fineEnabled)
				{
					System.out.print(".");
				}
				final File file2 = new File(file, str2 + ".implied.compact.dot");
				final LineWriter lineWriter3 = new LineWriter(file2, "UTF-8");
				final boolean writeAllRelationships = DotFormatter.getInstance()
						.writeAllRelationships(database, (Collection<Table>) list3, true, b, writeStats, lineWriter3);
				final Set<TableColumn> excludedColumns = writeStats.getExcludedColumns();
				lineWriter3.close();
				if (writeAllRelationships)
				{
					final LineWriter lineWriter4 = new LineWriter(new File(file, str2 + ".implied.large.dot"), "UTF-8");
					DotFormatter.getInstance().writeAllRelationships(
							database, (Collection<Table>) list3, false, b, writeStats, lineWriter4
					);
					lineWriter4.close();
				} else
				{
					file2.delete();
				}
				final LineWriter lineWriter5 = new LineWriter(new File(outputDir, str2 + ".html"), config.getCharset());
				HtmlRelationshipsPage.getInstance()
						.write(database, file, str2, b3, b2, writeAllRelationships, excludedColumns, lineWriter5);
				lineWriter5.close();
				if (!this.fineEnabled)
				{
					System.out.print(".");
				}
				final LineWriter lineWriter6 = new LineWriter(
						new File(outputDir, "utilities" + ".html"), config.getCharset()
				);
				HtmlOrphansPage.getInstance().write(database, orphans, file, lineWriter6);
				lineWriter6.close();
				if (!this.fineEnabled)
				{
					System.out.print(".");
				}
				final LineWriter lineWriter7 = new LineWriter(
						new File(outputDir, "index.html"), 65536, config.getCharset()
				);
				HtmlMainIndexPage.getInstance().write(database, (Collection<Table>) list3, b3, lineWriter7);
				lineWriter7.close();
				if (!this.fineEnabled)
				{
					System.out.print(".");
				}
				final List<ForeignKeyConstraint> foreignKeyConstraints = DbAnalyzer
						.getForeignKeyConstraints((Collection<Table>) list3);
				final LineWriter lineWriter8 = new LineWriter(
						new File(outputDir, "constraints.html"), 262144, config.getCharset()
				);
				HtmlConstraintsPage.getInstance()
						.write(database, foreignKeyConstraints, (Collection<Table>) list3, b3, lineWriter8);
				lineWriter8.close();
				if (!this.fineEnabled)
				{
					System.out.print(".");
				}
				final LineWriter lineWriter9 = new LineWriter(
						new File(outputDir, "anomalies.html"), 16384, config.getCharset()
				);
				HtmlAnomaliesPage.getInstance()
						.write(database, (Collection<Table>) list3, impliedConstraints, b3, lineWriter9);
				lineWriter9.close();
				if (!this.fineEnabled)
				{
					System.out.print(".");
				}
				for (final HtmlColumnsPage.ColumnInfo columnInfo : HtmlColumnsPage.getInstance().getColumnInfos())
				{
					final LineWriter lineWriter10 = new LineWriter(
							new File(outputDir, columnInfo.getLocation()), 16384, config.getCharset()
					);
					HtmlColumnsPage.getInstance()
							.write(database, (Collection<Table>) list3, columnInfo, b3, lineWriter10);
					lineWriter10.close();
				}
				n2 = System.currentTimeMillis();
				if (!this.fineEnabled)
				{
					System.out.println("(" + (n2 - currentTimeMillis) / 1000L + "sec)");
				}
				this.logger.info("Completed summary in " + (n2 - currentTimeMillis) / 1000L + " seconds");
				this.logger.info("Writing/diagramming details");
				if (!this.fineEnabled)
				{
					System.out.print("Writing/diagramming details");
				}
				final HtmlTablePage instance = HtmlTablePage.getInstance();
				for (final Table table : list3)
				{
					if (!this.fineEnabled)
					{
						System.out.print('.');
					} else
					{
						this.logger.fine("Writing details of " + table.getName());
					}
					final LineWriter lineWriter11 = new LineWriter(
							new File(outputDir, "tables/" + table.getName() + ".html"), 24576, config.getCharset()
					);
					instance.write(database, table, b3, outputDir, writeStats, lineWriter11);
					lineWriter11.close();
				}
				final LineWriter lineWriter12 = new LineWriter(
						new File(outputDir, "schemaSpy.css"), config.getCharset()
				);
				StyleSheet.getInstance().write(lineWriter12);
				lineWriter12.close();
			}
			XmlTableFormatter.getInstance().appendTables(element, (Collection<Table>) list3);
			String s3 = new File(db).getName();
			if (s != null)
			{
				s3 = s3 + '.' + s;
			}
			final LineWriter lineWriter13 = new LineWriter(new File(outputDir, s3 + ".xml"), "UTF-8");
			document.getDocumentElement().normalize();
			DOMUtil.printDOM(document, lineWriter13);
			lineWriter13.close();
			final List<Table> tablesOrderedByRI = new TableOrderer()
					.getTablesOrderedByRI(database.getTables(), new ArrayList<ForeignKeyConstraint>());
			final LineWriter lineWriter14 = new LineWriter(new File(outputDir, "insertionOrder.txt"), 16384, "UTF-8");
			TextFormatter.getInstance().write(tablesOrderedByRI, false, lineWriter14);
			lineWriter14.close();
			final LineWriter lineWriter15 = new LineWriter(new File(outputDir, "deletionOrder.txt"), 16384, "UTF-8");
			Collections.reverse(tablesOrderedByRI);
			TextFormatter.getInstance().write(tablesOrderedByRI, false, lineWriter15);
			lineWriter15.close();
			if (config.isHtmlGenerationEnabled())
			{
				final long currentTimeMillis2 = System.currentTimeMillis();
				if (!this.fineEnabled)
				{
					System.out.println("(" + (currentTimeMillis2 - n2) / 1000L + "sec)");
				}
				this.logger.info("Wrote table details in " + (currentTimeMillis2 - n2) / 1000L + " seconds");
				if (this.logger.isLoggable(Level.INFO))
				{
					this.logger.info(
							"Wrote relationship details of " + list3.size() + " tables/views to directory '"
									+ config.getOutputDir() + "' in " + (currentTimeMillis2 - n) / 1000L + " seconds."
					);
					this.logger.info("View the results by opening " + new File(config.getOutputDir(), "index.html"));
				} else
				{
					System.out.println(
							"Wrote relationship details of " + list3.size() + " tables/views to directory '"
									+ config.getOutputDir() + "' in " + (currentTimeMillis2 - n) / 1000L + " seconds."
					);
					System.out.println("View the results by opening " + new File(config.getOutputDir(), "index.html"));
				}
			}
			return database;
		}
		catch (Config.MissingRequiredParameterException ex)
		{
			config.dumpUsage(ex.getMessage(), ex.isDbTypeSpecific());
			return null;
		}
	}

	private static void dumpNoTablesMessage(
			final String str, final String str2, final DatabaseMetaData databaseMetaData, final boolean b
	) throws SQLException
	{
		System.out.println();
		System.out.println();
		System.out.println("No tables or views were found in schema '" + str + "'.");
		final List<String> schemas = DbAnalyzer.getSchemas(databaseMetaData);
		if (str == null || schemas.contains(str))
		{
			System.out.println("The schema exists in the database, but the user you specified (" + str2 + ')');
			System.out.println("  might not have rights to read its contents.");
			if (b)
			{
				System.out.println("Another possibility is that the regular expression that you specified");
				System.out.println("  for what to include (via -i) didn't match any tables.");
			}
		} else
		{
			System.out.println("The schema does not exist in the database.");
			System.out.println("Make sure that you specify a valid schema with the -s option and that");
			System.out.println("  the user specified (" + str2 + ") can read from the schema.");
			System.out.println("Note that schema names are usually case sensitive.");
		}
		System.out.println();
		final boolean b2 = schemas.size() != 1;
		System.out.println(
				schemas.size() + " schema" + (b2 ? "s" : "") + " exist" + (b2 ? "" : "s") + " in this database."
		);
		System.out.println("Some of these \"schemas\" may be users or system schemas.");
		System.out.println();
		final Iterator<String> iterator = schemas.iterator();
		while (iterator.hasNext())
		{
			System.out.print(iterator.next() + " ");
		}
		System.out.println();
		final List<String> populatedSchemas = DbAnalyzer.getPopulatedSchemas(databaseMetaData);
		if (populatedSchemas.isEmpty())
		{
			System.out.println("Unable to determine if any of the schemas contain tables/views");
		} else
		{
			System.out.println("These schemas contain tables/views that user '" + str2 + "' can see:");
			System.out.println();
			final Iterator<String> iterator2 = populatedSchemas.iterator();
			while (iterator2.hasNext())
			{
				System.out.print(" " + iterator2.next());
			}
		}
	}

	private Connection getConnection(final Config config, final String str, final String str2, final String str3)
			throws FileNotFoundException, IOException
	{
		if (this.logger.isLoggable(Level.INFO))
		{
			this.logger.info("Using database properties:");
			this.logger.info("  " + config.getDbPropertiesLoadedFrom());
		} else
		{
			System.out.println("Using database properties:");
			System.out.println("  " + config.getDbPropertiesLoadedFrom());
		}
		final ArrayList<URL> list = new ArrayList<URL>();
		final ArrayList<File> x = new ArrayList<File>();
		final StringTokenizer stringTokenizer = new StringTokenizer(str3, File.pathSeparator);
		while (stringTokenizer.hasMoreTokens())
		{
			final File file = new File(stringTokenizer.nextToken());
			if (file.exists())
			{
				list.add(file.toURI().toURL());
			} else
			{
				x.add(file);
			}
		}
		final URLClassLoader loader = new URLClassLoader(list.toArray(new URL[list.size()]));
		Driver driver;
		try
		{
			driver = (Driver) Class.forName(str2, true, loader).getDeclaredConstructor().newInstance();
		}
		catch (Exception x2)
		{
			System.err.println(x2);
			System.err.println();
			System.err.print("Failed to load driver '" + str2 + "'");
			if (list.isEmpty())
			{
				System.err.println();
			} else
			{
				System.err.println("from: " + list);
			}
			if (!x.isEmpty())
			{
				if (x.size() == 1)
				{
					System.err.print("This entry doesn't point to a valid file/directory: ");
				} else
				{
					System.err.print("These entries don't point to valid files/directories: ");
				}
				System.err.println(x);
			}
			System.err.println();
			System.err.println("Use the -dp option to specify the location of the database");
			System.err.println("drivers for your database (usually in a .jar or .zip/.Z).");
			System.err.println();
			throw new ConnectionFailure(x2);
		}
		final Properties connectionProperties = config.getConnectionProperties();
		if (config.getUser() != null)
		{
			connectionProperties.put("user", config.getUser());
		}
		if (config.getPassword() != null)
		{
			connectionProperties.put("password", config.getPassword());
		} else if (config.isPromptForPasswordEnabled())
		{
			connectionProperties.put(
					"password", new String(PasswordReader.getInstance().readPassword("Password: ", new Object[0]))
			);
		}
		connectionProperties.put("characterEncoding", "utf8");
		
		Connection connect;
		try
		{
			connect = driver.connect(str, connectionProperties);
			if (connect == null)
			{
				System.err.println();
				System.err.println("Cannot connect to this database URL:");
				System.err.println("  " + str);
				System.err.println("with this driver:");
				System.err.println("  " + str2);
				System.err.println();
				System.err.println("Additional connection information may be available in ");
				System.err.println("  " + config.getDbPropertiesLoadedFrom());
				throw new ConnectionFailure("Cannot connect to '" + str + "' with driver '" + str2 + "'");
			}
		}
		catch (UnsatisfiedLinkError unsatisfiedLinkError)
		{
			System.err.println();
			System.err.println("Failed to load driver [" + str2 + "] from classpath " + list);
			System.err.println();
			System.err.println("Make sure the reported library (.dll/.lib/.so) from the following line can be");
			System.err.println("found by your PATH (or LIB*PATH) environment variable");
			System.err.println();
			unsatisfiedLinkError.printStackTrace();
			throw new ConnectionFailure(unsatisfiedLinkError);
		}
		catch (Exception ex)
		{
			System.err.println();
			System.err.println("Failed to connect to database URL [" + str + "]");
			System.err.println();
			ex.printStackTrace();
			throw new ConnectionFailure(ex);
		}
		return connect;
	}

	private static void yankParam(final List<String> list, final String s)
	{
		final int index = list.indexOf(s);
		if (index >= 0)
		{
			list.remove(index);
			list.remove(index);
		}
	}
}
