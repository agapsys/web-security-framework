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
import com.agapsys.security.web.SessionCsrfSecurityManager;
import com.agapsys.security.web.WebApplicationFilter;
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.sevlet.test.ServletContainerBuilder;
import com.agapsys.sevlet.test.StacktraceErrorHandler;
import com.agapsys.test.TestUtils.LoginType;
import com.agapsys.test.app.SessionServlet;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
public class CsrfServletTest {
	// CLASS SCOPE =============================================================
	@BeforeClass
	public static void beforeClass() {
		MockedWebSecurity.allowMultipleInitialization();
		MockedWebSecurity.init(new SessionCsrfSecurityManager(), "com.agapsys.test.app.CsrfServlet");
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
		String uri = "/session/publicGet";
		
		// Unlogged access -----------------------------------------------------
		resp = sc.doRequest(new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Simple user ---------------------------------------------------------
		client = TestUtils.doSessionLogin(sc, LoginType.SIMPLE);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Extra user ----------------------------------------------------------
		client = TestUtils.doSessionLogin(sc, LoginType.EXTRA);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Admin user ---------------------------------------------------------
		client = TestUtils.doSessionLogin(sc, LoginType.ADMIN);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
	}
	
	@Test
	public void securedGetTest() {
		HttpResponse.StringResponse resp;
		HttpClient client;
		String uri = "/session/securedGet";
		
		// Unlogged access -----------------------------------------------------
		resp = sc.doRequest(new HttpGet(uri));
		TestUtils.assertStatus(401, resp);
		// ---------------------------------------------------------------------
		
		// Simple user ---------------------------------------------------------
		client = TestUtils.doSessionLogin(sc, LoginType.SIMPLE);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Extra user ----------------------------------------------------------
		client = TestUtils.doSessionLogin(sc, LoginType.EXTRA);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Admin user ---------------------------------------------------------
		client = TestUtils.doSessionLogin(sc, LoginType.ADMIN);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
	}
	
	@Test
	public void extraSecuredGetTest() {
		HttpResponse.StringResponse resp;
		HttpClient client;
		String uri = "/session/extraSecuredGet";
		
		// Unlogged access -----------------------------------------------------
		resp = sc.doRequest(new HttpGet(uri));
		TestUtils.assertStatus(401, resp);
		// ---------------------------------------------------------------------
		
		// Simple user ---------------------------------------------------------
		client = TestUtils.doSessionLogin(sc, LoginType.SIMPLE);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStatus(403, resp);
		// ---------------------------------------------------------------------
		
		// Extra user ----------------------------------------------------------
		client = TestUtils.doSessionLogin(sc, LoginType.EXTRA);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
		
		// Admin user ---------------------------------------------------------
		client = TestUtils.doSessionLogin(sc, LoginType.ADMIN);
		resp = sc.doRequest(client, new HttpGet(uri));
		TestUtils.assertStringResponse(200, "OK", resp);
		// ---------------------------------------------------------------------
	}
	// =========================================================================
}
