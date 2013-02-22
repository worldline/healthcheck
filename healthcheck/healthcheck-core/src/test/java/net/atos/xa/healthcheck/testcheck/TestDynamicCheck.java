package net.atos.xa.healthcheck.testcheck;

import java.util.ArrayList;
import java.util.List;

import net.atos.xa.healthcheck.spi.DynamicHealthCheck;

import com.yammer.metrics.core.HealthCheck;

public class TestDynamicCheck implements DynamicHealthCheck {

	public List<HealthCheck> getHealthChecks() {

		List<HealthCheck> results = new ArrayList<HealthCheck>();
		results.add(new TestCheck5());
		return results;
	}

	public class TestCheck5 extends HealthCheck {

		public TestCheck5() {
			super("testCheck5");
		}

		@Override
		protected Result check() throws Exception {
			// TODO Auto-generated method stub
			return Result.healthy();
		}

	}
}
