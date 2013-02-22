package net.atos.xa.healthcheck.servlet;

import com.yammer.metrics.reporting.AdminServlet;
import com.yammer.metrics.reporting.MetricsServlet;
import com.yammer.metrics.reporting.PingServlet;
import com.yammer.metrics.reporting.ThreadDumpServlet;

/**
 * Complement the {@link AdminServlet} servlet from the <a
 * href="http ://metrics.codahale.com">Yammer metrics framework</a>
 * 
 * 
 */
public class XaAdminServlet extends AdminServlet {

	private static final long serialVersionUID = 2423573475231726991L;

	public XaAdminServlet() {
		super(new XaHealthCheckServlet(), new MetricsServlet(),
				new PingServlet(), new ThreadDumpServlet(),
				DEFAULT_HEALTHCHECK_URI, DEFAULT_METRICS_URI, DEFAULT_PING_URI,
				DEFAULT_THREADS_URI);
	}

}
