package net.atos.xa.healthcheck.checks;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.HealthCheck;

/**
 * 
 * <p>
 * A simple http check. It returns healthy if the http response code = 200 else
 * it returns unhealthy
 * </p>
 */
public class SimpleHttpCheck extends HealthCheck {

	/** the logger */
	private static Logger log = LoggerFactory.getLogger(SimpleHttpCheck.class
			.getName());

	/**
	 * the remote host
	 */
	private HttpHost host;

	/**
	 * the proxy host
	 */
	private HttpHost proxyHost;

	/**
	 * the http request to send
	 */
	private HttpRequest request;

	/**
	 * 
	 * The following parameters can be used to customize the behavior of this
	 * class:
	 * <ul>
	 * <li>{@link org.apache.http.params.CoreProtocolPNames#PROTOCOL_VERSION}</li>
	 * <li>
	 * {@link org.apache.http.params.CoreProtocolPNames#STRICT_TRANSFER_ENCODING}
	 * </li>
	 * <li>
	 * {@link org.apache.http.params.CoreProtocolPNames#HTTP_ELEMENT_CHARSET}</li>
	 * <li>{@link org.apache.http.params.CoreProtocolPNames#USE_EXPECT_CONTINUE}
	 * </li>
	 * <li>{@link org.apache.http.params.CoreProtocolPNames#WAIT_FOR_CONTINUE}</li>
	 * <li>{@link org.apache.http.params.CoreProtocolPNames#USER_AGENT}</li>
	 * <li>{@link org.apache.http.params.CoreConnectionPNames#TCP_NODELAY}</li>
	 * <li>{@link org.apache.http.params.CoreConnectionPNames#SO_TIMEOUT}</li>
	 * <li>{@link org.apache.http.params.CoreConnectionPNames#SO_LINGER}</li>
	 * <li>{@link org.apache.http.params.CoreConnectionPNames#SO_REUSEADDR}</li>
	 * <li>
	 * {@link org.apache.http.params.CoreConnectionPNames#SOCKET_BUFFER_SIZE}</li>
	 * <li>
	 * {@link org.apache.http.params.CoreConnectionPNames#CONNECTION_TIMEOUT}</li>
	 * <li>{@link org.apache.http.params.CoreConnectionPNames#MAX_LINE_LENGTH}</li>
	 * <li>{@link org.apache.http.params.CoreConnectionPNames#MAX_HEADER_COUNT}</li>
	 * <li>
	 * {@link org.apache.http.params.CoreConnectionPNames#STALE_CONNECTION_CHECK}
	 * </li>
	 * <li>{@link org.apache.http.conn.params.ConnRoutePNames#FORCED_ROUTE}</li>
	 * <li>{@link org.apache.http.conn.params.ConnRoutePNames#LOCAL_ADDRESS}</li>
	 * <li>{@link org.apache.http.conn.params.ConnRoutePNames#DEFAULT_PROXY}</li>
	 * <li>{@link org.apache.http.cookie.params.CookieSpecPNames#DATE_PATTERNS}</li>
	 * <li>
	 * {@link org.apache.http.cookie.params.CookieSpecPNames#SINGLE_COOKIE_HEADER}
	 * </li>
	 * <li>{@link org.apache.http.auth.params.AuthPNames#CREDENTIAL_CHARSET}</li>
	 * <li>{@link org.apache.http.client.params.ClientPNames#COOKIE_POLICY}</li>
	 * <li>
	 * {@link org.apache.http.client.params.ClientPNames#HANDLE_AUTHENTICATION}</li>
	 * <li>{@link org.apache.http.client.params.ClientPNames#HANDLE_REDIRECTS}</li>
	 * <li>{@link org.apache.http.client.params.ClientPNames#MAX_REDIRECTS}</li>
	 * <li>
	 * {@link org.apache.http.client.params.ClientPNames#ALLOW_CIRCULAR_REDIRECTS}
	 * </li>
	 * <li>{@link org.apache.http.client.params.ClientPNames#VIRTUAL_HOST}</li>
	 * <li>{@link org.apache.http.client.params.ClientPNames#DEFAULT_HOST}</li>
	 * <li>{@link org.apache.http.client.params.ClientPNames#DEFAULT_HEADERS}</li>
	 * <li>
	 * {@link org.apache.http.client.params.ClientPNames#CONN_MANAGER_TIMEOUT}</li>
	 * </ul>
	 * 
	 */
	private HttpParams params;

	public SimpleHttpCheck(String name, HttpHost host, HttpHost proxyHost,
			HttpRequest request, HttpParams params) {
		super("simpleHttpCheck " + name);
		this.host = host;
		this.proxyHost = proxyHost;
		this.request = request;
		if (params == null)
			this.params = new BasicHttpParams();
		else
			this.params = params;
	}

	public SimpleHttpCheck(String name, HttpHost host, HttpRequest request,
			HttpParams params) {
		this(name, host, null, request, params);
	}

	public SimpleHttpCheck(String name, HttpHost host, String uri,
			HttpParams params) {
		this(name, host, new HttpGet(uri), params);
	}

	public SimpleHttpCheck(String name, HttpHost host, HttpParams params) {
		this(name, host, new HttpGet("/"), params);
	}

	@Override
	protected Result check() throws Exception {

		HttpClient httpclient = new DefaultHttpClient(params);

		HttpContext context = new BasicHttpContext(null);
		if (proxyHost != null)
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxyHost);

		HttpResponse response = httpclient.execute(host, request, context);

		if (log.isDebugEnabled()) {
			log.debug("[HealthCheck] Response body for check {} : \n {} \n {}",
					getName(), response.getStatusLine(),
					getResponseBody(response));
		}

		if (response.getStatusLine().getStatusCode() == 200)
			return Result.healthy();
		else {
			return Result.unhealthy("HTTP status code "
					+ response.getStatusLine().getStatusCode() + " for check "
					+ getName());
		}

	}

	private String getResponseBody(final HttpResponse response)
			throws HttpResponseException, IOException {
		HttpEntity entity = response.getEntity();
		return entity == null ? null : EntityUtils.toString(entity);
	}
}
