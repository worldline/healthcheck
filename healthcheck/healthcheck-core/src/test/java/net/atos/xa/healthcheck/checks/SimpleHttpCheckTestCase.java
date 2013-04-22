package net.atos.xa.healthcheck.checks;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

import junit.framework.Assert;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleHttpCheckTestCase {

	static ServerSocket serversocket = null;

	@BeforeClass
	public static void init() throws Exception {

		serversocket = new ServerSocket(0);
		System.out.println("port used : " + serversocket.getLocalPort());
		Thread t = new RequestListenerThread(serversocket, null);
		t.setDaemon(false);
		t.start();
	}

	@Test
	public void testSimpleHttpCheck() throws Exception {

		HttpHost host = new HttpHost("localhost", serversocket.getLocalPort());
		SimpleHttpCheck check1 = new SimpleHttpCheck("ok", host, "/ok", null);
		Assert.assertTrue(check1.execute().isHealthy());

		// internal server error
		testErrorCase(host, "500");

		// move temporarily
		testErrorCase(host, "302");

		// move permanently
		testErrorCase(host, "301");

		// not found
		testErrorCase(host, "404");

		// forbidden
		testErrorCase(host, "403");

		// payment Required
		testErrorCase(host, "402");

		// Unauthorized
		testErrorCase(host, "401");

		// Bad Request
		testErrorCase(host, "400");

	}

	private void testErrorCase(HttpHost host, String httpErrorCode) {
		SimpleHttpCheck check = new SimpleHttpCheck("code" + httpErrorCode,
				host, "/code" + httpErrorCode, null);
		Assert.assertFalse(check.execute().isHealthy());
	}

	static class RequestListenerThread extends Thread {

		private final HttpParams params;
		private final HttpService httpService;

		public RequestListenerThread(ServerSocket serversocket,
				final String docroot) throws IOException {

			this.params = new SyncBasicHttpParams();

			// Set up the HTTP protocol processor
			HttpProcessor httpproc = new ImmutableHttpProcessor(
					new HttpResponseInterceptor[] { new ResponseDate(),
							new ResponseServer(), new ResponseContent(),
							new ResponseConnControl() });

			// Set up request handlers
			HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
			reqistry.register("*", new HttpFileHandler());

			// Set up the HTTP service
			this.httpService = new HttpService(httpproc,
					new DefaultConnectionReuseStrategy(),
					new DefaultHttpResponseFactory(), reqistry, this.params);
		}

		@Override
		public void run() {
			System.out.println("Listening on port "
					+ serversocket.getLocalPort());
			while (!Thread.interrupted()) {
				try {
					// Set up HTTP connection
					Socket socket = serversocket.accept();
					DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
					System.out.println("Incoming connection from "
							+ socket.getInetAddress());
					conn.bind(socket, this.params);

					// Start worker thread
					Thread t = new WorkerThread(this.httpService, conn);
					t.setDaemon(true);
					t.start();
				} catch (InterruptedIOException ex) {
					break;
				} catch (IOException e) {
					System.err
							.println("I/O error initialising connection thread: "
									+ e.getMessage());
					break;
				}
			}
		}
	}

	static class HttpFileHandler implements HttpRequestHandler {

		public HttpFileHandler() {
			super();
		}

		public void handle(final HttpRequest request,
				final HttpResponse response, final HttpContext context)
				throws HttpException, IOException {

			String target = request.getRequestLine().getUri();

			if (target.endsWith("/ok")) {
				response.setStatusCode(HttpStatus.SC_OK);
			} else if (target.endsWith("/code302")) {
				response.setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY);
				response.setHeader("Location", "http://localhost:"
						+ serversocket.getLocalPort() + "/ok");
			} else if (target.endsWith("/code301")) {
				response.setStatusCode(HttpStatus.SC_MOVED_PERMANENTLY);
				response.setHeader("Location", "http://localhost:"
						+ serversocket.getLocalPort() + "/ok");
			} else if (target.endsWith("/code401")) {
				response.setStatusCode(HttpStatus.SC_UNAUTHORIZED);

			} else if (target.endsWith("/code402")) {
				response.setStatusCode(HttpStatus.SC_PAYMENT_REQUIRED);

			} else if (target.endsWith("/code403")) {
				response.setStatusCode(HttpStatus.SC_FORBIDDEN);

			} else if (target.endsWith("/code404")) {
				response.setStatusCode(HttpStatus.SC_NOT_FOUND);

			} else {
				response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

			System.out.println(response.getStatusLine());
		}

	}

	static class WorkerThread extends Thread {

		private final HttpService httpservice;
		private final HttpServerConnection conn;

		public WorkerThread(final HttpService httpservice,
				final HttpServerConnection conn) {
			super();
			this.httpservice = httpservice;
			this.conn = conn;
		}

		@Override
		public void run() {
			System.out.println("New connection thread");
			HttpContext context = new BasicHttpContext(null);
			try {
				while (!Thread.interrupted() && this.conn.isOpen()) {
					this.httpservice.handleRequest(this.conn, context);
				}
			} catch (ConnectionClosedException ex) {
				System.err.println("Client closed connection");
			} catch (IOException ex) {
				System.err.println("I/O error: " + ex.getMessage());
			} catch (Exception ex) {
				System.err.println("Unrecoverable HTTP protocol violation: "
						+ ex.getMessage());
			} finally {
				try {
					this.conn.shutdown();
				} catch (IOException ignore) {
				}
			}
		}

	}
}
