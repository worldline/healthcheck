package net.atos.xa.healthcheck.util;

import junit.framework.Assert;

import org.junit.Test;

public class JdbcUtilTestCase {

	@Test
	public void testIdentifyJdbcDriverFromUrl() {

		String oracle = JdbcUtil
				.identifyJdbcDriverFromUrl("jdbc:oracle:thin:@localhost:1521:database");

		Assert.assertNotNull(oracle);
		Assert.assertTrue(oracle.toLowerCase().contains("oracle"));

		String mysql = JdbcUtil
				.identifyJdbcDriverFromUrl("jdbc:mysql://localhost:3306/database");

		Assert.assertNotNull(mysql);
		Assert.assertTrue(mysql.toLowerCase().contains("mysql"));

		Assert.assertNull(JdbcUtil
				.identifyJdbcDriverFromUrl("jdbc:unknowndriver"));

		Assert.assertNull(JdbcUtil.identifyJdbcDriverFromUrl(null));

	}

	@Test
	public void testIdentifyJdbcDriverFromName() {

		String oracle = JdbcUtil
				.identifyJdbcDriverFromName("Oracle JDBC driver");

		Assert.assertNotNull(oracle);
		Assert.assertEquals(JdbcUtil.JDBC_DRIVER_ORACLE, oracle);

		String mysql = JdbcUtil
				.identifyJdbcDriverFromName("MySQL-AB JDBC Driver");

		Assert.assertNotNull(mysql);
		Assert.assertEquals(JdbcUtil.JDBC_DRIVER_MYSQL, mysql);

		Assert.assertNull(JdbcUtil.identifyJdbcDriverFromName("unknown driver"));

		Assert.assertNull(JdbcUtil.identifyJdbcDriverFromName(null));

	}

	@Test
	public void testGetValidationQueryFromJdbcDriver() {
		Assert.assertEquals(JdbcUtil.SELECT_1_FROM_DUAL_QUERY, JdbcUtil
				.getValidationQueryFromJdbcDriver(JdbcUtil.JDBC_DRIVER_ORACLE));

		Assert.assertEquals(JdbcUtil.SELECT_1_QUERY, JdbcUtil
				.getValidationQueryFromJdbcDriver(JdbcUtil.JDBC_DRIVER_MYSQL));

		Assert.assertNull(JdbcUtil.getValidationQueryFromJdbcDriver(null));

		Assert.assertNull(JdbcUtil
				.getValidationQueryFromJdbcDriver("unkown driver"));

	}
}
