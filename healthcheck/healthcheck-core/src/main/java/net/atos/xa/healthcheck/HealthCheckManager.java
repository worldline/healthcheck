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
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.atos.xa.healthcheck.spi.HealthCheckFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheck.Result;

/**
 * 
 * <p>
 * API for managing health checks
 * 
 * HealthCheck manager enhances the <a href="http://metrics.codahale.com/"
 * target="_blank">Metrics framework</a> by providing a SPI mechnanism for
 * registering health checks.
 * 
 * The SPI mechanism is a standard java mechanism for loading service. See
 * {@link ServiceLoader} for details
 * 
 * 
 * With HealthCheck manager, we provide two SPI to make available the checks
 * you've developped
 * 
 * <h2>How to register static check</h2>
 * 
 * A first SPI is used for static check, you must provide a file named
 * 
 * <pre>
 * com.yammer.metrics.core.HealthCheck
 * </pre>
 * 
 * in the
 * 
 * <pre>
 * META-INF/services/
 * </pre>
 * 
 * folder. This file must list all the subclasses of
 * com.yammer.metrics.core.HealthCheck you've developped.
 * 
 * <h2>How to register dynamic check</h2>
 * You may not know in advance how many checks you need to execute. For
 * instance, in a application server, you cannot know in advance how many
 * datasources will be configured for a project.
 * 
 * For that purpose, we provide a SPI for creating checks dynamically. You must
 * provide a file named
 * 
 * <pre>
 * net.atos.xa.healthcheck.spi.HealthCheckFactory
 * </pre>
 * 
 * in the
 * 
 * <pre>
 * META-INF/services/
 * </pre>
 * 
 * folder. This file must list all the implementation of the
 * {@link HealthCheckFactory} interface. This interface exposes one method which
 * allows to create how many HealthCheck as necessary.
 * 
 * 
 * </p>
 * 
 * 
 */
public class HealthCheckManager {
	// we delegate calls to HealthCheckManagerUnit class
	private static volatile HealthCheckManagerUnit managerInstance = null;

	// lock to synchronize context initialization
	private static Lock initializationLock = new ReentrantLock();

	private HealthCheckManager() {

	}

	/**
	 * get all the availbable health checks
	 * 
	 * @return
	 */
	public static Collection<HealthCheck> getAllHealthChecks() {
		if (managerInstance == null) {
			createManager();
		}
		return managerInstance.getAllHealthChecks();
	}

	/**
	 * Find all the available healthcheck except those mentionned in parameter
	 * 
	 * @param excludeChecks
	 *            a list of healthcheck names to exclude from the list
	 * @return
	 */
	public static Collection<HealthCheck> getFilteredHealthChecks(
			List<String> excludeChecks) {
		if (managerInstance == null) {
			createManager();
		}
		return managerInstance.getFilteredHealthChecks(excludeChecks);
	}

	/**
	 * Find all the available healthcheck except those mentionned in parameter
	 * 
	 * @param excludeChecks
	 *            an array of healthcheck names to exclude from the list
	 * @return
	 */
	public static Collection<HealthCheck> getFilteredHealthChecks(
			String... excludeChecks) {
		if (managerInstance == null) {
			createManager();
		}
		return managerInstance.getFilteredHealthChecks(excludeChecks);
	}

	/**
	 * get a list of healthcheck <br/>
	 * 
	 * Example, if we have the following healthchecks with the names: "check1",
	 * "check2" and "check3".<br/>
	 * A call to getFilteredHealthChecksList("check2;check3") will return only
	 * the "check1" healthcheck<br/>
	 * <br/>
	 * 
	 * Implementations of healthcheck are retrieved thanks to SPI mechanism. @see
	 * {@link java.util.ServiceLoader}
	 * 
	 * @param excludeHealthChecks
	 *            list of excluded healthchecks (list of names separated by a
	 *            semicolon
	 * @return
	 */
	public static Collection<HealthCheck> getFilteredHealthChecksList(
			String excludeHealthChecks) {

		if (managerInstance == null) {
			createManager();
		}
		return managerInstance.getFilteredHealthChecksList(excludeHealthChecks);

	}

	/**
	 * register all the available healthchecks into the {@link HealthChecks}
	 * class.
	 * 
	 */
	public static void registerAllHealthChecks() {
		registerHealthChecks(getAllHealthChecks());
	}

	/**
	 * register all the available healthchecks into the {@link HealthChecks}
	 * except those mentionned in parameter class.
	 * 
	 * @param excludeChecks
	 *            a list of healthcheck names to exclude for the registration
	 * 
	 */
	public static void registerFilteredHealthChecks(List<String> excludeChecks) {
		registerHealthChecks(getFilteredHealthChecks(excludeChecks));
	}

	/**
	 * register the healthchecks into the {@link HealthChecks} class.
	 * 
	 * @param healthChecks
	 */
	public static void registerHealthChecks(Collection<HealthCheck> healthChecks) {
		if (managerInstance == null) {
			createManager();
		}
		managerInstance.registerHealthChecks(healthChecks);

	}

	/**
	 * register the healthchecks into the {@link HealthChecks} class.
	 * 
	 * @param healthChecks
	 */
	public static void registerHealthChecks(HealthCheck... healthChecks) {
		if (managerInstance == null) {
			createManager();
		}
		managerInstance.registerHealthChecks(healthChecks);

	}

	/**
	 * Run all the registered healthchecks
	 * 
	 * @return true if all the results are healthy
	 */
	public static boolean runHealthchecks() {
		if (managerInstance == null) {
			createManager();
		}
		return managerInstance.runHealthchecks();
	}

	/**
	 * Run all the registered healthchecks and produce a detailed report
	 * 
	 * @return a map with one entry per executed check. For each check, it
	 *         indicates if it is healthy or not
	 */
	public static Map<String, Result> runHealthchecksWithDetailReport() {
		if (managerInstance == null) {
			createManager();
		}
		return managerInstance.runHealthchecksWithDetailReport();
	}

	/**
	 * Create our HealthCheckLocatorUnit instance
	 * 
	 */
	private static HealthCheckManagerUnit createManager() {
		// double check locking to be thread safe
		try {
			initializationLock.lock();
			if (managerInstance == null) {
				managerInstance = new HealthCheckManagerUnit();
			}
		} finally {
			initializationLock.unlock();
		}
		return managerInstance;
	}

}

class HealthCheckManagerUnit {

	/** the logger */
	private static Logger log = LoggerFactory
			.getLogger(HealthCheckManagerUnit.class.getName());

	private Map<String, HealthCheck> healthChecks = new HashMap<String, HealthCheck>();

	/**
	 * Constructor
	 * 
	 */
	public HealthCheckManagerUnit() {
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
			if (factory.getHealthChecks() != null) {
				for (HealthCheck check : factory.getHealthChecks()) {
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
				HealthChecks.register(healthCheck);
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
					HealthChecks.register(healthChecks[i]);
				}
			}
		}
	}

	boolean runHealthchecks() {
		return isAllHealthy(runHealthchecksWithDetailReport());
	}

	Map<String, Result> runHealthchecksWithDetailReport() {
		return HealthChecks.runHealthChecks();
	}

	private String trimToEmpty(String str) {
		return str == null ? "" : str.trim();
	}

	private boolean isAllHealthy(Map<String, HealthCheck.Result> results) {
		for (HealthCheck.Result result : results.values()) {
			if (!result.isHealthy()) {
				return false;
			}
		}
		return true;
	}

}
