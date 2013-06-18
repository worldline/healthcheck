package net.atos.xa.healthcheck.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.atos.xa.healthcheck.HealthCheckManager;
import net.atos.xa.healthcheck.HealthCheckReport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;

/**
 * <p>
 * Complement the {@link HealthCheckServlet} servlet from the <a
 * href="http ://metrics.codahale.com">Yammer metrics framework</a>
 * 
 * Initialization parameters
 * 
 * param name "excludeChecks": a list of healthcheck names separated by
 * semi-colon ';'. white spaces are ignored between each semi-colon and name. If
 * a name does not match an available check, it is ignored.
 * 
 * 
 * Example: <textarea rows="11" cols="10 name="xml" readonly> <servlet>
 * <servlet-name>HealthCheckServlet</servlet-name> <servlet-class>
 * net.atos.xa.healthcheck.servlet.XaHealthCheckServlet </servlet-class>
 * <init-param> <param-name>excludeChecks</param-name>
 * <param-value>check1;check2</param-value> </init-param> </servlet> </textarea>
 * </p>
 * 
 */
public class XaHealthCheckServlet extends HttpServlet {

	private static final String EXCLUDE_CHECKS = "excludeChecks";

	/** the logger */
	private static Logger log = LoggerFactory
			.getLogger(XaHealthCheckServlet.class.getName());

	/**
	 * The attribute name of the {@link HealthCheckRegistry} instance in the
	 * servlet context.
	 */
	public static final String REGISTRY_ATTRIBUTE = XaHealthCheckServlet.class
			.getName() + ".registry";
	private static final String CONTENT_TYPE = "text/plain";

	/**
	 * 
	 */
	private static final long serialVersionUID = 5207476216122582925L;

	@Override
	public void init(ServletConfig config) throws ServletException {

		log.info("[HealthCheck] register healthcheck");

		Enumeration<String> initParameterNames = config.getInitParameterNames();
		Map<String, String> environment = null;
		while (initParameterNames.hasMoreElements()) {
			String initParameterName = initParameterNames.nextElement();
			log.info("Init param name to add {}", initParameterName);
			if (!initParameterName.trim().equals(EXCLUDE_CHECKS)) {
				if (environment == null) {
					environment = new HashMap<String, String>();
				}

				log.info("Init param value to add {}",
						config.getInitParameter(initParameterName));

				environment.put(initParameterName,
						config.getInitParameter(initParameterName));
			}
		}

		Collection<HealthCheck> healthChecks = HealthCheckManager
				.getFilteredHealthChecksList(
						config.getInitParameter(EXCLUDE_CHECKS), environment);

		HealthCheckManager.registerHealthChecks(healthChecks);

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		log.info("[HealthCheck] call healtchCheck servlet");

		long start = System.currentTimeMillis();

		resp.setContentType(CONTENT_TYPE);
		resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
		final PrintWriter writer = resp.getWriter();

		if (req.getRequestURI().endsWith("/deactivateChecks")) {

			// deactivate the register checks
			HealthCheckManager.deactivateAllChecks();
			writer.println("All checks are deactivated");
		} else if (req.getRequestURI().endsWith("/activateChecks")) {

			Collection<HealthCheck> healthChecks = HealthCheckManager
					.getFilteredHealthChecksList(req.getSession()
							.getServletContext()
							.getInitParameter(EXCLUDE_CHECKS));

			if (healthChecks != null) {
				for (HealthCheck healthCheck : healthChecks) {
					HealthCheckManager.activateCheck(healthCheck.getName());
				}
			}

			writer.println("All checks are activated");

		} else {
			final Map<String, HealthCheck.Result> results = HealthCheckManager
					.runHealthchecksWithDetailedReport();

			writer.format("Server host: %s (%s)\n", req.getLocalName(),
					req.getLocalAddr());
			writer.format("Client host: %s (%s)\n\n", req.getRemoteHost(),
					req.getRemoteAddr());

			if (results.isEmpty()) {
				resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
			} else {
				if (isAllHealthy(results)) {
					resp.setStatus(HttpServletResponse.SC_OK);
				} else {
					resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			}

			HealthCheckReport.produceReport(writer, results);

		}

		writer.format("\nTotal execution time : %s ms \n",
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
