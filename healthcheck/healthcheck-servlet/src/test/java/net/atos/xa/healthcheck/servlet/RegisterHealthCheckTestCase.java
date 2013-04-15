package net.atos.xa.healthcheck.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;

import net.atos.xa.healthcheck.HealthCheckManager;

import org.junit.Test;

import com.yammer.metrics.core.HealthCheck;

public class RegisterHealthCheckTestCase {

	@Test
	public void testGetFilteredHealthChecksList() {

		Collection<HealthCheck> healthChecks = HealthCheckManager
				.getFilteredHealthChecksList(null);

		assertNotNull(healthChecks);
		assertEquals(3, healthChecks.size());

		healthChecks = HealthCheckManager
				.getFilteredHealthChecksList("testCheck3 ");

		assertNotNull(healthChecks);
		assertEquals(2, healthChecks.size());

		healthChecks = HealthCheckManager
				.getFilteredHealthChecksList("testCheck3   ;   testCheck2");

		assertNotNull(healthChecks);
		assertEquals(1, healthChecks.size());

	}
}
