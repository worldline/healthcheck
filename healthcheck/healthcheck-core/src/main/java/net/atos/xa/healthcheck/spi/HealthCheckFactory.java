package net.atos.xa.healthcheck.spi;

import java.util.List;
import java.util.Map;

import com.yammer.metrics.core.HealthCheck;

/**
 * This is a factory of health checks
 * 
 * Implement this interface if you do not know in advance how many health checks
 * you will have<br/>
 * This interface is suitable when you deal with dynamic configuration<br/>
 * Example of checks about datasource in application server
 * 
 * 
 */
public interface HealthCheckFactory {

	/**
	 * @param environment
	 *            environment variable used for the setup of the checks <br>
	 *            This is an optional option, it depends on the checks you use.
	 *            Please consult the javadoc of the checks you use for details
	 *            about the possible variable environments
	 * 
	 * @return a list of healthcheck to be checked, null in case of error
	 */
	List<HealthCheck> getHealthChecks(Map<String, String> environment);
}
