package net.atos.xa.healthcheck.checks;

import junit.framework.Assert;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;

import com.yammer.metrics.core.HealthCheck.Result;

public class DatabaseCheckTestCase {

	@Test
	public void testExecuteCheck() throws Exception {

		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:activiti");
		ds.setUser("sa");
		ds.setPassword("sa");

		DatabaseCheck check = new DatabaseCheck("name", ds, null, 0);
		Result result = check.check();
		Assert.assertEquals(Result.healthy(), result);

	}

}
