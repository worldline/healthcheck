package net.atos.xa.healthcheck.checks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.HealthCheck;

public class DatabaseCheck extends HealthCheck {

	/** the logger */
	private static Logger log = LoggerFactory.getLogger(DatabaseCheck.class
			.getName());

	private DataSource dataSource;
	private String validationQuery;
	private int validationQueryTimeout;

	public DatabaseCheck(String name, DataSource dataSource,
			String validationQuery, int validationQueryTimeout) {
		super("databaseCheck " + name);
		this.dataSource = dataSource;
		if (validationQuery != null)
			this.validationQuery = validationQuery;
		else
			this.validationQuery = "select 1 from dual";
		this.validationQueryTimeout = validationQueryTimeout;
	}

	@Override
	protected Result check() throws Exception {

		log.info("[HealthCheck] execute check {}", getName());

		if (dataSource == null)
			return Result.unhealthy("no datasource provided");

		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = dataSource.getConnection();

			log.info(
					"[HealthCheck] execute validationQuery {} on database {} with timeout {}",
					validationQuery, connection.getMetaData().getURL(),
					validationQueryTimeout);

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
