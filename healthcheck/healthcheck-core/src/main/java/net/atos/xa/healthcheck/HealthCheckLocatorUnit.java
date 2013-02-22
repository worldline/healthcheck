package net.atos.xa.healthcheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import net.atos.xa.healthcheck.spi.DynamicHealthCheck;

import com.yammer.metrics.core.HealthCheck;

public class HealthCheckLocatorUnit {

	private Map<String, HealthCheck> healthChecks = new HashMap<String, HealthCheck>();

	/**
	 * Constructor
	 * 
	 */
	public HealthCheckLocatorUnit() {
		init();
	}

	/**
	 * Initialization method
	 * 
	 * @param initialData
	 */
	protected void init() {
		ServiceLoader<HealthCheck> serviceLoader = ServiceLoader
				.load(HealthCheck.class);
		Iterator<HealthCheck> iterator = serviceLoader.iterator();
		while (iterator.hasNext()) {
			HealthCheck check = iterator.next();
			healthChecks.put(check.getName(), check);
		}

		ServiceLoader<DynamicHealthCheck> serviceLoaderDynamic = ServiceLoader
				.load(DynamicHealthCheck.class);
		Iterator<DynamicHealthCheck> iteratorDynamic = serviceLoaderDynamic
				.iterator();
		while (iteratorDynamic.hasNext()) {
			DynamicHealthCheck dynamicCheck = iteratorDynamic.next();
			if (dynamicCheck.getHealthChecks() != null) {
				for (HealthCheck check : dynamicCheck.getHealthChecks()) {
					healthChecks.put(check.getName(), check);
				}
			}

		}
	}

	public Collection<HealthCheck> getAllHealthChecks() {
		return healthChecks.values();
	}

	public Collection<HealthCheck> getFilteredHealthChecks(
			List<String> excludeChecks) {

		if (excludeChecks == null)
			return healthChecks.values();

		List<String> trimmedExcludeChecks = new ArrayList<String>();
		for (String string : excludeChecks) {
			trimmedExcludeChecks.add(trimToEmpty(string));
		}

		Iterator<HealthCheck> iterator = healthChecks.values().iterator();
		List<HealthCheck> result = null;

		while (iterator.hasNext()) {
			HealthCheck healthcheck = iterator.next();

			if (!trimmedExcludeChecks.contains(trimToEmpty(healthcheck
					.getName()))) {
				if (result == null)
					result = new ArrayList<HealthCheck>();

				result.add(healthcheck);
			}

		}

		return result;
	}

	private String trimToEmpty(String str) {
		return str == null ? "" : str.trim();
	}

}
