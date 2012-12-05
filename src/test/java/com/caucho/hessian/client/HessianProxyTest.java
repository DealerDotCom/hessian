package com.caucho.hessian.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.caucho.hessian.server.HessianServlet;

public class HessianProxyTest {

	private static final String DATA = "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdf" +
			"asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasd" +
			"fasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfas" +
			"dfasdf";
	
	private Server server;
	private String url;
	
	@Before
	public void setup() throws Exception {
		
		int port = 9876;
		String serviceName = TestService.class.getSimpleName();
		url = "http://localhost:" + port + "/" + serviceName;

		server = new Server(port);

		HessianServlet hessianServlet = new HessianServlet();
		hessianServlet.setAPIClass(TestService.class);
		hessianServlet.setService(new TestServiceImpl());

		ServletHolder holder = new ServletHolder(serviceName, hessianServlet);

		ServletContextHandler ctxHandler = new ServletContextHandler();
		ctxHandler.addServlet(holder, "/" + serviceName);

		server.setHandler(ctxHandler);
		server.start();
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
	}

	/**
	 * Verifies fix of caucho tracked bug 0003655: http://bugs.caucho.com/view.php?id=3655
	 */
	@Test
	public void testServiceReturnInputStream() throws Exception {

		HessianProxyFactory pf = new HessianProxyFactory();
		TestService service = (TestService)pf.create(TestService.class, url);

		InputStream in = service.getStream();
		String received;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			IOUtils.copy(in, out);
			out.close();
			received = new String(out.toByteArray());
		}
		finally {
			in.close();
		}

		assertEquals(DATA, received);
	}
	
	public static interface TestService {
		InputStream getStream();
	}

	public static class TestServiceImpl implements TestService {

		@Override
		public InputStream getStream() {
			return new ByteArrayInputStream(HessianProxyTest.DATA.getBytes());
		}
	}
}
