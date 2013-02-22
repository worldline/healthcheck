package net.atos.xa.healthcheck.testcheck;

import com.yammer.metrics.core.HealthCheck;

public class TestCheck1 extends HealthCheck {

	public TestCheck1() {
		super("testCheck1");
	}

	@Override
	protected Result check() throws Exception {
		return Result.healthy();
	}

}
