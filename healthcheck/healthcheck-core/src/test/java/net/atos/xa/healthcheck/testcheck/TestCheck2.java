package net.atos.xa.healthcheck.testcheck;

import com.yammer.metrics.core.HealthCheck;

public class TestCheck2 extends HealthCheck {

	public TestCheck2() {
		super("testCheck2");
	}

	@Override
	protected Result check() throws Exception {
		return Result.healthy();
	}

}
