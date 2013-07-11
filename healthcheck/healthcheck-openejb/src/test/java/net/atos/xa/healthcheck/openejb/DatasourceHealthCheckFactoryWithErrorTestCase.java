package net.atos.xa.healthcheck.openejb;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import net.atos.xa.healthcheck.HealthCheckManager;
import net.atos.xa.healthcheck.HealthCheckResult;
import net.atos.xa.healthcheck.openejb.util.OpenEjbUtil;

import org.junit.Before;
import org.junit.Test;

import com.yammer.metrics.core.HealthCheck;

public class DatasourceHealthCheckFactoryWithErrorTestCase {

	@Before
	public void beforeTest() throws Exception {
		OpenEjbUtil.startOpenEjb("missingDatasource");
	}

	@Test
	public void testGetHealthChecks() {

		DatasourceHealthCheckFactory factory = new DatasourceHealthCheckFactory();
		List<HealthCheck> healthChecks = factory.getHealthChecks(null);
		Assert.assertNotNull(healthChecks);

		HealthCheckManager.registerHealthChecks(healthChecks);
		Map<String, HealthCheckResult> results = HealthCheckManager
				.runHealthchecksWithDetailedReport();

		Assert.assertNotNull(results);

		for (String key : results.keySet()) {
			System.out.println("key " + key + " = " + results.get(key));
		}

	}

	public boolean contains(List<HealthCheck> healthChecks, String checkName) {

		boolean found = false;
		Iterator<HealthCheck> iterator = healthChecks.iterator();
		while (iterator.hasNext() && !found) {
			HealthCheck check = iterator.next();
			found = check.getName().equals(checkName);
		}

		return found;
	}
}
