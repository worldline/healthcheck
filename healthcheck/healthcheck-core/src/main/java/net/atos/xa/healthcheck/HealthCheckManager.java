package net.atos.xa.healthcheck;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.atos.xa.healthcheck.spi.HealthCheckFactory;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheck.Result;

/**
 * 
 * <p>
 * API for managing health checks.
 * 
 * <h2>API Usage</h2>
 * 
 * You must first register your checks by one the registerHealthChecks methods
 * 
 * <pre>
 * HealthCheckManager.registerHealthChecks(yourChecks);
 * </pre>
 * 
 * Then, call one of the runHealthChecks methods to launch the checks
 * 
 * <pre>
 * HealthCheckManager.runHealthchecks();
 * </pre>
 * 
 * 
 * <h2>Develop and add your own checks</h2>
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
	private static volatile CustomHealthCheckRegistry managerInstance = null;

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
		return getAllHealthChecks(null);
	}

	/**
	 * get all the availbable health checks
	 * 
	 * @param environment
	 *            environment variable used for the setup of the checks <br>
	 *            This is an optional option, it depends on the checks you use.
	 *            Please consult the javadoc of the checks you use for details
	 *            about the possible variable environments
	 * 
	 * @return
	 */
	public static Collection<HealthCheck> getAllHealthChecks(
			Map<String, String> environment) {
		if (managerInstance == null) {
			createManager(environment);
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
		return getFilteredHealthChecks(excludeChecks, null);
	}

	/**
	 * Find all the available healthcheck except those mentionned in parameter
	 * 
	 * @param excludeChecks
	 *            a list of healthcheck names to exclude from the list
	 * @param environment
	 *            environment variable used for the setup of the checks <br>
	 *            This is an optional option, it depends on the checks you use.
	 *            Please consult the javadoc of the checks you use for details
	 *            about the possible variable environments
	 * @return
	 */
	public static Collection<HealthCheck> getFilteredHealthChecks(
			List<String> excludeChecks, Map<String, String> environment) {
		if (managerInstance == null) {
			createManager(environment);
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
		return getFilteredHealthChecks(null, excludeChecks);
	}

	/**
	 * Find all the available healthcheck except those mentionned in parameter
	 * 
	 * @param environment
	 *            environment variable used for the setup of the checks <br>
	 *            This is an optional option, it depends on the checks you use.
	 *            Please consult the javadoc of the checks you use for details
	 *            about the possible variable environments
	 * @param excludeChecks
	 *            an array of healthcheck names to exclude from the list
	 * 
	 * @return
	 */
	public static Collection<HealthCheck> getFilteredHealthChecks(
			Map<String, String> environment, String... excludeChecks) {
		if (managerInstance == null) {
			createManager(environment);
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

		return getFilteredHealthChecksList(excludeHealthChecks, null);

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
	 * 
	 * @param environment
	 *            environment variable used for the setup of the checks <br>
	 *            This is an optional option, it depends on the checks you use.
	 *            Please consult the javadoc of the checks you use for details
	 *            about the possible variable environments
	 * @return
	 */
	public static Collection<HealthCheck> getFilteredHealthChecksList(
			String excludeHealthChecks, Map<String, String> environment) {

		if (managerInstance == null) {
			createManager(environment);
		}
		return managerInstance.getFilteredHealthChecksList(excludeHealthChecks);

	}

	/**
	 * register all the available healthchecks into the {@link HealthChecks}
	 * class.
	 * 
	 */
	public static void registerAllHealthChecks() {
		registerAllHealthChecks(null);
	}

	/**
	 * register all the available healthchecks into the {@link HealthChecks}
	 * class.
	 * 
	 * @param environment
	 *            environment variable used for the setup of the checks <br>
	 *            This is an optional option, it depends on the checks you use.
	 *            Please consult the javadoc of the checks you use for details
	 *            about the possible variable environments
	 * 
	 */
	public static void registerAllHealthChecks(Map<String, String> environment) {
		registerHealthChecks(getAllHealthChecks(environment));
	}

	/**
	 * register all the available healthchecks into the {@link HealthChecks}
	 * except those mentionned in parameter.
	 * 
	 * @param excludeChecks
	 *            a list of healthcheck names to exclude for the registration
	 * 
	 */
	public static void registerFilteredHealthChecks(List<String> excludeChecks) {
		registerFilteredHealthChecks(excludeChecks, null);
	}

	/**
	 * register all the available healthchecks into the {@link HealthChecks}
	 * except those mentionned in parameter.
	 * 
	 * @param excludeChecks
	 *            a list of healthcheck names to exclude for the registration
	 * @param environment
	 *            environment variable used for the setup of the checks <br>
	 *            This is an optional option, it depends on the checks you use.
	 *            Please consult the javadoc of the checks you use for details
	 *            about the possible variable environments
	 */
	public static void registerFilteredHealthChecks(List<String> excludeChecks,
			Map<String, String> environment) {
		registerHealthChecks(getFilteredHealthChecks(excludeChecks, environment));
	}

	/**
	 * register the healthchecks into the {@link HealthChecks} class.
	 * 
	 * @param healthChecks
	 */
	public static void registerHealthChecks(Collection<HealthCheck> healthChecks) {
		registerHealthChecks(healthChecks, null);

	}

	/**
	 * register the healthchecks into the {@link HealthChecks} class.
	 * 
	 * @param healthChecks
	 * @param environment
	 *            environment variable used for the setup of the checks <br>
	 *            This is an optional option, it depends on the checks you use.
	 *            Please consult the javadoc of the checks you use for details
	 *            about the possible variable environments
	 */
	public static void registerHealthChecks(
			Collection<HealthCheck> healthChecks,
			Map<String, String> environment) {
		if (managerInstance == null) {
			createManager(environment);
		}
		managerInstance.registerHealthChecks(healthChecks);

	}

	/**
	 * register the healthchecks into the {@link HealthChecks} class.
	 * 
	 * @param healthChecks
	 */
	public static void registerHealthChecks(HealthCheck... healthChecks) {
		registerHealthChecks(null, healthChecks);

	}

	/**
	 * register the healthchecks into the {@link HealthChecks} class.
	 * 
	 * @param environment
	 *            environment variable used for the setup of the checks <br>
	 *            This is an optional option, it depends on the checks you use.
	 *            Please consult the javadoc of the checks you use for details
	 *            about the possible variable environments
	 * @param healthChecks
	 */
	public static void registerHealthChecks(Map<String, String> environment,
			HealthCheck... healthChecks) {
		if (managerInstance == null) {
			createManager(environment);
		}
		managerInstance.registerHealthChecks(healthChecks);

	}

	/**
	 * Run all the registered healthchecks<br>
	 * You must register your checks before running the checks
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
	public static Map<String, Result> runHealthchecksWithDetailedReport() {
		if (managerInstance == null) {
			createManager();
		}
		return managerInstance.runHealthchecksWithDetailedReport();
	}

	/**
	 * Deactivate a previously registered check
	 * 
	 * @param checkName
	 *            the name of the check; if null, the method does nothing.
	 */
	public static void deactivateCheck(String checkName) {
		if (managerInstance == null) {
			createManager();
		}
		managerInstance.deactivateCheck(checkName);
	}

	/**
	 * Deactivate a set of previously registered checks
	 * 
	 * @param checkNames
	 *            the name of the checks; if null, the method does nothing.
	 */
	public static void deactivateChecks(String... checkNames) {
		if (managerInstance == null) {
			createManager();
		}
		managerInstance.deactivateChecks(checkNames);
	}

	/**
	 * Deactivate all the registered checks
	 * 
	 */
	public static void deactivateAllChecks() {
		if (managerInstance == null) {
			createManager();
		}
		managerInstance.deactivateAllChecks();
	}

	/**
	 * Activate a previously unregistered check
	 * 
	 * @param checkName
	 *            the name of the check; if null, the method does nothing.
	 */
	public static void activateCheck(String checkName) {
		if (managerInstance == null) {
			createManager();
		}
		managerInstance.activateCheck(checkName);
	}

	/**
	 * Activate a set of previously unregistered checks
	 * 
	 * @param checkNames
	 *            the name of the checks; if null, the method does nothing.
	 */
	public static void activateChecks(String... checkNames) {
		if (managerInstance == null) {
			createManager();
		}
		managerInstance.activateChecks(checkNames);
	}

	/**
	 * Create our HealthCheckLocatorUnit instance
	 * 
	 */
	private static CustomHealthCheckRegistry createManager() {
		return createManager(null);
	}

	/**
	 * Create our HealthCheckLocatorUnit instance
	 * 
	 */
	private static CustomHealthCheckRegistry createManager(
			Map<String, String> environment) {
		// double check locking to be thread safe
		try {
			initializationLock.lock();
			if (managerInstance == null) {
				managerInstance = new CustomHealthCheckRegistry(environment);
			}
		} finally {
			initializationLock.unlock();
		}
		return managerInstance;
	}

}
