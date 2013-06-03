package net.atos.xa.healthcheck.util;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.atos.xa.healthcheck.checks.DatabaseCheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for selecting jdbc drivers and database validation queries
 * 
 * 
 */
public class JdbcUtil {

	/** the logger */
	private static Logger log = LoggerFactory.getLogger(DatabaseCheck.class
			.getName());

	public static final String SELECT_1_QUERY = "select 1";
	public static final String SELECT_1_FROM_DUAL_QUERY = "select 1 from dual";

	public static final String JDBC_DRIVER_HSQLDB = "org.hsqldb.jdbcDriver";
	public static final String JDBC_DRIVER_ORACLE = "oracle.jdbc.OracleDriver";
	public static final String JDBC_DRIVER_ORACLE_ALTERNATE = "oracle.jdbc.driver.OracleDriver";
	public static final String JDBC_DRIVER_MYSQL = "com.mysql.jdbc.Driver";
	public static final String JDBC_DRIVER_MYSQL_ALTERNATE = "org.gjt.mm.mysql.Driver";
	public static final String JDBC_DRIVER_DB2 = "com.ibm.db2.jcc.DB2Driver";
	public static final String JDBC_DRIVER_SQLSERVER = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
	public static final String JDBC_DRIVER_SQLSERVER_ALTERNATE = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static final String JDBC_DRIVER_POSTGRESQL = "org.postgresql.Driver";
	public static final String JDBC_DRIVER_INGRES = "com.ingres.jdbc.IngresDriver";
	public static final String JDBC_DRIVER_DERBY = "org.apache.derby.jdbc.ClientDriver";
	public static final String JDBC_DRIVER_DERBY_ALTERNATE = "org.apache.derby.jdbc.AutoloadedDriver";
	public static final String JDBC_DRIVER_H2 = "org.h2.Driver";

	private static final Map<String, String> PREDEFINED_VALIDATION_QUERIES;
	private static final Map<String, String> JDBC_DRIVER_FROM_NAME;

	static {
		Map<String, String> aMap = new HashMap<String, String>();
		// hsqldb
		aMap.put(JDBC_DRIVER_HSQLDB,
				"select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");

		// oracle
		aMap.put(JDBC_DRIVER_ORACLE, SELECT_1_FROM_DUAL_QUERY);
		aMap.put(JDBC_DRIVER_ORACLE_ALTERNATE, SELECT_1_FROM_DUAL_QUERY);

		// mysql
		aMap.put(JDBC_DRIVER_MYSQL, SELECT_1_QUERY);
		aMap.put(JDBC_DRIVER_MYSQL_ALTERNATE, SELECT_1_QUERY);

		// db2
		aMap.put(JDBC_DRIVER_DB2, "select 1 from sysibm.sysdummy1");

		// microsoft sql
		aMap.put(JDBC_DRIVER_SQLSERVER, SELECT_1_QUERY);
		aMap.put(JDBC_DRIVER_SQLSERVER_ALTERNATE, SELECT_1_QUERY);

		// postgresql
		aMap.put(JDBC_DRIVER_POSTGRESQL, "select version()");

		// ingres
		aMap.put(JDBC_DRIVER_INGRES, SELECT_1_QUERY);

		// derby
		aMap.put(JDBC_DRIVER_DERBY, "values 1");
		aMap.put(JDBC_DRIVER_DERBY_ALTERNATE, "values 1");

		// h2
		aMap.put(JDBC_DRIVER_H2, SELECT_1_QUERY);

		PREDEFINED_VALIDATION_QUERIES = Collections.unmodifiableMap(aMap);

	}

	static {

		Map<String, String> aMap = new HashMap<String, String>();

		// hsqldb
		aMap.put("hsqldb", JDBC_DRIVER_HSQLDB);

		// oracle
		aMap.put("oracle", JDBC_DRIVER_ORACLE);

		// mysql
		aMap.put("mysql", JDBC_DRIVER_MYSQL);

		// db2
		aMap.put("db2", JDBC_DRIVER_DB2);

		// microsoft sql
		aMap.put("sqlserver", JDBC_DRIVER_SQLSERVER);

		// postgresql
		aMap.put("postgresql", JDBC_DRIVER_POSTGRESQL);

		// ingres
		aMap.put("ingres", JDBC_DRIVER_INGRES);

		// derby
		aMap.put("derby", JDBC_DRIVER_DERBY);

		// h2
		aMap.put("h2", JDBC_DRIVER_H2);

		JDBC_DRIVER_FROM_NAME = Collections.unmodifiableMap(aMap);

	}

	/**
	 * get the jdbc driver class name from the url
	 * 
	 * @param jdbcUrl
	 *            , the jdbc url (e.g. jdbc:oracle:thin:@[HOST][:PORT]:SID for
	 *            Oracle).
	 * @return the jdbc driver class name (e.g.
	 *         "oracle.jdbc.driver.OracleDriver") if found; return null,
	 *         otherwise
	 */
	public static String identifyJdbcDriverFromUrl(String jdbcUrl) {
		Driver driver;

		String jdbcDriverUsed = null;
		try {
			// found the jdbc driver from the jdbc url
			driver = DriverManager.getDriver(jdbcUrl);
			jdbcDriverUsed = driver.getClass().getName();

			log.debug(
					"[HealthCheck] jdbc driver class name found thanks to java.sql.DriverManager : \"{}\" ",
					jdbcDriverUsed);

		} catch (Exception e) {
			log.debug(
					"[HealthCheck] error when trying to detect jdbc driver ({})",
					e.getMessage());
		}

		return jdbcDriverUsed;

	}

	/**
	 * get the jdbc driver class name from its name (e.g. "Oracle JDBC driver"
	 * for oracle)
	 * 
	 * @param jdbcdriverName
	 *            the jdbc driver name (information produced by the jdbc driver)
	 * @return the jdbc driver class name (e.g.
	 *         "oracle.jdbc.driver.OracleDriver") if found; return null,
	 *         otherwise
	 */
	public static String identifyJdbcDriverFromName(String jdbcDriverName) {

		String jdbcDriverUsed = null;

		if (jdbcDriverName != null) {

			// identify a key in JDBC_DRIVER_FROM_NAME which is contained in the
			// driver name
			boolean found = false;
			Iterator<String> keys = JDBC_DRIVER_FROM_NAME.keySet().iterator();
			String key = null;
			while (!found && keys.hasNext()) {
				key = keys.next();
				found = jdbcDriverName.toLowerCase().contains(key);
			}

			if (found) {
				jdbcDriverUsed = JDBC_DRIVER_FROM_NAME.get(key);

				log.debug(
						"[HealthCheck] jdbc driver class name found thanks to the driver name \"{}\" : \"{}\" ",
						jdbcDriverName, jdbcDriverUsed);

			}

		}

		return jdbcDriverUsed;

	}

	/**
	 * 
	 * @param jdbcDriver
	 * @return
	 */
	public static String getValidationQueryFromJdbcDriver(String jdbcDriver) {

		return (jdbcDriver != null) ? PREDEFINED_VALIDATION_QUERIES
				.get(jdbcDriver) : null;

	}
}
