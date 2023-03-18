// 
// Decompiled by Procyon v0.5.36
// 

package net.sourceforge.schemaspy;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sourceforge.schemaspy.model.InvalidConfigurationException;
import net.sourceforge.schemaspy.util.DbSpecificConfig;
import net.sourceforge.schemaspy.util.Dot;
import net.sourceforge.schemaspy.view.DefaultSqlFormatter;
import net.sourceforge.schemaspy.view.SqlFormatter;

public class Config
{
	private static Config instance;
	private final List<String> options;
	private Map<String, String> dbSpecificOptions;
	private Map<String, String> originalDbSpecificOptions;
	private boolean helpRequired;
	private boolean dbHelpRequired;
	private File outputDir;
	private File graphvizDir;
	private String dbType;
	private String schema;
	private List<String> schemas;
	private String user;
	private Boolean singleSignOn;
	private Boolean noSchema;
	private String password;
	private Boolean promptForPassword;
	private String db;
	private String host;
	private Integer port;
	private String server;
	private String meta;
	private Pattern tableInclusions;
	private Pattern tableExclusions;
	private Pattern columnExclusions;
	private Pattern indirectColumnExclusions;
	private String userConnectionPropertiesFile;
	private Properties userConnectionProperties;
	private Integer maxDbThreads;
	private Integer maxDetailedTables;
	private String driverPath;
	private String css;
	private String charset;
	private String font;
	private Integer fontSize;
	private String description;
	private String dbPropertiesLoadedFrom;
	private Level logLevel;
	private SqlFormatter sqlFormatter;
	private String sqlFormatterClass;
	private Boolean generateHtml;
	private Boolean includeImpliedConstraints;
	private Boolean logoEnabled;
	private Boolean rankDirBugEnabled;
	private Boolean encodeCommentsEnabled;
	private Boolean numRowsEnabled;
	private Boolean viewsEnabled;
	private Boolean meterEnabled;
	private Boolean railsEnabled;
	private Boolean evaluteAll;
	private Boolean highQuality;
	private Boolean lowQuality;
	private Boolean adsEnabled;
	private String schemaSpec;
	private boolean populating;
	public static final String DOT_CHARSET = "UTF-8";
	private static final String ESCAPED_EQUALS = "\\=";

	public Config()
	{
		this.populating = false;
		if (Config.instance == null)
		{
			setInstance(this);
		}
		this.options = new ArrayList<String>();
	}

	public Config(final String[] a)
	{
		this.populating = false;
		setInstance(this);
		this.options = this.fixupArgs(Arrays.asList(a));
		this.helpRequired = (this.options.remove("-?") || this.options.remove("/?") || this.options.remove("?")
				|| this.options.remove("-h") || this.options.remove("-help") || this.options.remove("--help"));
		this.dbHelpRequired = (this.options.remove("-dbHelp") || this.options.remove("-dbhelp"));
	}

	public static Config getInstance()
	{
		if (Config.instance == null)
		{
			Config.instance = new Config();
		}
		return Config.instance;
	}

	public static void setInstance(final Config instance)
	{
		Config.instance = instance;
	}

	public void setHtmlGenerationEnabled(final boolean b)
	{
		this.generateHtml = b;
	}

	public boolean isHtmlGenerationEnabled()
	{
		if (this.generateHtml == null)
		{
			this.generateHtml = !this.options.remove("-nohtml");
		}
		return this.generateHtml;
	}

	public void setImpliedConstraintsEnabled(final boolean b)
	{
		this.includeImpliedConstraints = b;
	}

	public boolean isImpliedConstraintsEnabled()
	{
		if (this.includeImpliedConstraints == null)
		{
			this.includeImpliedConstraints = !this.options.remove("-noimplied");
		}
		return this.includeImpliedConstraints;
	}

	public void setOutputDir(String substring)
	{
		if (substring.endsWith("\""))
		{
			substring = substring.substring(0, substring.length() - 1);
		}
		this.setOutputDir(new File(substring));
	}

	public void setOutputDir(final File outputDir)
	{
		this.outputDir = outputDir;
	}

	public File getOutputDir()
	{
		if (this.outputDir == null)
		{
			this.setOutputDir(this.pullRequiredParam("-o"));
		}
		return this.outputDir;
	}

	public void setGraphvizDir(String substring)
	{
		if (substring.endsWith("\""))
		{
			substring = substring.substring(0, substring.length() - 1);
		}
		this.setGraphvizDir(new File(substring));
	}

	public void setGraphvizDir(final File graphvizDir)
	{
		this.graphvizDir = graphvizDir;
	}

	public File getGraphvizDir()
	{
		if (this.graphvizDir == null)
		{
			final String pullParam = this.pullParam("-gv");
			if (pullParam != null)
			{
				this.setGraphvizDir(pullParam);
			}
		}
		return this.graphvizDir;
	}

	public void setMeta(final String meta)
	{
		this.meta = meta;
	}

	public String getMeta()
	{
		if (this.meta == null)
		{
			this.meta = this.pullParam("-meta");
		}
		return this.meta;
	}

	public void setDbType(final String dbType)
	{
		this.dbType = dbType;
	}

	public String getDbType()
	{
		if (this.dbType == null)
		{
			this.dbType = this.pullParam("-t");
			if (this.dbType == null)
			{
				this.dbType = "ora";
			}
		}
		return this.dbType;
	}

	public void setDb(final String db)
	{
		this.db = db;
	}

	public String getDb()
	{
		if (this.db == null)
		{
			this.db = this.pullParam("-db");
		}
		return this.db;
	}

	public void setSchema(final String schema)
	{
		this.schema = schema;
	}

	public String getSchema()
	{
		if (this.schema == null)
		{
			this.schema = this.pullParam("-s");
		}
		return this.schema;
	}

	public boolean isSchemaDisabled()
	{
		if (this.noSchema == null)
		{
			this.noSchema = this.options.remove("-noschema");
		}
		return this.noSchema;
	}

	public void setHost(final String host)
	{
		this.host = host;
	}

	public String getHost()
	{
		if (this.host == null)
		{
			this.host = this.pullParam("-host");
		}
		return this.host;
	}

	public void setPort(final Integer port)
	{
		this.port = port;
	}

	public Integer getPort()
	{
		if (this.port == null)
		{
			try
			{
				this.port = Integer.valueOf(this.pullParam("-port"));
			}
			catch (Exception ex)
			{
			}
		}
		return this.port;
	}

	public void setServer(final String server)
	{
		this.server = server;
	}

	public String getServer()
	{
		if (this.server == null)
		{
			this.server = this.pullParam("-server");
		}
		return this.server;
	}

	public void setUser(final String user)
	{
		this.user = user;
	}

	public String getUser()
	{
		if (this.user == null)
		{
			if (!this.isSingleSignOn())
			{
				this.user = this.pullRequiredParam("-u");
			} else
			{
				this.user = this.pullParam("-u");
			}
		}
		return this.user;
	}

	public void setSingleSignOn(final boolean b)
	{
		this.singleSignOn = b;
	}

	public boolean isSingleSignOn()
	{
		if (this.singleSignOn == null)
		{
			this.singleSignOn = this.options.remove("-sso");
		}
		return this.singleSignOn;
	}

	public void setPassword(final String password)
	{
		this.password = password;
	}

	public String getPassword()
	{
		if (this.password == null)
		{
			this.password = this.pullParam("-p");
		}
		return this.password;
	}

	public void setPromptForPasswordEnabled(final boolean b)
	{
		this.promptForPassword = b;
	}

	public boolean isPromptForPasswordEnabled()
	{
		if (this.promptForPassword == null)
		{
			this.promptForPassword = this.options.remove("-pfp");
		}
		return this.promptForPassword;
	}

	public void setMaxDetailedTabled(final int value)
	{
		this.maxDetailedTables = new Integer(value);
	}

	public int getMaxDetailedTables()
	{
		if (this.maxDetailedTables == null)
		{
			int int1 = 300;
			try
			{
				int1 = Integer.parseInt(this.pullParam("-maxdet"));
			}
			catch (Exception ex)
			{
			}
			this.maxDetailedTables = new Integer(int1);
		}
		return this.maxDetailedTables;
	}

	public String getConnectionPropertiesFile()
	{
		return this.userConnectionPropertiesFile;
	}

	public void setConnectionPropertiesFile(final String s) throws FileNotFoundException, IOException
	{
		if (this.userConnectionProperties == null)
		{
			this.userConnectionProperties = new Properties();
		}
		this.userConnectionProperties.load(new FileInputStream(s));
		this.userConnectionPropertiesFile = s;
	}

	public Properties getConnectionProperties() throws FileNotFoundException, IOException
	{
		if (this.userConnectionProperties == null)
		{
			final String pullParam = this.pullParam("-connprops");
			if (pullParam != null)
			{
				if (pullParam.indexOf("\\=") != -1)
				{
					this.setConnectionProperties(pullParam);
				} else
				{
					this.setConnectionPropertiesFile(pullParam);
				}
			} else
			{
				this.userConnectionProperties = new Properties();
			}
		}
		return this.userConnectionProperties;
	}

	public void setConnectionProperties(final String str)
	{
		this.userConnectionProperties = new Properties();
		final StringTokenizer stringTokenizer = new StringTokenizer(str, ";");
		while (stringTokenizer.hasMoreElements())
		{
			final String nextToken = stringTokenizer.nextToken();
			final int index = nextToken.indexOf("\\=");
			if (index != -1)
			{
				this.userConnectionProperties
						.put(nextToken.substring(0, index), nextToken.substring(index + "\\=".length()));
			}
		}
	}

	public void setDriverPath(final String driverPath)
	{
		this.driverPath = driverPath;
	}

	public String getDriverPath()
	{
		if (this.driverPath == null)
		{
			this.driverPath = this.pullParam("-dp");
		}
		if (this.driverPath == null)
		{
			this.driverPath = this.pullParam("-cp");
		}
		return this.driverPath;
	}

	public void setCss(final String css)
	{
		this.css = css;
	}

	public String getCss()
	{
		if (this.css == null)
		{
			this.css = this.pullParam("-css");
			if (this.css == null)
			{
				this.css = "schemaSpy.css";
			}
		}
		return this.css;
	}

	public void setFont(final String font)
	{
		this.font = font;
	}

	public String getFont()
	{
		if (this.font == null)
		{
			this.font = this.pullParam("-font");
			if (this.font == null)
			{
				this.font = "Helvetica";
			}
		}
		return this.font;
	}

	public void setFontSize(final int value)
	{
		this.fontSize = new Integer(value);
	}

	public int getFontSize()
	{
		if (this.fontSize == null)
		{
			int int1 = 11;
			try
			{
				int1 = Integer.parseInt(this.pullParam("-fontsize"));
			}
			catch (Exception ex)
			{
			}
			this.fontSize = new Integer(int1);
		}
		return this.fontSize;
	}

	public void setCharset(final String charset)
	{
		this.charset = charset;
	}

	public String getCharset()
	{
		if (this.charset == null)
		{
			this.charset = this.pullParam("-charset");
			if (this.charset == null)
			{
				this.charset = "ISO-8859-1";
			}
		}
		return this.charset;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		if (this.description == null)
		{
			this.description = this.pullParam("-desc");
		}
		return this.description;
	}

	public void setMaxDbThreads(final int value)
	{
		this.maxDbThreads = new Integer(value);
	}

	public int getMaxDbThreads() throws InvalidConfigurationException
	{
		if (this.maxDbThreads == null)
		{
			Properties dbProperties;
			try
			{
				dbProperties = this.getDbProperties(this.getDbType());
			}
			catch (IOException obj)
			{
				throw new InvalidConfigurationException(
						"Failed to load properties for " + this.getDbType() + ": " + obj
				).setParamName("-type");
			}
			int value = Integer.MAX_VALUE;
			String s = dbProperties.getProperty("dbThreads");
			if (s == null)
			{
				s = dbProperties.getProperty("dbthreads");
			}
			if (s != null)
			{
				value = Integer.parseInt(s);
			}
			String s2 = this.pullParam("-dbThreads");
			if (s2 == null)
			{
				s2 = this.pullParam("-dbthreads");
			}
			if (s2 != null)
			{
				value = Integer.parseInt(s2);
			}
			if (value < 0)
			{
				value = Integer.MAX_VALUE;
			} else if (value == 0)
			{
				value = 1;
			}
			this.maxDbThreads = new Integer(value);
		}
		return this.maxDbThreads;
	}

	public boolean isLogoEnabled()
	{
		if (this.logoEnabled == null)
		{
			this.logoEnabled = !this.options.remove("-nologo");
		}
		return this.logoEnabled;
	}

	public void setRankDirBugEnabled(final boolean b)
	{
		this.rankDirBugEnabled = b;
	}

	public boolean isRankDirBugEnabled()
	{
		if (this.rankDirBugEnabled == null)
		{
			this.rankDirBugEnabled = this.options.remove("-rankdirbug");
		}
		return this.rankDirBugEnabled;
	}

	public void setRailsEnabled(final boolean b)
	{
		this.railsEnabled = b;
	}

	public boolean isRailsEnabled()
	{
		if (this.railsEnabled == null)
		{
			this.railsEnabled = this.options.remove("-rails");
		}
		return this.railsEnabled;
	}

	public void setEncodeCommentsEnabled(final boolean b)
	{
		this.encodeCommentsEnabled = b;
	}

	public boolean isEncodeCommentsEnabled()
	{
		if (this.encodeCommentsEnabled == null)
		{
			this.encodeCommentsEnabled = !this.options.remove("-ahic");
		}
		return this.encodeCommentsEnabled;
	}

	public void setNumRowsEnabled(final boolean b)
	{
		this.numRowsEnabled = b;
	}

	public boolean isNumRowsEnabled()
	{
		if (this.numRowsEnabled == null)
		{
			this.numRowsEnabled = !this.options.remove("-norows");
		}
		return this.numRowsEnabled;
	}

	public void setViewsEnabled(final boolean b)
	{
		this.viewsEnabled = b;
	}

	public boolean isViewsEnabled()
	{
		if (this.viewsEnabled == null)
		{
			this.viewsEnabled = !this.options.remove("-noviews");
		}
		return this.viewsEnabled;
	}

	public boolean isMeterEnabled()
	{
		if (this.meterEnabled == null)
		{
			this.meterEnabled = this.options.remove("-meter");
		}
		return this.meterEnabled;
	}

	public void setColumnExclusions(final String regex)
	{
		this.columnExclusions = Pattern.compile(regex);
	}

	public Pattern getColumnExclusions()
	{
		if (this.columnExclusions == null)
		{
			String pullParam = this.pullParam("-X");
			if (pullParam == null)
			{
				pullParam = "[^.]";
			}
			this.columnExclusions = Pattern.compile(pullParam);
		}
		return this.columnExclusions;
	}

	public void setIndirectColumnExclusions(final String regex)
	{
		this.indirectColumnExclusions = Pattern.compile(regex);
	}

	public Pattern getIndirectColumnExclusions()
	{
		if (this.indirectColumnExclusions == null)
		{
			String pullParam = this.pullParam("-x");
			if (pullParam == null)
			{
				pullParam = "[^.]";
			}
			this.indirectColumnExclusions = Pattern.compile(pullParam);
		}
		return this.indirectColumnExclusions;
	}

	public void setTableInclusions(final String regex)
	{
		this.tableInclusions = Pattern.compile(regex);
	}

	public Pattern getTableInclusions()
	{
		if (this.tableInclusions == null)
		{
			String pullParam = this.pullParam("-i");
			if (pullParam == null)
			{
				pullParam = ".*";
			}
			try
			{
				this.tableInclusions = Pattern.compile(pullParam);
			}
			catch (PatternSyntaxException ex)
			{
				throw new InvalidConfigurationException(ex).setParamName("-i");
			}
		}
		return this.tableInclusions;
	}

	public void setTableExclusions(final String regex)
	{
		this.tableExclusions = Pattern.compile(regex);
	}

	public Pattern getTableExclusions()
	{
		if (this.tableExclusions == null)
		{
			String pullParam = this.pullParam("-I");
			if (pullParam == null)
			{
				pullParam = "";
			}
			try
			{
				this.tableExclusions = Pattern.compile(pullParam);
			}
			catch (PatternSyntaxException ex)
			{
				throw new InvalidConfigurationException(ex).setParamName("-I");
			}
		}
		return this.tableExclusions;
	}

	public List<String> getSchemas()
	{
		if (this.schemas == null)
		{
			String s = this.pullParam("-schemas");
			if (s == null)
			{
				s = this.pullParam("-schemata");
			}
			if (s != null)
			{
				this.schemas = new ArrayList<String>();
				final String[] split = s.split("[ ,\"]");
				for (int length = split.length, i = 0; i < length; ++i)
				{
					this.schemas.add(split[i]);
				}
				if (this.schemas.isEmpty())
				{
					this.schemas = null;
				}
			}
		}
		return this.schemas;
	}

	public void setSqlFormatter(final String sqlFormatterClass)
	{
		this.sqlFormatterClass = sqlFormatterClass;
		this.sqlFormatter = null;
	}

	public void setSqlFormatter(final SqlFormatter sqlFormatter)
	{
		this.sqlFormatter = sqlFormatter;
		if (sqlFormatter != null)
		{
			this.sqlFormatterClass = sqlFormatter.getClass().getName();
		}
	}

	public SqlFormatter getSqlFormatter() throws InvalidConfigurationException
	{
		if (this.sqlFormatter == null)
		{
			if (this.sqlFormatterClass == null)
			{
				this.sqlFormatterClass = this.pullParam("-sqlFormatter");
				if (this.sqlFormatterClass == null)
				{
					this.sqlFormatterClass = DefaultSqlFormatter.class.getName();
				}
			}
			try
			{
				this.sqlFormatter = (SqlFormatter) Class.forName(this.sqlFormatterClass).newInstance();
			}
			catch (Exception ex)
			{
				throw new InvalidConfigurationException("Failed to initialize instance of SQL formatter: ", ex)
						.setParamName("-sqlFormatter");
			}
		}
		return this.sqlFormatter;
	}

	public void setEvaluateAllEnabled(final boolean b)
	{
		this.evaluteAll = b;
	}

	public boolean isEvaluateAllEnabled()
	{
		if (this.evaluteAll == null)
		{
			this.evaluteAll = this.options.remove("-all");
		}
		return this.evaluteAll;
	}

	public boolean isOneOfMultipleSchemas()
	{
		return Boolean.getBoolean("oneofmultipleschemas");
	}

	public void setSchemaSpec(final String schemaSpec)
	{
		this.schemaSpec = schemaSpec;
	}

	public String getSchemaSpec()
	{
		if (this.schemaSpec == null)
		{
			this.schemaSpec = this.pullParam("-schemaSpec");
		}
		return this.schemaSpec;
	}

	public void setRenderer(final String renderer)
	{
		Dot.getInstance().setRenderer(renderer);
	}

	public String getRenderer()
	{
		final String pullParam = this.pullParam("-renderer");
		if (pullParam != null)
		{
			this.setRenderer(pullParam);
		}
		return Dot.getInstance().getRenderer();
	}

	public void setHighQuality(final boolean b)
	{
		this.highQuality = b;
		this.lowQuality = !b;
		Dot.getInstance().setHighQuality(b);
	}

	public boolean isHighQuality()
	{
		if (this.highQuality == null)
		{
			this.highQuality = this.options.remove("-hq");
			if (this.highQuality)
			{
				Dot.getInstance().setHighQuality(this.highQuality);
			}
		}
		this.highQuality = Dot.getInstance().isHighQuality();
		return this.highQuality;
	}

	public boolean isLowQuality()
	{
		if (this.lowQuality == null)
		{
			this.lowQuality = this.options.remove("-lq");
			if (this.lowQuality)
			{
				Dot.getInstance().setHighQuality(!this.lowQuality);
			}
		}
		this.lowQuality = !Dot.getInstance().isHighQuality();
		return this.lowQuality;
	}

	public void setAdsEnabled(final boolean b)
	{
		this.adsEnabled = b;
	}

	public boolean isAdsEnabled()
	{
		if (this.adsEnabled == null)
		{
			this.adsEnabled = !this.options.remove("-noads");
		}
		return this.adsEnabled;
	}

	public void setLogLevel(final String str)
	{
		if (str == null)
		{
			this.logLevel = Level.WARNING;
			return;
		}
		final LinkedHashMap<Object, Level> linkedHashMap = new LinkedHashMap<Object, Level>();
		linkedHashMap.put("severe", Level.SEVERE);
		linkedHashMap.put("warning", Level.WARNING);
		linkedHashMap.put("info", Level.INFO);
		linkedHashMap.put("config", Level.CONFIG);
		linkedHashMap.put("fine", Level.FINE);
		linkedHashMap.put("finer", Level.FINER);
		linkedHashMap.put("finest", Level.FINEST);
		this.logLevel = (Level) linkedHashMap.get(str.toLowerCase());
		if (this.logLevel == null)
		{
			throw new InvalidConfigurationException(
					"Invalid logLevel: '" + str + "'. Must be one of: " + linkedHashMap.keySet()
			);
		}
	}

	public Level getLogLevel()
	{
		if (this.logLevel == null)
		{
			this.setLogLevel(this.pullParam("-loglevel"));
		}
		return this.logLevel;
	}

	public boolean isHelpRequired()
	{
		return this.helpRequired;
	}

	public boolean isDbHelpRequired()
	{
		return this.dbHelpRequired;
	}

	public static String getLoadedFromJar()
	{
		return new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator).nextToken();
	}

	public Properties getDbProperties(final String str) throws IOException, InvalidConfigurationException
	{
		ResourceBundle resourceBundle;
		try
		{
			final File file = new File(str);
			resourceBundle = new PropertyResourceBundle(new FileInputStream(file));
			this.dbPropertiesLoadedFrom = file.getAbsolutePath();
		}
		catch (FileNotFoundException ex3)
		{
			try
			{
				final File file2 = new File(str + ".properties");
				resourceBundle = new PropertyResourceBundle(new FileInputStream(file2));
				this.dbPropertiesLoadedFrom = file2.getAbsolutePath();
			}
			catch (FileNotFoundException ex2)
			{
				try
				{
					resourceBundle = ResourceBundle.getBundle(str);
					this.dbPropertiesLoadedFrom = "[" + getLoadedFromJar() + "]" + File.separator + str + ".properties";
				}
				catch (Exception ex4)
				{
					try
					{
						final String replace = (TableOrderer.class.getPackage().getName() + ".dbTypes." + str)
								.replace('.', '/');
						resourceBundle = ResourceBundle.getBundle(replace);
						this.dbPropertiesLoadedFrom = "[" + getLoadedFromJar() + "]/" + replace + ".properties";
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
						ex2.printStackTrace();
						throw ex3;
					}
				}
			}
		}
		Properties properties = asProperties(resourceBundle);
		final String dbPropertiesLoadedFrom = this.dbPropertiesLoadedFrom;
		int i = 1;
		while (true)
		{
			final String s = (String) properties.remove("include." + i);
			if (s == null)
			{
				final String s2 = (String) properties.remove("extends");
				if (s2 != null)
				{
					final Properties dbProperties = this.getDbProperties(s2.trim());
					dbProperties.putAll(properties);
					properties = dbProperties;
				}
				this.dbPropertiesLoadedFrom = dbPropertiesLoadedFrom;
				return properties;
			}
			final int index = s.indexOf("::");
			if (index == -1)
			{
				throw new InvalidConfigurationException(
						"include directive in " + this.dbPropertiesLoadedFrom + " must have '::' between dbType and key"
				);
			}
			final String trim = s.substring(0, index).trim();
			final String trim2 = s.substring(index + 2).trim();
			properties.put(trim2, this.getDbProperties(trim).getProperty(trim2));
			++i;
		}
	}

	protected String getDbPropertiesLoadedFrom() throws IOException
	{
		if (this.dbPropertiesLoadedFrom == null)
		{
			this.getDbProperties(this.getDbType());
		}
		return this.dbPropertiesLoadedFrom;
	}

	public List<String> getRemainingParameters()
	{
		try
		{
			this.populate();
		}
		catch (IllegalArgumentException ex)
		{
			throw new InvalidConfigurationException(ex);
		}
		catch (IllegalAccessException ex2)
		{
			throw new InvalidConfigurationException(ex2);
		}
		catch (InvocationTargetException ex3)
		{
			if (ex3.getCause() instanceof InvalidConfigurationException)
			{
				throw (InvalidConfigurationException) ex3.getCause();
			}
			throw new InvalidConfigurationException(ex3.getCause());
		}
		catch (IntrospectionException ex4)
		{
			throw new InvalidConfigurationException(ex4);
		}
		return this.options;
	}

	public void setDbSpecificOptions(final Map<String, String> map)
	{
		this.dbSpecificOptions = map;
		this.originalDbSpecificOptions = new HashMap<String, String>(map);
	}

	public Map<String, String> getDbSpecificOptions()
	{
		if (this.dbSpecificOptions == null)
		{
			this.dbSpecificOptions = new HashMap<String, String>();
		}
		return this.dbSpecificOptions;
	}

	public static Properties asProperties(final ResourceBundle resourceBundle)
	{
		final Properties properties = new Properties();
		final Enumeration<String> keys = resourceBundle.getKeys();
		while (keys.hasMoreElements())
		{
			final String s = keys.nextElement();
			properties.put(s, resourceBundle.getObject(s));
		}
		return properties;
	}

	private String pullParam(final String s)
	{
		return this.pullParam(s, false, false);
	}

	private String pullRequiredParam(final String s)
	{
		return this.pullParam(s, true, false);
	}

	private String pullParam(final String s, final boolean b, final boolean b2) throws MissingRequiredParameterException
	{
		final int index = this.options.indexOf(s);
		if (index >= 0)
		{
			this.options.remove(index);
			final String string = this.options.get(index).toString();
			this.options.remove(index);
			return string;
		}
		if (b)
		{
			throw new MissingRequiredParameterException(s, b2);
		}
		return null;
	}

	protected List<String> fixupArgs(final List<String> list)
	{
		final ArrayList<String> list2 = new ArrayList<String>();
		for (final String s : list)
		{
			final int index = s.indexOf(61);
			if (index != -1 && index - 1 != s.indexOf("\\="))
			{
				list2.add(s.substring(0, index));
				list2.add(s.substring(index + 1));
			} else
			{
				list2.add(s);
			}
		}
		final ArrayList<String> list3 = new ArrayList<String>();
		for (String substring : list2)
		{
			if (substring.startsWith("\"") && substring.endsWith("\""))
			{
				substring = substring.substring(1, substring.length() - 1);
			}
			list3.add(substring);
		}
		return list3;
	}

	private void populate()
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IntrospectionException
	{
		if (!this.populating)
		{
			this.populating = true;
			final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(Config.class)
					.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; ++i)
			{
				final Method readMethod = propertyDescriptors[i].getReadMethod();
				if (readMethod != null)
				{
					readMethod.invoke(this, (Object[]) null);
				}
			}
			this.populating = false;
		}
	}

	public static Set<String> getBuiltInDatabaseTypes(final String name)
	{
		final TreeSet<String> set = new TreeSet<String>();
		JarInputStream jarInputStream = null;
		try
		{
			jarInputStream = new JarInputStream(new FileInputStream(name));
			JarEntry nextJarEntry;
			while ((nextJarEntry = jarInputStream.getNextJarEntry()) != null)
			{
				final String name2 = nextJarEntry.getName();
				final int index = name2.indexOf(".properties");
				if (index != -1)
				{
					set.add(name2.substring(0, index));
				}
			}
		}
		catch (IOException ex)
		{
		}
		finally
		{
			if (jarInputStream != null)
			{
				try
				{
					jarInputStream.close();
				}
				catch (IOException ex2)
				{
				}
			}
		}
		return set;
	}

	protected void dumpUsage(final String str, final boolean b)
	{
		if (str != null)
		{
			System.out.flush();
			System.err.println("*** " + str + " ***");
		} else
		{
			System.out.println("SchemaSpy generates an HTML representation of a database schema's relationships.");
		}
		System.err.flush();
		System.out.println();
		if (!b)
		{
			System.out.println("Usage:");
			System.out.println(" java -jar " + getLoadedFromJar() + " [options]");
			System.out.println("   -t databaseType       type of database - defaults to ora");
			System.out.println("                           use -dbhelp for a list of built-in types");
			System.out.println("   -u user               connect to the database with this user id");
			System.out.println("   -s schema             defaults to the specified user");
			System.out.println("   -p password           defaults to no password");
			System.out.println("   -o outputDirectory    directory to place the generated output in");
			System.out.println("   -dp pathToDrivers     optional - looks for JDBC drivers here before looking");
			System.out.println("                           in driverPath in [databaseType].properties.");
			System.out.println("Go to http://schemaspy.sourceforge.net for a complete list/description");
			System.out.println(" of additional parameters.");
			System.out.println();
		}
		if (b)
		{
			System.out.println("Built-in database types and their required connection parameters:");
			final Iterator<String> iterator = getBuiltInDatabaseTypes(getLoadedFromJar()).iterator();
			while (iterator.hasNext())
			{
				new DbSpecificConfig(iterator.next()).dumpUsage();
			}
			System.out.println();
		}
		if (b)
		{
			System.out.println(
					"You can use your own database types by specifying the filespec of a .properties file with -t."
			);
			System.out.println("Grab one out of " + getLoadedFromJar() + " and modify it to suit your needs.");
			System.out.println();
		}
		System.out.println("Sample usage using the default database type (implied -t ora):");
		System.out.println(" java -jar schemaSpy.jar -db mydb -s myschema -u devuser -p password -o output");
		System.out.println();
		System.out.flush();
	}

	public String getParam(final String anotherString)
	{
		try
		{
			final PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(Config.class)
					.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; ++i)
			{
				final PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
				if (propertyDescriptor.getName().equalsIgnoreCase(anotherString))
				{
					final Object invoke = propertyDescriptor.getReadMethod().invoke(this, (Object[]) null);
					return (invoke == null) ? null : invoke.toString();
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	public List<String> asList() throws IOException
	{
		final ArrayList<String> list = new ArrayList<String>();
		if (this.originalDbSpecificOptions != null)
		{
			for (String string : this.originalDbSpecificOptions.keySet())
			{
				final String s = this.originalDbSpecificOptions.get(string);
				if (!string.startsWith("-"))
				{
					string = "-" + string;
				}
				list.add(string);
				list.add(s);
			}
		}
		if (this.isEncodeCommentsEnabled())
		{
			list.add("-ahic");
		}
		if (this.isEvaluateAllEnabled())
		{
			list.add("-all");
		}
		if (!this.isHtmlGenerationEnabled())
		{
			list.add("-nohtml");
		}
		if (!this.isImpliedConstraintsEnabled())
		{
			list.add("-noimplied");
		}
		if (!this.isLogoEnabled())
		{
			list.add("-nologo");
		}
		if (this.isMeterEnabled())
		{
			list.add("-meter");
		}
		if (!this.isNumRowsEnabled())
		{
			list.add("-norows");
		}
		if (!this.isViewsEnabled())
		{
			list.add("-noviews");
		}
		if (this.isRankDirBugEnabled())
		{
			list.add("-rankdirbug");
		}
		if (this.isRailsEnabled())
		{
			list.add("-rails");
		}
		if (this.isSingleSignOn())
		{
			list.add("-sso");
		}
		if (!this.isAdsEnabled())
		{
			list.add("-noads");
		}
		if (this.isSchemaDisabled())
		{
			list.add("-noschema");
		}
		final String driverPath = this.getDriverPath();
		if (driverPath != null)
		{
			list.add("-dp");
			list.add(driverPath);
		}
		list.add("-css");
		list.add(this.getCss());
		list.add("-charset");
		list.add(this.getCharset());
		list.add("-font");
		list.add(this.getFont());
		list.add("-fontsize");
		list.add(String.valueOf(this.getFontSize()));
		list.add("-t");
		list.add(this.getDbType());
		list.add("-renderer");
		list.add(this.getRenderer());
		final String description = this.getDescription();
		if (description != null)
		{
			list.add("-desc");
			list.add(description);
		}
		final String password = this.getPassword();
		if (password != null)
		{
			list.add("-p");
			list.add(password);
		}
		if (this.isPromptForPasswordEnabled())
		{
			list.add("-pfp");
		}
		final String schema = this.getSchema();
		if (schema != null)
		{
			list.add("-s");
			list.add(schema);
		}
		final String user = this.getUser();
		if (user != null)
		{
			list.add("-u");
			list.add(user);
		}
		final String connectionPropertiesFile = this.getConnectionPropertiesFile();
		if (connectionPropertiesFile != null)
		{
			list.add("-connprops");
			list.add(connectionPropertiesFile);
		} else
		{
			final Properties connectionProperties = this.getConnectionProperties();
			if (!connectionProperties.isEmpty())
			{
				list.add("-connprops");
				final StringBuilder sb = new StringBuilder();
				for (final Map.Entry<Object, Object> entry : connectionProperties.entrySet())
				{
					sb.append(entry.getKey());
					sb.append("\\=");
					sb.append(entry.getValue());
					sb.append(';');
				}
				list.add(sb.toString());
			}
		}
		final String db = this.getDb();
		if (db != null)
		{
			list.add("-db");
			list.add(db);
		}
		final String host = this.getHost();
		if (host != null)
		{
			list.add("-host");
			list.add(host);
		}
		if (this.getPort() != null)
		{
			list.add("-port");
			list.add(this.getPort().toString());
		}
		final String server = this.getServer();
		if (server != null)
		{
			list.add("-server");
			list.add(server);
		}
		final String meta = this.getMeta();
		if (meta != null)
		{
			list.add("-meta");
			list.add(meta);
		}
		if (this.getGraphvizDir() != null)
		{
			list.add("-gv");
			list.add(this.getGraphvizDir().toString());
		}
		list.add("-loglevel");
		list.add(this.getLogLevel().toString().toLowerCase());
		list.add("-sqlFormatter");
		list.add(this.getSqlFormatter().getClass().getName());
		list.add("-i");
		list.add(this.getTableInclusions().pattern());
		list.add("-I");
		list.add(this.getTableExclusions().pattern());
		list.add("-x");
		list.add(this.getColumnExclusions().pattern());
		list.add("-X");
		list.add(this.getIndirectColumnExclusions().pattern());
		list.add("-dbthreads");
		list.add(String.valueOf(this.getMaxDbThreads()));
		list.add("-maxdet");
		list.add(String.valueOf(this.getMaxDetailedTables()));
		list.add("-o");
		list.add(this.getOutputDir().toString());
		return list;
	}

	public static class MissingRequiredParameterException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		private final boolean dbTypeSpecific;

		public MissingRequiredParameterException(final String s, final boolean b)
		{
			this(s, null, b);
		}

		public MissingRequiredParameterException(final String str, final String str2, final boolean dbTypeSpecific)
		{
			super(
					"Required parameter '" + str + "' " + ((str2 == null) ? "" : ("(" + str2 + ") "))
							+ "was not specified." + (dbTypeSpecific ? "  It is required for this database type." : "")
			);
			this.dbTypeSpecific = dbTypeSpecific;
		}

		public boolean isDbTypeSpecific()
		{
			return this.dbTypeSpecific;
		}
	}
}
