package net.atos.xa.healthcheck;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.core.HealthCheckRegistry;

/**
 * 
 * Utility class that produce the healthCheck textual report
 * 
 */
public final class HealthCheckReport {

	private HealthCheckReport() {

	}

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

		final Map<String, HealthCheckResult> healthcheckResults = new HashMap<String, HealthCheckResult>();
		for (String key : results.keySet()) {
			healthcheckResults.put(key, new HealthCheckResult(results.get(key),
					-1));
		}

		produceReport(writer, healthcheckResults);

	}

	/**
	 * Produce a textual report for the healthcheck
	 * 
	 * @param writer
	 *            the writer
	 */
	public static void produceReport(final PrintWriter writer) {

		final Map<String, HealthCheckResult> results = HealthCheckManager
				.runHealthchecksWithDetailedReport();

		produceReport(writer, results);
	}

	/**
	 * Produce a textual report for the healthcheck
	 * 
	 * @param writer
	 *            the writer
	 * @param results
	 *            the results
	 */
	public static void produceReport(final PrintWriter writer,
			final Map<String, HealthCheckResult> results) {

		if (results.isEmpty()) {
			writer.println("! No health checks registered.");
		} else {
			for (Map.Entry<String, HealthCheckResult> entry : results
					.entrySet()) {
				final HealthCheckResult result = entry.getValue();
				if (result.isHealthy()) {
					if (result.getMessage() != null) {
						writer.format(
								"* %s=OK (executed at %s) in %sms\n  %s\n",
								entry.getKey(), new Date(),
								result.getExecutionTime(), result.getMessage());
					} else {
						writer.format("* %s=OK (executed at %s) in %sms\n",
								entry.getKey(), new Date(),
								result.getExecutionTime());
					}
				} else {
					if (result.getMessage() != null) {
						writer.format("! %s=ERROR in  %sms\n!  %s\n",
								entry.getKey(), result.getExecutionTime(),
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
