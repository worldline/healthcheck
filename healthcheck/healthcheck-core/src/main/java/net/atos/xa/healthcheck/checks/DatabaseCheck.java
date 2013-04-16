package net.atos.xa.healthcheck.checks;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.HealthCheck;

/**
 * 
 * <p>
 * A check to test the connection to a DB.
 * 
 * This check requires a datasource on which the connection test is done.
 * Optionnally, a validation SQL query and its associated timeout can be given.
 * 
 * If no validation query is given in parameter, a predefined query will be
 * executed depending on the JDBC driver used
 * </p>
 * <br/>
 * <br/>
 * 
 * List of predefined queries per JDBC driver: <br/>
 * org.hsqldb.jdbcDriver = "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS"<br/>
 * oracle.jdbc.driver.OracleDriver = "select 1 from dual"<br/>
 * com.mysql.jdbc.Driver or org.gjt.mm.mysql.Driver = "select 1"<br />
 * com.ibm.db2.jcc.DB2Driver = "select 1 from sysibm.sysdummy1" <br/>
 * com.microsoft.jdbc.sqlserver.SQLServerDriver = "select 1" <br/>
 * org.postgresql.Driver = "select version()"<br/>
 * com.ingres.jdbc.IngresDriver = "select 1"<br/>
 * org.apache.derby.jdbc.ClientDriver = "values 1"<br/>
 * org.h2.Driver = "select 1"<br/>
 * 
 */
public class DatabaseCheck extends HealthCheck {

	/** the logger */
	private static Logger log = LoggerFactory.getLogger(DatabaseCheck.class
			.getName());

	private static final String SELECT_1_QUERY = "select 1";

	private static final Map<String, String> PREDEFINED_VALIDATION_QUERIES;
	static {
		Map<String, String> aMap = new HashMap<String, String>();
		// hsqldb
		aMap.put("org.hsqldb.jdbcDriver",
				"select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");

		// oracle
		aMap.put("oracle.jdbc.driver.OracleDriver", "select 1 from dual");

		// mysql
		aMap.put("com.mysql.jdbc.Driver", SELECT_1_QUERY);
		aMap.put("org.gjt.mm.mysql.Driver", SELECT_1_QUERY);

		// db2
		aMap.put("com.ibm.db2.jcc.DB2Driver", "select 1 from sysibm.sysdummy1");

		// microsoft sql
		aMap.put("com.microsoft.jdbc.sqlserver.SQLServerDriver", SELECT_1_QUERY);

		// postgresql
		aMap.put("org.postgresql.Driver", "select version()");

		// ingres
		aMap.put("com.ingres.jdbc.IngresDriver", SELECT_1_QUERY);

		// derby
		aMap.put("org.apache.derby.jdbc.ClientDriver", "values 1");

		// h2
		aMap.put("org.h2.Driver", SELECT_1_QUERY);

		PREDEFINED_VALIDATION_QUERIES = Collections.unmodifiableMap(aMap);
	}

	/** the datasource on which the test is done */
	private DataSource dataSource;
	/** the validation SQL query for the checking the database connection */
	private String validationQuery;
	/** the validation query timeout (if timeout is exceeded, the check fails) */
	private int validationQueryTimeout;

	/** the classname of the jdbc driver */
	private String jdbcDriver;

	/**
	 * 
	 * @param name
	 *            a name for this health check
	 * @param dataSource
	 *            on which the test is done
	 * @param validationQuery
	 *            SQL query for the checking the database connection, if null a
	 *            default SQL query is selected among some predefined ones
	 * @param validationQueryTimeout
	 *            validation query timeout , if timeout is exceeded, the check
	 *            fails
	 */
	public DatabaseCheck(String name, DataSource dataSource,
			String validationQuery, String jdbcDriver,
			int validationQueryTimeout) {
		super("databaseCheck " + name);
		this.dataSource = dataSource;
		this.validationQuery = validationQuery;
		this.validationQueryTimeout = validationQueryTimeout;
		this.jdbcDriver = jdbcDriver;
	}

	@Override
	protected Result check() throws Exception {

		log.info("[HealthCheck] execute check {}", getName());

		if (dataSource == null) {
			return Result.unhealthy("no datasource provided");
		}

		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = dataSource.getConnection();

			if (validationQuery == null) {
				// use of a predefined (can be different depending on the
				// database used
				String jdbcDriverUsed = jdbcDriver;
				if (jdbcDriverUsed == null) {
					Driver driver = DriverManager.getDriver(connection
							.getMetaData().getURL());
					jdbcDriverUsed = driver.getClass().getName();
				}

				log.debug("[HealthCheck] jdbc driver class name : {} ",
						jdbcDriverUsed);

				validationQuery = PREDEFINED_VALIDATION_QUERIES
						.get(jdbcDriverUsed);

				if (validationQuery == null) {
					log.debug(
							"[HealthCheck] predefined validation query is null, use the default {}",
							SELECT_1_QUERY);

					validationQuery = SELECT_1_QUERY;
				}

			}

			log.info(
					"[HealthCheck] execute validationQuery {} on database {} with timeout {} and jdbc driver {} v{}",
					validationQuery, connection.getMetaData().getURL(),
					validationQueryTimeout, connection.getMetaData()
							.getDriverName(), connection.getMetaData()
							.getDriverVersion());

			stmt = connection.prepareStatement(validationQuery);

			if (validationQueryTimeout > 0) {
				stmt.setQueryTimeout(validationQueryTimeout);
			}

			rs = stmt.executeQuery();

		} catch (SQLException e) {
			log.error("Cannot access database", e);

			return Result.unhealthy("Cannot connect to " + getName(), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					log.error("Cannot close database connection", e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					log.error("Cannot close statement", e);
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					log.error("Cannot close result set", e);
				}
			}
		}

		log.info("[HealthCheck] check {} is OK", getName());

		return Result.healthy();

	}
}
