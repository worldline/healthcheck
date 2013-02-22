package net.atos.xa.healthcheck.openejb;

import java.util.ArrayList;
import java.util.List;

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

public class DatasourceDynamicHealthCheck implements DynamicHealthCheck {

	private static final String ROOT = "";

	public List<HealthCheck> getHealthChecks() {

		return getHealthChecks(
				SystemInstance.get().getComponent(ContainerSystem.class)
						.getJNDIContext(), null);
	}

	private List<HealthCheck> getHealthChecks(Context context, String prefix) {

		List<HealthCheck> result = null;

		try {
			NamingEnumeration<Binding> ne = context.listBindings(ROOT);

			while (ne.hasMoreElements()) {
				Binding current = ne.next();
				Object obj = current.getObject();
				// System.out.println(obj.getClass());
				if (obj instanceof Context) {
					List<HealthCheck> healthChecks = getHealthChecks(
							(Context) obj, prefix + '/' + current.getName());

					if (healthChecks != null) {
						if (result == null)
							result = new ArrayList<HealthCheck>();
						result.addAll(healthChecks);
					}

				} else if (obj instanceof BasicDataSource) {
					BasicDataSource basicDS = (BasicDataSource) obj;
					if (result == null)
						result = new ArrayList<HealthCheck>();
					// no support of validation query timeout with openEJB 3.1
					result.add(new DatabaseCheck(current.getName(), basicDS,
							basicDS.getValidationQuery(), 0));
				}
			}

		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;

	}
}
