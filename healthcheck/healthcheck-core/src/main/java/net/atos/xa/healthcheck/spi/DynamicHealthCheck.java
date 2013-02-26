package net.atos.xa.healthcheck.spi;

import java.util.List;

import com.yammer.metrics.core.HealthCheck;

/**
 * Implement this interface if you do not know in advance how many health checks
 * you will have<br/>
 * This interface is suitable when you deal with dynamic configuration<br/>
 * Example of checks about datasource in application server
 * 
 * @author jmacke
 * 
 */
public interface DynamicHealthCheck {

	/**
	 * 
	 * @return a list of healthcheck to be checked, null in case of error
	 */
	List<HealthCheck> getHealthChecks();
}
