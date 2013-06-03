package net.atos.xa.healthcheck.was;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.atos.xa.healthcheck.checks.DatabaseCheck;
import net.atos.xa.healthcheck.spi.HealthCheckFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.HealthCheck;

/**
 * 
 * <p>
 * Implementation of {@link HealthCheckFactory} for checking IBM WebSphere
 * datasources datasources This implementation requires the following
 * environmment variables:
 * <ul>
 * <li>wasCheck.jdbcDatasources : a list of JNDI names of the datasource you
 * want to check</li>
 * 
 * </ul>
 * 
 * if you use healthcheck servlet, this environment variables are set up in the
 * servlet init parameter
 * </p>
 */
public class DatasourceHealthCheckFactory implements HealthCheckFactory {

	/** the logger */
	private static Logger log = LoggerFactory
			.getLogger(DatasourceHealthCheckFactory.class.getName());

	private static final String JDBC_DATASOURCE_LIST_KEY = "wasCheck.jdbcDatasources";

	/**
	 * @param environment
	 *            environment variable used for the setup of the checks <br>
	 *            This is an optional option, it depends on the checks you use.
	 *            Please consult the javadoc of the checks you use for details
	 *            about the possible variable environments
	 */
	public List<HealthCheck> getHealthChecks(Map<String, String> environment) {

		if (log.isDebugEnabled()) {

			StringBuffer buffer = new StringBuffer();
			if (environment != null) {
				Set<String> keys = environment.keySet();
				for (String key : keys) {
					buffer.append("key " + key + " : " + environment.get(key)
							+ "\n");
				}
			} else {
				buffer.append("environment is null");
			}

			log.debug("[HealthCheck] IBM WAS datasource check factory with environment : ");
			log.debug(buffer.toString());

		}

		List<HealthCheck> healthchecks = null;
		if (environment != null
				&& environment.get(JDBC_DATASOURCE_LIST_KEY) != null) {

			Hashtable<String, String> env = new Hashtable<String, String>();
			// env.put("java.naming.factory.initial",
			// "com.ibm.websphere.naming.WsnInitialContextFactory");

			InitialContext context = null;
			try {
				context = new InitialContext(env);

			} catch (NamingException e) {
				log.error(
						"[HealthCheck] error when instantiating the jndi context "
								+ e.getMessage(), e);
				throw new RuntimeException(e.getMessage());
			}

			String jndiNamesForDatasource = environment.get(
					JDBC_DATASOURCE_LIST_KEY).trim();

			log.debug("JNDI names for datasource {}", jndiNamesForDatasource);

			StringTokenizer tokenizer = new StringTokenizer(
					jndiNamesForDatasource, ";");
			while (tokenizer.hasMoreTokens()) {
				String jndiName = tokenizer.nextToken().trim();
				if (!jndiName.isEmpty()) {
					if (healthchecks == null) {
						healthchecks = new ArrayList<HealthCheck>();
					}

					HealthCheck check = getHealthCheck(context, jndiName);

					if (check != null) {
						healthchecks.add(check);
					}
				}

			}

		}

		return healthchecks;
	}

	private HealthCheck getHealthCheck(Context context, String jndi) {

		log.debug("[HealthCheck] create database check for jndi name {}", jndi);

		try {
			DataSource ds = (DataSource) context.lookup(jndi);

			if (ds.getClass().getName()
					.equals("com.ibm.ws.rsadapter.jdbc.WSJdbcDataSource")) {

			}

			return new DatabaseCheck("databaseCheck " + jndi, ds, null, null,
					-1);

		} catch (NamingException e) {
			log.warn("[HealthCheck] the JNDI name " + jndi + " cannot be found");
			return null;
		}

	}

	private Object invokeMethodWithoutParams(Object object, String methodName) {
		try {

			Method methodGetContextID = object.getClass().getDeclaredMethod(
					methodName);
			return methodGetContextID.invoke(object);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
