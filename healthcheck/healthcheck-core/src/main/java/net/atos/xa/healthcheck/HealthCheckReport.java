package net.atos.xa.healthcheck;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;

/**
 * 
 * Utility class that produce the healthCheck textual report
 * 
 */
public class HealthCheckReport {

	/**
	 * Produce a textual report for the healthcheck
	 * 
	 * @param registry
	 *            the healthcheck registry which contains all the checks to
	 *            execute
	 * @param writer
	 *            the writer
	 */
	public static void produceReport(HealthCheckRegistry registry,
			final PrintWriter writer) {

		final Map<String, HealthCheck.Result> results = registry
				.runHealthChecks();

		if (results.isEmpty()) {
			writer.println("! No health checks registered.");
		} else {
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

	}

}
