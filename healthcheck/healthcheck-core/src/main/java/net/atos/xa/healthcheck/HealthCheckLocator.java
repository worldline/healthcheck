package net.atos.xa.healthcheck;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.yammer.metrics.core.HealthCheck;

public class HealthCheckLocator {
	// we delegate calls to HealthCheckLocatorUnit class
	private volatile static HealthCheckLocatorUnit resourceLocatorInstance = null;

	// lock to synchronize context initialization
	private static Lock initializationLock = new ReentrantLock();

	public static Collection<HealthCheck> getAllHealthChecks() {
		if (resourceLocatorInstance == null) {
			createLocator();
		}
		return resourceLocatorInstance.getAllHealthChecks();
	}

	public static Collection<HealthCheck> getFilteredHealthChecks(
			List<String> excludeChecks) {
		if (resourceLocatorInstance == null) {
			createLocator();
		}
		return resourceLocatorInstance.getFilteredHealthChecks(excludeChecks);
	}

	/**
	 * Create our HealthCheckLocatorUnit instance
	 * 
	 */
	private static HealthCheckLocatorUnit createLocator() {
		// double check locking to be thread safe
		try {
			initializationLock.lock();
			if (resourceLocatorInstance == null) {
				resourceLocatorInstance = new HealthCheckLocatorUnit();
			}
		} finally {
			initializationLock.unlock();
		}
		return resourceLocatorInstance;
	}

}
