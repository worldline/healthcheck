package net.atos.xa.healthcheck.servlet;

import java.util.Collection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.reporting.HealthCheckServlet;

/**
 * Complement the {@link HealthCheckServlet} servlet from the <a
 * href="http ://metrics.codahale.com">Yammer metrics framework</a>
 * 
 * 
 */
public class XaHealthCheckServlet extends HealthCheckServlet {

	private static final String EXCLUDE_CHECKS = "excludeChecks";

	/**
	 * 
	 */
	private static final long serialVersionUID = 5207476216122582925L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		Collection<HealthCheck> healthChecks = RegisterHealthCheck
				.getFilteredHealthChecksList(config
						.getInitParameter(EXCLUDE_CHECKS));

		RegisterHealthCheck.registerHealthChecks(healthChecks);

	}
}
