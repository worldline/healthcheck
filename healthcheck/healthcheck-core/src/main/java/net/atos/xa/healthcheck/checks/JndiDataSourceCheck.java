package net.atos.xa.healthcheck.checks;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.HealthCheck;

/**
 * 
 * <p>
 * A check to test the connection to a DB referenced by using JNDI
 * 
 * Require :
 * <ul>
 * <li>A JNDI context</li>
 * <li>A JNDI name which references the datasource</li>
 * <li>A query timeout in seconds. If timeout is exceeded, the checks fails.</li>
 * </ul>
 * </p>
 */
public class JndiDataSourceCheck extends HealthCheck {

	/** the logger */
	private static Logger log = LoggerFactory.getLogger(DatabaseCheck.class
			.getName());

	private Context jndiContext;

	private String jndiName;

	private int queryTimeout;

	/**
	 * 
	 * @param jndiContext
	 *            the JNDI context
	 * @param jndiName
	 *            the JNDI name
	 * @param queryTimeout
	 *            query timeout in seconds , if timeout is exceeded, the check
	 *            fails
	 */
	public JndiDataSourceCheck(Context jndiContext, String jndiName,
			int queryTimeout) {
		super("databaseCheck " + jndiName);
		this.jndiContext = jndiContext;
		this.jndiName = jndiName;
		this.queryTimeout = queryTimeout;
	}

	@Override
	protected Result check() throws Exception {
		return getHealthCheck(jndiContext, jndiName, queryTimeout).execute();
	}

	private HealthCheck getHealthCheck(Context context, String jndi,
			int queryTimeout) {

		log.debug("[HealthCheck] create database check for jndi name {}", jndi);

		try {
			DataSource ds = (DataSource) context.lookup(jndi);

			return new DatabaseCheck(jndi, ds, null, null, queryTimeout);

		} catch (NamingException e) {
			log.warn("[HealthCheck] the JNDI name " + jndi + " cannot be found");
			return new DatabaseCheck(jndi, null, null, null, queryTimeout);
		}

	}

}
