package net.atos.xa.healthcheck;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import net.atos.xa.healthcheck.spi.HealthCheckFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.HealthCheck;

public class HealthCheckLocatorUnit {

	/** the logger */
	private static Logger log = LoggerFactory
			.getLogger(HealthCheckLocatorUnit.class.getName());

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

		ClassLoader classLoader = this.getClass().getClassLoader();

		if (log.isDebugEnabled()) {
			log.debug(
					"[HealthCheck] init healthcheck locator (classloader : {} )",
					classLoader.getClass());

			try {
				Enumeration<URL> urls = classLoader
						.getResources("META-INF/services/"
								+ HealthCheck.class.getName());
				if (urls != null) {
					while (urls.hasMoreElements()) {
						URL url = urls.nextElement();
						log.debug(
								"[HealthCheck] load SPI file {} for healthcheck",
								url.getPath());
					}
				}

				urls = classLoader.getResources("META-INF/services/"
						+ HealthCheckFactory.class.getName());
				if (urls != null) {
					while (urls.hasMoreElements()) {
						URL url = urls.nextElement();
						log.debug(
								"[HealthCheck] load SPI file {} for healthcheck",
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

		ServiceLoader<HealthCheckFactory> serviceLoaderDynamic = ServiceLoader
				.load(HealthCheckFactory.class, classLoader);
		Iterator<HealthCheckFactory> iteratorDynamic = serviceLoaderDynamic
				.iterator();
		while (iteratorDynamic.hasNext()) {
			HealthCheckFactory dynamicCheck = iteratorDynamic.next();
			if (dynamicCheck.getHealthChecks() != null) {
				for (HealthCheck check : dynamicCheck.getHealthChecks()) {
					log.debug(
							"[HealthCheck] found a check {} (classname : {}) given by the healthcheck factory {}",
							check.getName(), check.getClass(),
							dynamicCheck.getClass());
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
