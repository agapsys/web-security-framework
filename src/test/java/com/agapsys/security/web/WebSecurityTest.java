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
package com.agapsys.security.web;

import com.agapsys.sevlet.test.AppContext;
import com.agapsys.sevlet.test.HttpClient;
import com.agapsys.sevlet.test.HttpGet;
import com.agapsys.sevlet.test.HttpPost;
import com.agapsys.sevlet.test.HttpRequest.HttpHeader;
import com.agapsys.sevlet.test.HttpResponse;
import com.agapsys.sevlet.test.ServletContainter;
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
		TestUser.addUser("foo", "foo-password", TestDefs.SECURED_ROLE);
		TestUser.addUser("foo1", "foo-password1", TestDefs.EXTRA_SECURED_ROLE);
	}
	// =========================================================================
	private ServletContainter sc;

	@Before
	public void setUp() {
		sc = new ServletContainter();
		
		AppContext context = new AppContext();
		context.registerServlet(TestServlet.class);
		context.registerServlet(AuthServlet.class);
		
		sc.registerContext(context, "/");
		sc.startServer();
	}
	
	@After
	public void tearDown() {
		sc.stopServer();
	}
	
	@Test
	public void unsupportedMethod() {
		HttpResponse response = sc.doGet(
			String.format(
				"%s?%s=%s&%s=%s", 
				AuthServlet.URL_LOGIN, 
				AuthServlet.PARAM_USERNAME,
				"foo", 
				AuthServlet.PARAM_PASSOWRD,
				"foo-password"
			)
		);
		
		assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatusCode());
	}
	
	@Test
	public void invalidCrendentials() {
		HttpPost post = new HttpPost(sc, AuthServlet.URL_LOGIN);
		post.addParameter(AuthServlet.PARAM_USERNAME, "foo");
		post.addParameter(AuthServlet.PARAM_PASSOWRD, "bar");
		
		HttpResponse response = sc.doPost(post);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
	}
	
	@Test
	public void validCredentials() {
		HttpPost post = new HttpPost(sc, AuthServlet.URL_LOGIN);
		post.addParameter(AuthServlet.PARAM_USERNAME, "foo");
		post.addParameter(AuthServlet.PARAM_PASSOWRD, "foo-password");
		
		HttpResponse response = sc.doPost(post);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		assertTrue(response.containsHeader(AbstractWebAction.XSRF_HEADER));
		HttpHeader xsrfHeader = response.getFirstHeader(AbstractWebAction.XSRF_HEADER);
		
		System.out.println("XSRF token: " + xsrfHeader.getValue());
		assertEquals(AuthServlet.XSRF_TOKEN_LENGTH, xsrfHeader.getValue().length());
	}
	
	@Test
	public void accessingPublicUrl() {
		HttpResponse response = sc.doGet(TestServlet.URL_PUBLIC);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		assertEquals("Hi <anonymous>", response.getResponseBody());
	}
	
	@Test
	public void accessingRestrictedUrlAnonymously() {
		HttpResponse response = sc.doGet(TestServlet.URL_SECURED);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
		
		response = sc.doGet(TestServlet.URL_EXTRA_SECURED);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
	}
	
	@Test
	public void accessingParamterAction() {
		HttpResponse response = sc.doGet(
			String.format(
				"%s?%s=%s", 
				TestServlet.URL_PARAM_ACTION,
				TestServlet.PARAM_ACTION_TYPE,
				TestServlet.PARAM_ACTION1_VALUE
			)
		);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		assertEquals("action1", response.getResponseBody());
		
		response = sc.doGet(
			String.format(
				"%s?%s=%s", 
				TestServlet.URL_PARAM_ACTION,
				TestServlet.PARAM_ACTION_TYPE,
				TestServlet.PARAM_ACTION2_VALUE
			)
		);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		assertEquals("action2", response.getResponseBody());
	}
	
	@Test
	public void accessingRestrictedUrlLoggedInButWithXsrfToken() {
		HttpClient client = new HttpClient();
		
		HttpPost post = new HttpPost(sc, AuthServlet.URL_LOGIN);
		post.addParameter(AuthServlet.PARAM_USERNAME, "foo");
		post.addParameter(AuthServlet.PARAM_PASSOWRD, "foo-password");
		HttpResponse response = sc.doPost(client, post);
		HttpHeader xsrfTokenHeader = response.getFirstHeader(AbstractWebAction.XSRF_HEADER);
		
		HttpGet getRequest = new HttpGet(sc, TestServlet.URL_SECURED);
		getRequest.addHeaders(xsrfTokenHeader);
		response = sc.doGet(client, getRequest);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		
		response = sc.doGet(client, TestServlet.URL_SECURED); // <-- here there is no CSRF token header
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
		
		// Logging out
		response = sc.doGet(client, AuthServlet.URL_LOGOUT);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		
		// Trying accessing again
		getRequest = new HttpGet(sc, TestServlet.URL_SECURED);
		getRequest.addHeaders(xsrfTokenHeader);
		response = sc.doGet(client, getRequest);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
	}
	
	@Test
	public void accessingRestrictedUrlLoggedInButWithInsufficientPrivileges() {
		HttpClient fullPrivilegesClient = new HttpClient();
		HttpPost post = new HttpPost(sc, AuthServlet.URL_LOGIN);
		post.addParameter(AuthServlet.PARAM_USERNAME, "foo1");
		post.addParameter(AuthServlet.PARAM_PASSOWRD, "foo-password1");
		HttpResponse response = sc.doPost(fullPrivilegesClient, post);
		HttpHeader fullPrivilegesXsrfToken = response.getFirstHeader(AbstractWebAction.XSRF_HEADER);
		HttpGet fullPrivilegesGet = new HttpGet(sc, TestServlet.URL_EXTRA_SECURED);
		fullPrivilegesGet.addHeaders(fullPrivilegesXsrfToken);
		
		HttpClient insufficientPrivilegesClient = new HttpClient();
		post = new HttpPost(sc, AuthServlet.URL_LOGIN);
		post.addParameter(AuthServlet.PARAM_USERNAME, "foo");
		post.addParameter(AuthServlet.PARAM_PASSOWRD, "foo-password");
		HttpHeader insufficientPrivilegesXsrfToken = response.getFirstHeader(AbstractWebAction.XSRF_HEADER);
		HttpGet insufficientPrivilegesGet = new HttpGet(sc, TestServlet.URL_EXTRA_SECURED);
		insufficientPrivilegesGet.addHeaders(insufficientPrivilegesXsrfToken);
		
		response = sc.doGet(fullPrivilegesClient, fullPrivilegesGet);
		assertEquals(HttpServletResponse.SC_OK, response.getStatusCode());
		assertEquals("Hi foo1", response.getResponseBody());
		
		response = sc.doGet(insufficientPrivilegesClient, insufficientPrivilegesGet);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
		
		sc.doGet(fullPrivilegesClient, AuthServlet.URL_LOGOUT); // <-- logout client with full privileges
		
		response = sc.doGet(fullPrivilegesClient, fullPrivilegesGet);
		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatusCode());
	}
}
