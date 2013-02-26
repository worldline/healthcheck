package net.atos.xa.healthcheck.openejb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import net.atos.xa.healthcheck.checks.DatabaseCheck;
import net.atos.xa.healthcheck.spi.DynamicHealthCheck;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import com.yammer.metrics.core.HealthCheck;

/**
 * Implementation of {@link DynamicHealthCheck} for checking OpenEJB /TomEE
 * datasources This implementation browses the JNDI tree by looking for
 * Datasource entries. For each datasource entry, a {@link DatabaseCheck} is
 * created
 * 
 */
public class DatasourceDynamicHealthCheck implements DynamicHealthCheck {

	/** the logger */
	private static Logger log = Logger.getLogger(DatabaseCheck.class.getName());

	private static final String ROOT = "";

	/**
	 * 
	 */
	public List<HealthCheck> getHealthChecks() {

		return getHealthChecks(
				SystemInstance.get().getComponent(ContainerSystem.class)
						.getJNDIContext(), null);
	}

	private List<HealthCheck> getHealthChecks(Context context, String prefix) {

		List<HealthCheck> result = null;

		Set<DatasourceWrapper> dataSourceToAdd = new HashSet<DatasourceWrapper>();

		try {
			NamingEnumeration<Binding> ne = context.listBindings(ROOT);

			while (ne.hasMoreElements()) {
				Binding current = ne.next();
				Object obj = current.getObject();
				if (obj instanceof Context) {
					List<HealthCheck> healthChecks = getHealthChecks(
							(Context) obj, prefix + '/' + current.getName());

					if (healthChecks != null) {
						if (result == null)
							result = new ArrayList<HealthCheck>();
						result.addAll(healthChecks);
					}

				} else if (obj instanceof BasicDataSource) {

					dataSourceToAdd.add(new DatasourceWrapper(
							current.getName(), (BasicDataSource) obj));

				}

			}

		} catch (NamingException e) {
			log.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}

		if (!dataSourceToAdd.isEmpty()) {

			if (result == null)
				result = new ArrayList<HealthCheck>();
			for (DatasourceWrapper datasourceWrapper : dataSourceToAdd) {
				// no support of validation query timeout with openEJB 3.1

				if (log.isLoggable(Level.FINE)) {
					log.fine("Add datasource checker for :"
							+ datasourceWrapper.getName());
				}

				result.add(new DatabaseCheck(datasourceWrapper.getName(),
						datasourceWrapper.getDatasource(), datasourceWrapper
								.getDatasource().getValidationQuery(), 0));
			}
		}

		return result;

	}

	/*
	 * By default OpenEJB creates two datasources (Jta and non Jta) for each
	 * datasource referenced in the configuration.
	 * 
	 * @see org.apache.openejb.config.AutoConfig Use a wrapper to keep only one
	 * datasource
	 */
	private class DatasourceWrapper {

		private BasicDataSource datasource;
		private String name;

		DatasourceWrapper(String name, BasicDataSource datasource) {
			this.datasource = datasource;
			this.name = name;
		}

		public boolean equals(Object obj) {

			if (obj == null || !(obj instanceof DatasourceWrapper))
				return false;

			String nameDs = ((DatasourceWrapper) obj).getName();

			return name.equals(nameDs + "Jta")
					|| name.equals(nameDs + "NonJta")
					|| nameDs.equals(name + "NonJta")
					|| nameDs.equals(name + "Jta");

		}

		public int hashCode() {
			return 1; // only the equals method must determine the equality
		}

		public String getName() {
			return name;
		}

		public BasicDataSource getDatasource() {
			return datasource;
		}

	}
}
