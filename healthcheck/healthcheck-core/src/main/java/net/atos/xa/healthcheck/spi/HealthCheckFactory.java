package net.atos.xa.healthcheck.spi;

import java.util.List;

import com.yammer.metrics.core.HealthCheck;

/**
 * This is a factory of health checks
 * 
 * Implement this interface if you do not know in advance how many health checks
 * you will have<br/>
 * This interface is suitable when you deal with dynamic configuration<br/>
 * Example of checks about datasource in application server
 * 
 * @author jmacke
 * 
 */
public interface HealthCheckFactory {

	/**
	 * 
	 * @return a list of healthcheck to be checked, null in case of error
	 */
	List<HealthCheck> getHealthChecks();
}
