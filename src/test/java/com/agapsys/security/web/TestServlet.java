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
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {TestServlet.URL_PUBLIC, TestServlet.URL_SECURED, TestServlet.URL_EXTRA_SECURED, TestServlet.URL_PARAM_ACTION})
public class TestServlet extends AbstractActionServlet {
	// CLASS SCOPE =============================================================
	public static final String URL_PUBLIC = "/public";
	public static final String URL_SECURED = "/secured";
	public static final String URL_EXTRA_SECURED = "/extra-secured";
	public static final String URL_PARAM_ACTION = "/params";
	
	public static final String PARAM_ACTION_TYPE = "action";
	public static final String PARAM_ACTION1_VALUE = "1";
	public static final String PARAM_ACTION2_VALUE = "2";
	
	private static class TestAction extends AbstractWebAction {

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
	
	private static final AbstractWebAction PARAM_ACTION1 = new TestAction(HttpMethod.GET) {

		@Override
		protected void run(HttpServletRequest request, HttpServletResponse response, Object... params) {
			try {
				response.getWriter().println("action1");
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	};
	
	private static final AbstractWebAction PARAM_ACTION2 = new TestAction(HttpMethod.GET) {

		@Override
		protected void run(HttpServletRequest request, HttpServletResponse response, Object... params) {
			try {
				response.getWriter().println("action2");
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	};
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private AbstractWebAction getParamAction(HttpServletRequest req, HttpServletResponse resp) {
		Map<String, String[]> parameters = req.getParameterMap();
		String[] paramActionValue = parameters.get(PARAM_ACTION_TYPE);
				
		if (paramActionValue == null) {
			try {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
				resp.getWriter().println("Missing paramter: " + PARAM_ACTION_TYPE);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
			
		switch(paramActionValue[0]) {
			case PARAM_ACTION1_VALUE:
				return PARAM_ACTION1;

			case PARAM_ACTION2_VALUE:
				return PARAM_ACTION2;

			default:
				try {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
					resp.getWriter().println(String.format("Invalid parameter for %s: %s", PARAM_ACTION_TYPE, paramActionValue[0]));
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
		}
				
		return null;
	}
	
	@Override
	protected AbstractWebAction getAction(HttpServletRequest req, HttpServletResponse resp) {
		String servletPath = req.getServletPath();
		
		switch(servletPath) {
			case URL_PUBLIC:
				return PUBLIC_ACTION;
				
			case URL_SECURED:
				return SECURED_ACTION;
				
			case URL_EXTRA_SECURED:
				return EXTRA_SECURED_ACTION;
				
			case URL_PARAM_ACTION:
				return getParamAction(req, resp);
					
			default:
				throw new RuntimeException("Invalid url: " + servletPath);
		}
	}
	// =========================================================================
}
