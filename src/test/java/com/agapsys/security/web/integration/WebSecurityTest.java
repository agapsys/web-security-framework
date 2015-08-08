/*
 * Copyright 2015 Agapsys Tecnologia Ltda-ME.
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
package com.agapsys.security.web.integration;

import com.agapsys.security.web.AbstractWebAction;
import com.agapsys.sevlet.test.ApplicationContext;
import com.agapsys.sevlet.test.HttpClient;
import com.agapsys.sevlet.test.HttpGet;
import com.agapsys.sevlet.test.HttpPost;
import com.agapsys.sevlet.test.HttpRequest.HttpHeader;
import com.agapsys.sevlet.test.HttpResponse;
import com.agapsys.sevlet.test.ServletContainer;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

public class WebSecurityTest {
	// CLASS SCOPE =============================================================
	@BeforeClass
	public static void beforeClass() {
		User.addUser("foo",  "foo-password",  Defs.SECURED_ROLE);
		User.addUser("foo1", "foo-password1", Defs.EXTRA_SECURED_ROLE);
	}
	// =========================================================================
	private ServletContainer sc;

	@Before
	public void setUp() {
		sc = new ServletContainer();
		
		ApplicationContext context = new ApplicationContext();
		context.registerServlet(DispatcherServlet.class);
		
		sc.registerContext(context, "/");
		sc.startServer();
	}
	
	@After
	public void tearDown() {
		sc.stopServer();
	}
	
	@Test
	public void actionNotFound() {
		HttpResponse response = sc.doGet(
			String.format(
				"%s?%s=%s&%s=%s", 
				DispatcherServlet.URL_LOGIN, 
				LoginAction.PARAM_USERNAME,
				"foo", 
				LoginAction.PARAM_PASSOWRD,
				"foo-password"
			)
		);
		
		assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatusCode());
	}
	
	@Test
	public void invalidCrendentials() {
		HttpPost post = new HttpPost(sc, DispatcherServlet.URL_LOGIN);
		post.addParameter(LoginAction.PARAM_USERNAME, "foo");
		post.addParameter(LoginAction.PARAM_PASSOWRD, "bar");
		
		HttpResponse response = sc.doPost(post);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
	}
	
	@Test
	public void validCredentials() {
		HttpPost post = new HttpPost(sc, DispatcherServlet.URL_LOGIN);
		post.addParameter(LoginAction.PARAM_USERNAME, "foo");
		post.addParameter(LoginAction.PARAM_PASSOWRD, "foo-password");
		
		HttpResponse response = sc.doPost(post);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		assertTrue(response.containsHeader(AbstractWebAction.CSRF_HEADER));
		HttpHeader csrfHeader = response.getFirstHeader(AbstractWebAction.CSRF_HEADER);
		
		System.out.println("CSRF token: " + csrfHeader.getValue());
	}
	
	@Test
	public void accessingPublicUrl() {
		HttpResponse response = sc.doGet(DispatcherServlet.URL_PUBLIC);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		assertEquals("Hi <anonymous>", response.getResponseBody());
	}
	
	@Test
	public void accessingRestrictedUrlAnonymously() {
		HttpResponse response = sc.doGet(DispatcherServlet.URL_SECURED);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
		
		response = sc.doGet(DispatcherServlet.URL_EXTRA_SECURED);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
	}
	
	@Test
	public void accessingRestrictedUrlLoggedInButWithCsrfToken() {
		HttpClient client = new HttpClient();
		
		HttpPost post = new HttpPost(sc, DispatcherServlet.URL_LOGIN);
		post.addParameter(LoginAction.PARAM_USERNAME, "foo");
		post.addParameter(LoginAction.PARAM_PASSOWRD, "foo-password");
		HttpResponse response = sc.doPost(client, post);
		HttpHeader csrfTokenHeader = response.getFirstHeader(AbstractWebAction.CSRF_HEADER);
		
		HttpGet getRequest = new HttpGet(sc, DispatcherServlet.URL_SECURED);
		getRequest.addHeaders(csrfTokenHeader);
		response = sc.doGet(client, getRequest);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		
		response = sc.doGet(client, DispatcherServlet.URL_SECURED); // <-- here there is no CSRF token header
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
		
		// Logging out
		response = sc.doGet(client, DispatcherServlet.URL_LOGOUT);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		
		// Trying accessing again
		getRequest = new HttpGet(sc, DispatcherServlet.URL_SECURED);
		getRequest.addHeaders(csrfTokenHeader);
		response = sc.doGet(client, getRequest);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
	}
	
	@Test
	public void accessingRestrictedUrlLoggedInButWithInsufficientPrivileges() {
		HttpClient fullPrivilegesClient = new HttpClient();
		HttpPost post = new HttpPost(sc, DispatcherServlet.URL_LOGIN);
		post.addParameter(LoginAction.PARAM_USERNAME, "foo1");
		post.addParameter(LoginAction.PARAM_PASSOWRD, "foo-password1");
		HttpResponse response = sc.doPost(fullPrivilegesClient, post);
		HttpHeader fullPrivilegesCsrfToken = response.getFirstHeader(AbstractWebAction.CSRF_HEADER);
		HttpGet fullPrivilegesGet = new HttpGet(sc, DispatcherServlet.URL_EXTRA_SECURED);
		fullPrivilegesGet.addHeaders(fullPrivilegesCsrfToken);
		
		HttpClient insufficientPrivilegesClient = new HttpClient();
		post = new HttpPost(sc, DispatcherServlet.URL_LOGIN);
		post.addParameter(LoginAction.PARAM_USERNAME, "foo");
		post.addParameter(LoginAction.PARAM_PASSOWRD, "foo-password");
		HttpHeader insufficientPrivilegesCsrfToken = response.getFirstHeader(AbstractWebAction.CSRF_HEADER);
		HttpGet insufficientPrivilegesGet = new HttpGet(sc, DispatcherServlet.URL_EXTRA_SECURED);
		insufficientPrivilegesGet.addHeaders(insufficientPrivilegesCsrfToken);
		
		response = sc.doGet(fullPrivilegesClient, fullPrivilegesGet);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		assertEquals("Hi foo1", response.getResponseBody());
		
		response = sc.doGet(insufficientPrivilegesClient, insufficientPrivilegesGet);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
		
		sc.doGet(fullPrivilegesClient, DispatcherServlet.URL_LOGOUT); // <-- logout client with full privileges
		
		response = sc.doGet(fullPrivilegesClient, fullPrivilegesGet);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
	}
}