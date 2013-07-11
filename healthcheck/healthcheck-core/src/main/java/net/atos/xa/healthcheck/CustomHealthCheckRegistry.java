package net.atos.xa.healthcheck;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.atos.xa.healthcheck.spi.HealthCheckFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheck.Result;

/**
 * 
 * A custom registry that replaces the one provided by metrics
 * 
 * 
 * 
 */
class CustomHealthCheckRegistry {

	/** the logger */
	private static Logger log = LoggerFactory
			.getLogger(CustomHealthCheckRegistry.class.getName());

	private final Map<String, HealthCheck> healthChecks = new HashMap<String, HealthCheck>();

	private final ConcurrentMap<String, HealthCheck> registerHealthChecks = new ConcurrentHashMap<String, HealthCheck>();

	/**
	 * Constructor
	 * 
	 */
	public CustomHealthCheckRegistry() {
		init(null);
	}

	/**
	 * Constructor
	 * 
	 */
	public CustomHealthCheckRegistry(Map<String, String> environment) {
		init(environment);
	}

	/**
	 * Initialization method
	 * 
	 * @param environment
	 *            environment variable used for the setup of the checks <br>
	 *            This is an optional option, it depends on the checks you use.
	 *            Please consult the javadoc of the checks you use for details
	 *            about the possible variable environments
	 */
	protected void init(Map<String, String> environment) {

		ClassLoader classLoader = this.getClass().getClassLoader();

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

			log.debug(
					"[HealthCheck] init healthcheck locator (classloader : {} ) with environment",
					classLoader.getClass());
			log.debug(buffer.toString());

			try {
				Enumeration<URL> urls = classLoader
						.getResources("META-INF/services/"
								+ HealthCheck.class.getName());
				if (urls != null) {
					while (urls.hasMoreElements()) {
						URL url = urls.nextElement();
						log.debug(
								"[HealthCheck] load SPI file {} for static healthcheck",
								url.getPath());
					}
				}

				urls = classLoader.getResources("META-INF/services/"
						+ HealthCheckFactory.class.getName());
				if (urls != null) {
					while (urls.hasMoreElements()) {
						URL url = urls.nextElement();
						log.debug(
								"[HealthCheck] load SPI file {} for dynamic healthcheck",
								url.getPath());
					}
				}
			} catch (IOException e) {
				// do nothing
				log.debug("[HealthCheck] error when getting resources", e);
			}
		}

		ServiceLoader<HealthCheck> serviceLoader = ServiceLoader.load(
				HealthCheck.class, classLoader);
		Iterator<HealthCheck> iterator = serviceLoader.iterator();
		while (iterator.hasNext()) {
			HealthCheck check = iterator.next();
			log.debug("[HealthCheck] found a check {} (classname : {})",
					check.getName(), check.getClass());
			healthChecks.put(check.getName(), check);
		}

		ServiceLoader<HealthCheckFactory> serviceLoaderFactory = ServiceLoader
				.load(HealthCheckFactory.class, classLoader);
		Iterator<HealthCheckFactory> iteratorFactory = serviceLoaderFactory
				.iterator();
		while (iteratorFactory.hasNext()) {
			HealthCheckFactory factory = iteratorFactory.next();
			List<HealthCheck> healthchecks = factory
					.getHealthChecks(environment);
			if (healthchecks != null) {
				for (HealthCheck check : healthchecks) {
					log.debug(
							"[HealthCheck] found a check {} (classname : {}) given by the healthcheck factory {}",
							check.getName(), check.getClass(),
							factory.getClass());
					healthChecks.put(check.getName(), check);
				}
			}

		}
	}

	Collection<HealthCheck> getAllHealthChecks() {
		return healthChecks.values();
	}

	Collection<HealthCheck> getFilteredHealthChecks(List<String> excludeChecks) {

		if (excludeChecks == null) {
			return healthChecks.values();
		}

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
				if (result == null) {
					result = new ArrayList<HealthCheck>();
				}

				result.add(healthcheck);
			}

		}

		return result;
	}

	Collection<HealthCheck> getFilteredHealthChecks(String... excludeChecks) {

		if (excludeChecks == null) {
			return healthChecks.values();
		}

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
				if (result == null) {
					result = new ArrayList<HealthCheck>();
				}

				result.add(healthcheck);
			}

		}

		return result;
	}

	Collection<HealthCheck> getFilteredHealthChecksList(
			String excludeHealthChecks) {

		List<String> excludeChecks = new ArrayList<String>();
		if (excludeHealthChecks != null) {

			StringTokenizer tokenizer = new StringTokenizer(
					excludeHealthChecks, ";");
			while (tokenizer.hasMoreTokens()) {
				excludeChecks.add(trimToEmpty(tokenizer.nextToken()));
			}
		}

		return getFilteredHealthChecks(excludeChecks);

	}

	/**
	 * register the healthchecks into the {@link HealthChecks} class.
	 * 
	 * @param healthChecks
	 */
	void registerHealthChecks(Collection<HealthCheck> healthChecks) {
		if (healthChecks != null) {
			for (HealthCheck healthCheck : healthChecks) {
				log.debug("[HealthCheck] register a check {} (classname : {})",
						healthCheck.getName(), healthCheck.getClass());
				register(healthCheck);
			}
		}

	}

	void registerHealthChecks(HealthCheck... healthChecks) {

		if (healthChecks != null) {
			for (int i = 0; i < healthChecks.length; i++) {
				if (healthChecks[i] != null) {
					log.debug(
							"[HealthCheck] register a check {} (classname : {})",
							healthChecks[i].getName(),
							healthChecks[i].getClass());
					register(healthChecks[i]);
				}
			}
		}
	}

	boolean runHealthchecks() {
		return isAllHealthy(runHealthchecksWithDetailedReport());
	}

	/**
	 * Runs the registered health checks and returns a map of the results.
	 * 
	 * @return a map of the health check results
	 */
	SortedMap<String, HealthCheckResult> runHealthchecksWithDetailedReport() {
		final SortedMap<String, HealthCheckResult> results = new TreeMap<String, HealthCheckResult>();
		for (Entry<String, HealthCheck> entry : registerHealthChecks.entrySet()) {
			long startTime = System.currentTimeMillis();
			final Result result = entry.getValue().execute();
			results.put(entry.getKey(),
					new HealthCheckResult(result, System.currentTimeMillis()
							- startTime));
		}
		return Collections.unmodifiableSortedMap(results);
	}

	void register(HealthCheck healthCheck) {
		registerHealthChecks.putIfAbsent(healthCheck.getName(), healthCheck);
	}

	void activateCheck(String name) {
		if (name == null) {
			return;
		}
		if (healthChecks.get(name.trim()) == null) {
			log.warn("[HealthCheck] Not able to activate an unknown check {}",
					name.trim());
		} else {
			register(healthChecks.get(name.trim()));
		}

	}

	void activateChecks(String... names) {

		if (names != null && names.length > 0) {
			for (int i = 0; i < names.length; i++) {
				activateCheck(names[i]);
			}
		}
	}

	void deactivateCheck(String name) {
		if (name == null) {
			return;
		}
		if (registerHealthChecks.get(name.trim()) == null) {
			log.warn(
					"[HealthCheck] Not able to deactivate an unknown check {}",
					name.trim());
		} else {
			registerHealthChecks.remove(name.trim());
		}

	}

	void deactivateCheck(HealthCheck healthCheck) {
		deactivateCheck(healthCheck.getName());
	}

	void deactivateChecks(String... names) {
		if (names != null && names.length > 0) {
			for (int i = 0; i < names.length; i++) {
				deactivateCheck(names[i]);
			}
		}
	}

	void deactivateAllChecks() {
		registerHealthChecks.clear();
	}

	private String trimToEmpty(String str) {
		return str == null ? "" : str.trim();
	}

	private boolean isAllHealthy(Map<String, HealthCheckResult> results) {
		for (HealthCheckResult result : results.values()) {
			if (!result.isHealthy()) {
				return false;
			}
		}
		return true;
	}

}
