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

import com.agapsys.security.DuplicateException;
import com.agapsys.security.Role;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {TestServlet.URL_PUBLIC, TestServlet.URL_SECURED, TestServlet.URL_EXTRA_SECURED})
public class TestServlet extends AbstractHttpServlet {
	// CLASS SCOPE =============================================================
	public static final String URL_PUBLIC = "/public";
	public static final String URL_SECURED = "/secured";
	public static final String URL_EXTRA_SECURED = "/extra-secured";
	
	private static final class TestAction extends AbstractWebAction {

		public TestAction(Role... requiredRoles) throws IllegalArgumentException, DuplicateException {
			super(requiredRoles);
		}

		public TestAction(HttpMethod acceptedMethod, Role... requiredRoles) throws IllegalArgumentException {
			super(acceptedMethod, requiredRoles);
		}
		
		@Override
		protected void run(HttpServletRequest request, HttpServletResponse response, Object... params) {
			TestUser connectedUser = (TestUser) getSessionUser(request);
			
			try {
				response.getWriter().print(String.format("Hi %s", connectedUser != null ? connectedUser.getUsername() : "<anonymous>"));
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	private static final AbstractWebAction PUBLIC_ACTION = new TestAction(HttpMethod.GET);
	
	private static final AbstractWebAction SECURED_ACTION = new TestAction(HttpMethod.GET, TestDefs.SECURED_ROLE);
	
	private static final AbstractWebAction EXTRA_SECURED_ACTION = new TestAction(HttpMethod.GET, TestDefs.EXTRA_SECURED_ROLE);
	
	private static final Map<String, AbstractWebAction> ACTION_MAP = new LinkedHashMap<>();
	
	static {
		ACTION_MAP.put(URL_PUBLIC, PUBLIC_ACTION);
		ACTION_MAP.put(URL_SECURED, SECURED_ACTION);
		ACTION_MAP.put(URL_EXTRA_SECURED, EXTRA_SECURED_ACTION);
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	@Override
	protected AbstractWebAction getAction(String servletPath) {
		return ACTION_MAP.get(servletPath);
	}
	// =========================================================================
}
