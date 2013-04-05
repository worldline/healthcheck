package net.atos.xa.healthcheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.yammer.metrics.core.HealthCheck;

public class HealthCheckManagerTestCase {

	@Test
	public void testGetFilteredHealthChecksList() {

		Collection<HealthCheck> healthChecks = HealthCheckManager
				.getFilteredHealthChecks(null);

		assertNotNull(healthChecks);
		assertEquals(4, healthChecks.size());

		List<String> exclude = new ArrayList<String>();
		exclude.add("testCheck3 ");

		healthChecks = HealthCheckManager.getFilteredHealthChecks(exclude);

		assertNotNull(healthChecks);
		assertEquals(3, healthChecks.size());

		exclude = new ArrayList<String>();
		exclude.add("testCheck3 ");
		exclude.add("   testCheck2");

		healthChecks = HealthCheckManager.getFilteredHealthChecks(exclude);

		assertNotNull(healthChecks);
		assertEquals(2, healthChecks.size());

	}

}
