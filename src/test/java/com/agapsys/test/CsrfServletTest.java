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
import com.agapsys.http.HttpHeader;
import com.agapsys.http.HttpResponse;
import com.agapsys.http.HttpResponse.StringResponse;
import com.agapsys.security.web.SessionCsrfSecurityManager;
import com.agapsys.security.web.WebSecurity;
import com.agapsys.security.web.WebSecurityFilter;
import com.agapsys.sevlet.container.ServletContainer;
import com.agapsys.sevlet.container.ServletContainerBuilder;
import com.agapsys.sevlet.container.StacktraceErrorHandler;
import com.agapsys.test.app.CsrfController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
public class CsrfServletTest {
	// CLASS SCOPE =============================================================
	private static final String BASE_URL = "/csrf";
	
	@BeforeClass
	public static void beforeClass() {
		WebSecurity.init(new SessionCsrfSecurityManager(), "com.agapsys.test.app.CsrfController");
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
	
	public static HttpClient doLogin(ServletContainer sc, LoginType loginType, boolean assignCsrf) {
		if (loginType == null)
			throw new IllegalArgumentException("Login type must be informed");
		
		HttpClient client = new HttpClient();
		StringResponse resp = sc.doRequest(client, new HttpGet(loginType.getUri()));
		HttpHeader csrfHeader = resp.getFirstHeader(SessionCsrfSecurityManager.CSRF_HEADER);
		Assert.assertNotNull(csrfHeader);
		System.out.println(String.format("CSRF token: %s", csrfHeader.getValue()));
		if (assignCsrf)
			client.addDefaultHeaders(csrfHeader);
		
		return client;
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final ServletContainer sc;
	
	public CsrfServletTest() {
		sc = new ServletContainerBuilder()
			.registerFilter(WebSecurityFilter.class, "/*")
			.registerServlet(CsrfController.class, "/csrf/*")
			.setErrorHandler(new StacktraceErrorHandler())
			.build();
	}
	
	@Before
	public void before() {
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
		client = doLogin(sc, LoginType.SIMPLE, true);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Extra user ----------------------------------------------------------
		client = doLogin(sc, LoginType.EXTRA, true);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Admin user ---------------------------------------------------------
		client = doLogin(sc, LoginType.ADMIN, true);
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
		
		// Simple user without CSRF token --------------------------------------
		client = doLogin(sc, LoginType.SIMPLE, false);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStatus(403, resp);
		// ---------------------------------------------------------------------
		
		// Simple user wit CSRF token ------------------------------------------
		client = doLogin(sc, LoginType.SIMPLE, true);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Extra user ----------------------------------------------------------
		client = doLogin(sc, LoginType.EXTRA, true);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Admin user without CSRF ---------------------------------------------
		client = doLogin(sc, LoginType.ADMIN, false);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStatus(403, resp);
		// ---------------------------------------------------------------------
		
		// Admin user with CSRF-------------------------------------------------
		client = doLogin(sc, LoginType.ADMIN, true);
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
		client = doLogin(sc, LoginType.SIMPLE, true);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStatus(403, resp);
		// ---------------------------------------------------------------------
		
		// Extra user without CSRF token --------------------------------------
		client = doLogin(sc, LoginType.EXTRA, false);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStatus(403, resp);
		// ---------------------------------------------------------------------
		
		// Extra user ----------------------------------------------------------
		client = doLogin(sc, LoginType.EXTRA, true);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Admin user without CSRF ---------------------------------------------
		client = doLogin(sc, LoginType.ADMIN, false);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStatus(403, resp);
		// ---------------------------------------------------------------------
		
		// Admin user with CSRF-------------------------------------------------
		client = doLogin(sc, LoginType.ADMIN, true);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
	}
	// =========================================================================
}
