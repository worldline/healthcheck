package net.atos.xa.healthcheck.checks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.yammer.metrics.core.HealthCheck;

public class DatabaseCheck extends HealthCheck {

	/** the logger (use the same logging mechanism as the activiti framework */
	private static Logger log = Logger.getLogger(DatabaseCheck.class.getName());

	private DataSource dataSource;
	private String validatinQuery;
	private int validationQueryTimeout;

	public DatabaseCheck(String name, DataSource dataSource,
			String validationQuery, int validationQueryTimeout) {
		super(name);
		this.dataSource = dataSource;
		this.validatinQuery = validationQuery;
		this.validationQueryTimeout = validationQueryTimeout;
	}

	@Override
	protected Result check() throws Exception {

		if (dataSource == null)
			return Result.healthy();

		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = dataSource.getConnection();
			if (validatinQuery != null)
				stmt = connection.prepareStatement(validatinQuery);
			else
				// TODO determine the database used and run a correct validation
				// query
				stmt = connection.prepareStatement("select 1 from dual");

			if (validationQueryTimeout > 0) {
				stmt.setQueryTimeout(validationQueryTimeout);
			}
			rs = stmt.executeQuery();

		} catch (SQLException e) {
			log.log(Level.SEVERE, "Cannot access database", e);

			Result.unhealthy("Cannot connect to " + getName(), e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception e) {
					// no-op
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					// no-op
				}
			}
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					// no-op
				}
			}
		}

		return Result.healthy();

	}
}
