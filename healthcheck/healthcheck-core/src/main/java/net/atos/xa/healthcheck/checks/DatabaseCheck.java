package net.atos.xa.healthcheck.checks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.atos.xa.healthcheck.util.JdbcUtil;

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
 * oracle.jdbc.driver.OracleDriver or oracle.jdbc.OracleDriver =
 * "select 1 from dual"<br/>
 * com.mysql.jdbc.Driver or org.gjt.mm.mysql.Driver = "select 1"<br />
 * com.ibm.db2.jcc.DB2Driver = "select 1 from sysibm.sysdummy1" <br/>
 * com.microsoft.jdbc.sqlserver.SQLServerDriver = "select 1" <br/>
 * org.postgresql.Driver = "select version()"<br/>
 * com.ingres.jdbc.IngresDriver = "select 1"<br/>
 * org.apache.derby.jdbc.ClientDriver = "values 1"<br/>
 * org.h2.Driver = "select 1"<br/>
 * 
 * 
 * <p>
 * 
 * <h2>Timeout management</h2>
 * 
 * Be careful to the timeout configuration! With a wrong timeout configuration
 * or if you keep the default configuration, the database check is blocked until
 * it gets a response from the database. This leads to an unefficient
 * monitoring.
 * 
 * Two timeout can be configured:
 * <ul>
 * <li><b>login timeout</b> : configured at the datasource level. Maximum time
 * in seconds that a data source will wait while attempting to connect to a
 * database. A value of zero specifies that the timeout is the default system
 * timeout if there is one; otherwise, it specifies that there is no timeout.</li>
 * <li><b>validation query timeout</b> : the validation query timeout in seconds
 * (if timeout is exceeded, the check fails)</li>
 * </ul>
 * 
 * You may have different configuration for timeout depending on the JDBC driver
 * you use.
 * 
 * Example in MySQL driver, you can configure a connection timeout and a socket
 * timeout
 * 
 * </p>
 * 
 * 
 */
public class DatabaseCheck extends HealthCheck {

	/** the logger */
	private static Logger log = LoggerFactory.getLogger(DatabaseCheck.class
			.getName());

	/**
	 * the datasource on which the test is done. As a precondition, we consider
	 * that the login timeout is set up in this datasource
	 */
	private DataSource dataSource;
	/** the validation SQL query for the checking the database connection */
	private String validationQuery;
	/**
	 * the validation query timeout in seconds (if timeout is exceeded, the
	 * check fails)
	 */
	private int validationQueryTimeout;

	/** the classname of the jdbc driver */
	private String jdbcDriver;

	/**
	 * 
	 * @param name
	 *            a name for this health check
	 * @param dataSource
	 *            on which the test is done. As a precondition, we consider that
	 *            login timeout is set up in this datasource
	 * @param validationQuery
	 *            SQL query for the checking the database connection, if null a
	 *            default SQL query is selected among some predefined ones
	 * @param validationQueryTimeout
	 *            validation query timeout in seconds , if timeout is exceeded,
	 *            the check fails
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

		log.info("[HealthCheck] execute database check \"{}\"", getName());

		if (dataSource == null) {
			return Result.unhealthy("no datasource found");
		}

		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = dataSource.getConnection();
			if (connection == null) {
				return Result.unhealthy("connection is null");
			}

			if (validationQuery == null) {
				// use of a predefined (can be different depending on the
				// database used
				String jdbcDriverUsed = jdbcDriver;

				if (jdbcDriverUsed == null) { // get jdbc driver from url
					jdbcDriverUsed = JdbcUtil
							.identifyJdbcDriverFromUrl(connection.getMetaData()
									.getURL());
				}

				if (jdbcDriverUsed == null) { // no suitable driver found

					// try to get a jdbc driver from the name
					jdbcDriverUsed = JdbcUtil
							.identifyJdbcDriverFromName(connection
									.getMetaData().getDriverName());
				}

				if (jdbcDriverUsed != null) {
					validationQuery = JdbcUtil
							.getValidationQueryFromJdbcDriver(jdbcDriverUsed);
				}

				if (validationQuery == null) {
					log.warn(
							"[HealthCheck] predefined validation query is null, use the default \"{}\"",
							JdbcUtil.SELECT_1_QUERY);

					validationQuery = JdbcUtil.SELECT_1_QUERY;
				}

			}

			log.info(
					"[HealthCheck] execute validationQuery \"{}\" on database \"{}\" with timeout {} and jdbc driver \"{} v{}\"",
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

			return Result.unhealthy(
					"Cannot connect to " + getName() + " (" + e.getMessage()
							+ ")", e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					log.error("[HealthCheck] Cannot close database connection",
							e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					log.error("[HealthCheck] Cannot close statement", e);
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					log.error("[HealthCheck] Cannot close result set", e);
				}
			}
		}

		log.info("[HealthCheck] check {} is OK", getName());

		return Result.healthy();

	}

}
