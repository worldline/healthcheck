<%@page import="java.util.HashMap"%>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.yammer.metrics.core.HealthCheck" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="net.atos.xa.healthcheck.HealthCheckReport" %>
<%@ page import="net.atos.xa.healthcheck.HealthCheckManager" %>
<%@ page import="net.atos.xa.healthcheck.HealthCheckResult" %>

<%!

public void jspInit(){
	
	
	/*
	* list of excluded healthchecks (list of names separated by a semicolon)
	* Example, if we have the following healthchecks with the names: "check1",
	* "check2" and "check3".<br/>
	* A call to getFilteredHealthChecksList("check2;check3") will return only
	* the "check1" healthcheck<br/>
	*/
	
	Map<String, String> environment = new HashMap<String, String>();
	environment.put("wasCheck.jdbcDatasources", "jdbc/WLP_COM;jdbc/WLP_CAS");
	environment.put("wasCheck.jdbcTimeout", "1");
	Collection<HealthCheck> healthChecks = HealthCheckManager
			.getAllHealthChecks();

	HealthCheckManager.registerHealthChecks(healthChecks);
	
}

%>

<%
	long start = System.currentTimeMillis();

	final Map<String, HealthCheckResult> results = HealthCheckManager
		.runHealthchecksWithDetailedReport();

	response.setContentType("text/plain");
	response.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
	
	final PrintWriter writer = response.getWriter();

	writer.format("Server host: %s (%s)\n", request.getLocalName(),
			request.getLocalAddr());
	writer.format("Client host: %s (%s)\n\n", request.getRemoteHost(),
			request.getRemoteAddr());

	if (results.isEmpty()) {
		response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
	} else {
		
		boolean allOk = true;
		for (HealthCheckResult result : results.values()) {
			if (!result.isHealthy()) {
				allOk =  false;
			}
		}
		
		if (allOk) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	HealthCheckReport.produceReport(response.getWriter(), results);

	writer.format("\nTotal execution time : %s ms \n",
			System.currentTimeMillis() - start);
	writer.close();
	
	

%>






