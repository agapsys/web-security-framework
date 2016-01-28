/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agapsys.test;

import com.agapsys.http.HttpClient;
import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpResponse;
import com.agapsys.security.web.SessionSecurityManager;
import com.agapsys.security.web.WebApplicationFilter;
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.sevlet.test.ServletContainerBuilder;
import com.agapsys.sevlet.test.StacktraceErrorHandler;
import com.agapsys.test.app.SessionServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
public class SessionServletTest {
	// CLASS SCOPE =============================================================
	private static final String BASE_URL = "/session";
	
	@BeforeClass
	public static void beforeClass() {
		MockedWebSecurity.allowMultipleInitialization();
		MockedWebSecurity.init(new SessionSecurityManager(), "com.agapsys.test.app.SessionServlet");
	}
	
	public static enum LoginType {
		SIMPLE(BASE_URL + "/doSimpleLogin"),
		EXTRA(BASE_URL + "/doExtraLogin"),
		ADMIN(BASE_URL + "/doAdminLogin");
		
		private final String uri;
		
		private LoginType(String uri) {
			this.uri = uri;
		}
		
		public String getUri() {
			return uri;
		}
	}
	
	public static HttpClient doLogin(ServletContainer sc, LoginType loginType) {
		if (loginType == null)
			throw new IllegalArgumentException("Login type must be informed");
		
		HttpClient client = new HttpClient();
		sc.doRequest(client, new HttpGet(loginType.getUri()));
		return client;
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	ServletContainer sc;
	
	@Before
	public void before() {
		sc = new ServletContainerBuilder()
			.addRootContext()
				.registerFilter(WebApplicationFilter.class, "/*")
				.registerServlet(SessionServlet.class)
				.setErrorHandler(new StacktraceErrorHandler())
			.endContext()
		.build();
		sc.startServer();
	}
	
	@After
	public void after() {
		sc.stopServer();
	}
	
	@Test
	public void publicGetTest() {
		HttpResponse.StringResponse resp;
		HttpClient client;
		String uri = BASE_URL + "/publicGet";
		
		// Unlogged access -----------------------------------------------------
		resp = sc.doRequest(new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Simple user ---------------------------------------------------------
		client = doLogin(sc, LoginType.SIMPLE);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Extra user ----------------------------------------------------------
		client = doLogin(sc, LoginType.EXTRA);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Admin user ---------------------------------------------------------
		client = doLogin(sc, LoginType.ADMIN);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
	}
	
	@Test
	public void securedGetTest() {
		HttpResponse.StringResponse resp;
		HttpClient client;
		String uri = BASE_URL + "/securedGet";
		
		// Unlogged access -----------------------------------------------------
		resp = sc.doRequest(new HttpGet(uri));
		TestUtils.assertStatus(401, resp);
		// ---------------------------------------------------------------------
		
		// Simple user ---------------------------------------------------------
		client = doLogin(sc, LoginType.SIMPLE);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Extra user ----------------------------------------------------------
		client = doLogin(sc, LoginType.EXTRA);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Admin user ---------------------------------------------------------
		client = doLogin(sc, LoginType.ADMIN);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
	}
	
	@Test
	public void extraSecuredGetTest() {
		HttpResponse.StringResponse resp;
		HttpClient client;
		String uri = BASE_URL + "/extraSecuredGet";
		
		// Unlogged access -----------------------------------------------------
		resp = sc.doRequest(new HttpGet(uri));
		TestUtils.assertStatus(401, resp);
		// ---------------------------------------------------------------------
		
		// Simple user ---------------------------------------------------------
		client = doLogin(sc, LoginType.SIMPLE);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStatus(403, resp);
		// ---------------------------------------------------------------------
		
		// Extra user ----------------------------------------------------------
		client = doLogin(sc, LoginType.EXTRA);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Admin user ---------------------------------------------------------
		client = doLogin(sc, LoginType.ADMIN);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
	}
	// =========================================================================
}
