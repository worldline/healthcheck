package net.atos.xa.healthcheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.yammer.metrics.core.HealthCheck;

public class HealthCheckManagerTestCase {

	@Test
	public void testGetFilteredHealthChecksList() {

		Collection<HealthCheck> healthChecks = HealthCheckManager
				.getFilteredHealthChecks((List) null);

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

		healthChecks = HealthCheckManager
				.getFilteredHealthChecksList("testCheck3 ; testCheck2");

		assertNotNull(healthChecks);
		assertEquals(2, healthChecks.size());

		healthChecks = HealthCheckManager.getFilteredHealthChecks(
				"testCheck3 ", " testCheck2");

		assertNotNull(healthChecks);
		assertEquals(2, healthChecks.size());

	}

	@Test
	public void testGetAllHealthChecks() {

		Collection<HealthCheck> healthChecks = HealthCheckManager
				.getAllHealthChecks();
		assertNotNull(healthChecks);
		assertEquals(4, healthChecks.size());

	}

	@Test
	public void testDeactivateCheck() {

		HealthCheckManager.deactivateAllChecks();

		HealthCheckManager.registerAllHealthChecks();
		Map<String, HealthCheckResult> results = testRunWithNumberOfExpectedResults(4);

		// nothing should happen
		HealthCheckManager.deactivateCheck(null);
		results = testRunWithNumberOfExpectedResults(4);

		// nothing should happen
		HealthCheckManager.deactivateCheck("unknowncheck");
		results = testRunWithNumberOfExpectedResults(4);

		HealthCheckManager.deactivateCheck("testCheck1 ");

		results = testRunWithNumberOfExpectedResults(3);
		assertNull(results.get("testCheck1"));

	}

	@Test
	public void testDeactivateChecks() {

		HealthCheckManager.deactivateAllChecks();

		HealthCheckManager.registerAllHealthChecks();
		Map<String, HealthCheckResult> results = testRunWithNumberOfExpectedResults(4);

		// nothing should happen
		HealthCheckManager.deactivateChecks((String[]) null);
		results = testRunWithNumberOfExpectedResults(4);

		// nothing should happen
		HealthCheckManager.deactivateChecks("unknowncheck");
		results = testRunWithNumberOfExpectedResults(4);

		HealthCheckManager.deactivateChecks("testCheck1 ", "testCheck2");

		results = testRunWithNumberOfExpectedResults(2);
		assertNull(results.get("testCheck1"));
		assertNull(results.get("testCheck2"));

	}

	@Test
	public void testActivateCheck() {

		HealthCheckManager.deactivateAllChecks();

		Collection<HealthCheck> healthChecks = HealthCheckManager
				.getFilteredHealthChecks("testCheck3 ", " testCheck2");
		HealthCheckManager.registerHealthChecks(healthChecks);
		Map<String, HealthCheckResult> results = testRunWithNumberOfExpectedResults(2);

		// nothing should happen
		HealthCheckManager.activateCheck(null);
		results = testRunWithNumberOfExpectedResults(2);

		// nothing should happen
		HealthCheckManager.activateCheck("unknowncheck");
		results = testRunWithNumberOfExpectedResults(2);

		HealthCheckManager.activateCheck("testCheck2 ");
		results = testRunWithNumberOfExpectedResults(3);
		assertNotNull(results.get("testCheck2"));

	}

	@Test
	public void testActivateChecks() {

		HealthCheckManager.deactivateAllChecks();

		Collection<HealthCheck> healthChecks = HealthCheckManager
				.getFilteredHealthChecks("testCheck3 ", " testCheck2");
		HealthCheckManager.registerHealthChecks(healthChecks);
		Map<String, HealthCheckResult> results = testRunWithNumberOfExpectedResults(2);

		// nothing should happen
		HealthCheckManager.activateChecks((String[]) null);
		results = testRunWithNumberOfExpectedResults(2);

		// nothing should happen
		HealthCheckManager.activateChecks("unknowncheck");
		results = testRunWithNumberOfExpectedResults(2);

		HealthCheckManager.activateChecks("testCheck2 ", "testCheck3 ");
		results = testRunWithNumberOfExpectedResults(4);
		assertNotNull(results.get("testCheck2"));
		assertNotNull(results.get("testCheck3"));

	}

	private Map<String, HealthCheckResult> testRunWithNumberOfExpectedResults(
			int numberOfExpectedResults) {

		Map<String, HealthCheckResult> results = HealthCheckManager
				.runHealthchecksWithDetailedReport();

		assertNotNull(results);
		assertEquals(numberOfExpectedResults, results.size());

		return results;
	}
}
