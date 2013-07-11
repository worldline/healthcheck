package net.atos.xa.healthcheck;

import com.yammer.metrics.core.HealthCheck.Result;

/**
 * Wrap the result from Healthcheck
 * 
 */
public class HealthCheckResult {

	private Result result;

	private long executionTime;

	public HealthCheckResult(Result result, long executionTime) {
		this.executionTime = executionTime;
		this.result = result;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	/**
	 * Returns {@code true} if the result indicates the component is healthy;
	 * {@code false} otherwise.
	 * 
	 * @return {@code true} if the result indicates the component is healthy
	 */
	public boolean isHealthy() {
		return result.isHealthy();
	}

	/**
	 * Returns any additional message for the result, or {@code null} if the
	 * result has no message.
	 * 
	 * @return any additional message for the result, or {@code null}
	 */
	public String getMessage() {
		return result.getMessage();
	}

	/**
	 * Returns any exception for the result, or {@code null} if the result has
	 * no exception.
	 * 
	 * @return any exception for the result, or {@code null}
	 */
	public Throwable getError() {
		return result.getError();
	}
}
