package net.atos.xa.healthcheck.spi;

import java.util.List;

import com.yammer.metrics.core.HealthCheck;

/**
 * Implement this interface if you do not know in advance how many health checks
 * you will have<br/>
 * Example of checks about datasource in application server
 * 
 * @author jmacke
 * 
 */
public interface DynamicHealthCheck {

	List<HealthCheck> getHealthChecks();
}
