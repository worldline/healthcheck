package net.atos.xa.healthcheck.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.reporting.HealthCheckServlet;

/**
 * Complement the {@link HealthCheckServlet} servlet from the <a
 * href="http ://metrics.codahale.com">Yammer metrics framework</a>
 * 
 * 
 */
public class XaHealthCheckServlet extends HealthCheckServlet {

	private static final String EXCLUDE_CHECKS = "excludeChecks";

	/** the logger */
	private static Logger log = LoggerFactory
			.getLogger(XaHealthCheckServlet.class.getName());

	/**
	 * The attribute name of the {@link HealthCheckRegistry} instance in the
	 * servlet context.
	 */
	public static final String REGISTRY_ATTRIBUTE = HealthCheckServlet.class
			.getName() + ".registry";
	private static final String CONTENT_TYPE = "text/plain";

	private HealthCheckRegistry registry;

	/**
	 * Creates a new {@link HealthCheckServlet} with the given
	 * {@link HealthCheckRegistry}.
	 * 
	 * @param registry
	 *            a {@link HealthCheckRegistry}
	 */
	public XaHealthCheckServlet(HealthCheckRegistry registry) {
		this.registry = registry;
	}

	/**
	 * Creates a new {@link HealthCheckServlet} with the default
	 * {@link HealthCheckRegistry}.
	 */
	public XaHealthCheckServlet() {
		this(HealthChecks.defaultRegistry());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5207476216122582925L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		final Object o = config.getServletContext().getAttribute(
				REGISTRY_ATTRIBUTE);
		if (o instanceof HealthCheckRegistry) {
			this.registry = (HealthCheckRegistry) o;
		}

		log.info("[HealthCheck] register healthcheck");
		Collection<HealthCheck> healthChecks = RegisterHealthCheck
				.getFilteredHealthChecksList(config
						.getInitParameter(EXCLUDE_CHECKS));

		RegisterHealthCheck.registerHealthChecks(healthChecks);

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		log.info("[HealthCheck] call healtchCheck servlet");

		long start = System.currentTimeMillis();

		final Map<String, HealthCheck.Result> results = registry
				.runHealthChecks();
		resp.setContentType(CONTENT_TYPE);
		resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
		final PrintWriter writer = resp.getWriter();
		if (results.isEmpty()) {
			resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
			writer.println("! No health checks registered.");
		} else {
			if (isAllHealthy(results)) {
				resp.setStatus(HttpServletResponse.SC_OK);
			} else {
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			for (Map.Entry<String, HealthCheck.Result> entry : results
					.entrySet()) {
				final HealthCheck.Result result = entry.getValue();
				if (result.isHealthy()) {
					if (result.getMessage() != null) {
						writer.format("* %s=OK (executed at %s)\n  %s\n",
								entry.getKey(), new Date(), result.getMessage());
					} else {
						writer.format("* %s=OK (executed at %s)\n",
								entry.getKey(), new Date());
					}
				} else {
					if (result.getMessage() != null) {
						writer.format("! %s=ERROR\n!  %s\n", entry.getKey(),
								result.getMessage());
					}

					@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
					final Throwable error = result.getError();
					if (error != null) {
						writer.println();
						error.printStackTrace(writer);
						writer.println();
					}
				}
			}
		}

		writer.format("Total execution time : %s ms \n",
				System.currentTimeMillis() - start);
		writer.close();
	}

	private static boolean isAllHealthy(Map<String, HealthCheck.Result> results) {
		for (HealthCheck.Result result : results.values()) {
			if (!result.isHealthy()) {
				return false;
			}
		}
		return true;
	}
}
