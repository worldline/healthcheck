package net.atos.xa.healthcheck.checks;

import junit.framework.Assert;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.yammer.metrics.core.HealthCheck.Result;

public class DatabaseCheckTestCase {

	@Test
	public void testWithNullParam() {
		DatabaseCheck check = new DatabaseCheck("name", null, null, null, 0);
		Assert.assertFalse(check.execute().isHealthy());
	}

	@Test
	public void testExecuteCheck() {

		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:activiti");
		ds.setUser("sa");
		ds.setPassword("sa");

		DatabaseCheck check = new DatabaseCheck("name", ds, null, null, 0);
		Result result = check.execute();
		Assert.assertEquals(Result.healthy(), result);

	}

	@Test
	public void testDatabaseConnectionKO() {

		MysqlDataSource mysql = new MysqlDataSource();
		mysql.setUrl("jdbc:mysql://localhost:3306/dummy");
		mysql.setUser("dummy");
		mysql.setPassword("dummy");
		DatabaseCheck check = new DatabaseCheck("name", mysql, null, null, 0);
		Assert.assertFalse("Must be unhealthy", check.execute().isHealthy());

	}

}
