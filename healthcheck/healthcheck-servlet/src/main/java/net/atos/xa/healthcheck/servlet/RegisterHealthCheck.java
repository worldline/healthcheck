package net.atos.xa.healthcheck.servlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import net.atos.xa.healthcheck.HealthCheckManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;

/**
 * An helper class for registering the healthcheck into the {@link HealthChecks}
 * class.
 * 
 * @see {@link http://metrics.codahale.com/manual/core/#health-checks} for
 *      detail about health check
 * 
 *      This class is based on the <a href="http ://metrics.codahale.com">Yammer
 *      metrics framework</a>.
 * 
 * @author jmacke
 * 
 */
public class RegisterHealthCheck {

	/** the logger */
	private static Logger log = LoggerFactory
			.getLogger(RegisterHealthCheck.class.getName());

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

		List<String> excludeChecks = new ArrayList<String>();
		if (excludeHealthChecks != null) {

			StringTokenizer tokenizer = new StringTokenizer(
					excludeHealthChecks, ";");
			while (tokenizer.hasMoreTokens()) {
				excludeChecks.add(trimToEmpty(tokenizer.nextToken()));
			}
		}

		return HealthCheckManager.getFilteredHealthChecks(excludeChecks);

	}

	/**
	 * register the healthchecks into the {@link HealthChecks} class.
	 * 
	 * @param healthChecks
	 */
	public static void registerHealthChecks(Collection<HealthCheck> healthChecks) {
		if (healthChecks != null) {
			for (HealthCheck healthCheck : healthChecks) {
				log.debug("[HealthCheck] register a check {} (classname : {})",
						healthCheck.getName(), healthCheck.getClass());
				HealthChecks.register(healthCheck);
			}
		}

	}

	private static String trimToEmpty(String str) {
		return str == null ? "" : str.trim();
	}
}
