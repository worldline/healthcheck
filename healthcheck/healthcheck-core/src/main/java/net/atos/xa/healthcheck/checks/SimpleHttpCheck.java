package net.atos.xa.healthcheck.checks;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpRequest;
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
 * it returns unhealthy This check uses Apache http client
 * </p>
 */
public class SimpleHttpCheck extends HealthCheck {

	/** the logger */
	private static Logger log = LoggerFactory.getLogger(SimpleHttpCheck.class
			.getName());

	/**
	 * the remote host
	 */
	private final HttpHost host;

	/**
	 * the proxy host
	 */
	private final HttpHost proxyHost;

	/**
	 * the http request to send
	 */
	private final HttpRequest request;

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
	private final HttpParams params;

	/**
	 * A simple check for an HTTP request (support any kind of HTTP request)
	 * through a proxy
	 * 
	 * @param name
	 *            the name of this check
	 * @param host
	 *            the target host (hostname, port, scheme to use)
	 * @param proxyHost
	 *            the proxy host (hostname, port, scheme to use)
	 * @param request
	 *            the HTTP request (can be any HTTP request)
	 * @param params
	 *            the additional parameters (charset, timeout etc..)
	 */
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

	/**
	 * A simple check for an HTTP request (support any kind of HTTP request)
	 * 
	 * @param name
	 *            the name of this check
	 * @param host
	 *            the target host (hostname, port, scheme to use)
	 * @param request
	 *            the HTTP request (can be any HTTP request)
	 * @param params
	 *            the additional parameters (charset, timeout etc..)
	 */
	public SimpleHttpCheck(String name, HttpHost host, HttpRequest request,
			HttpParams params) {
		this(name, host, null, request, params);
	}

	/**
	 * A simple check for an HTTP GET request
	 * 
	 * @param name
	 *            the name of this check
	 * @param hostname
	 *            the target host name
	 * @param port
	 *            the target post
	 * @param uri
	 *            the uri to call on this target host
	 * @param params
	 *            the additional parameters (charset, timeout etc..)
	 */
	public SimpleHttpCheck(String name, String hostname, int port, String uri,
			HttpParams params) {
		this(name, new HttpHost(hostname, port), new BasicHttpRequest("GET",
				uri), params);
	}

	/**
	 * A simple check for an HTTP GET request
	 * 
	 * @param name
	 *            the name of this check
	 * @param host
	 *            the target host (hostname, port, scheme to use)
	 * @param uri
	 *            the uri to call on this target host
	 * @param params
	 *            the additional parameters (charset, timeout etc..)
	 */
	public SimpleHttpCheck(String name, HttpHost host, String uri,
			HttpParams params) {
		this(name, host, new BasicHttpRequest("GET", uri), params);
	}

	/**
	 * A simple check for an HTTP GET request (use default parameters)
	 * 
	 * @param name
	 *            the name of this check
	 * @param host
	 *            the target host (hostname, port, scheme to use)
	 * @param uri
	 *            the uri to call on this target host
	 */
	public SimpleHttpCheck(String name, HttpHost host, String uri) {
		this(name, host, new BasicHttpRequest("GET", uri), null);
	}

	/**
	 * A simple check for an HTTP GET request with uri "/"
	 * 
	 * @param name
	 *            the name of this check
	 * @param host
	 *            the target host (hostname, port, scheme to use)
	 * @param params
	 *            the additional parameters (charset, timeout etc..)
	 */
	public SimpleHttpCheck(String name, HttpHost host, HttpParams params) {
		this(name, host, new BasicHttpRequest("GET", "/"), params);
	}

	@Override
	protected Result check() throws Exception {

		log.info("[HealthCheck] execute check {}", getName());

		HttpClient httpclient = new DefaultHttpClient(params);

		HttpContext context = new BasicHttpContext(null);
		if (proxyHost != null) {

			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxyHost);
		}

		httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,
				false);

		log.info(
				"[HealthCheck] use execute HTTP request {} on host {} through the proxy {}",
				request, host, proxyHost);
		HttpResponse response = httpclient.execute(host, request, context);

		if (log.isDebugEnabled()) {
			log.debug("[HealthCheck] Response body for check {} : \n {} \n {}",
					getName(), response.getStatusLine(),
					getResponseBody(response));
		}

		if (response.getStatusLine().getStatusCode() == 200) {
			log.info("[HealthCheck] check {} is OK", getName());

			return Result.healthy();
		}

		else {
			log.info("[HealthCheck] check {} is ERROR", getName());
			return Result.unhealthy("HTTP status code "
					+ response.getStatusLine().getStatusCode() + " for check "
					+ getName());
		}

	}

	private String getResponseBody(final HttpResponse response)
			throws IOException {
		HttpEntity entity = response.getEntity();
		return entity == null ? null : EntityUtils.toString(entity);
	}
}
