package net.atos.xa.healthcheck.testcheck;

import com.yammer.metrics.core.HealthCheck;

public class TestCheck3 extends HealthCheck {

	public TestCheck3() {
		super("testCheck3");
	}

	@Override
	protected Result check() throws Exception {
		return Result.healthy();
	}

}
