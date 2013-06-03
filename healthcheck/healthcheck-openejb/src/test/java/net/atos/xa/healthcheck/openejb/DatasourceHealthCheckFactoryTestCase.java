package net.atos.xa.healthcheck.openejb;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import net.atos.xa.healthcheck.openejb.util.OpenEjbUtil;

import org.junit.Before;
import org.junit.Test;

import com.yammer.metrics.core.HealthCheck;

public class DatasourceHealthCheckFactoryTestCase {

	@Before
	public void beforeTest() throws Exception {
		OpenEjbUtil.startOpenEjb("testFactory");
	}

	@Test
	public void testGetHealthChecks() {

		DatasourceHealthCheckFactory factory = new DatasourceHealthCheckFactory();
		List<HealthCheck> healthChecks = factory.getHealthChecks(null);
		Assert.assertNotNull(healthChecks);
		Assert.assertEquals(3, healthChecks.size());

		Assert.assertTrue(contains(healthChecks, "databaseCheck DefaultDS"));
		Assert.assertTrue(contains(healthChecks,
				"databaseCheck xastory-cc-commonobjectsDS"));
		Assert.assertTrue(contains(healthChecks,
				"databaseCheck xastory-csm-coreDS"));
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
