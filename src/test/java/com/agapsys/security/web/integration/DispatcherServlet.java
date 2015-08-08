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

import com.agapsys.security.DuplicateException;
import com.agapsys.security.Role;
import com.agapsys.security.web.AbstractWebAction;
import com.agapsys.security.web.ActionDispatcherServlet;
import com.agapsys.security.web.HttpMethod;
import com.agapsys.security.web.Session;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/*")
public class DispatcherServlet extends ActionDispatcherServlet {
	// CLASS SCOPE =============================================================
	public static final String URL_LOGIN = "/login";
	public static final String URL_LOGOUT = "/logout";
	
	public static final String URL_PUBLIC        = "/public";
	public static final String URL_SECURED       = "/secured";
	public static final String URL_EXTRA_SECURED = "/extra-secured";
	public static final String URL_PARAM_ACTION  = "/params";
	
	private static class TestAction extends AbstractWebAction {

		public TestAction(Role... requiredRoles) throws IllegalArgumentException, DuplicateException {
			super(requiredRoles);
		}
		
		@Override
		protected void run(HttpServletRequest request, HttpServletResponse response, Object... params) {
			User connectedUser = (User) Session.getSessionUser(request);
			
			try {
				response.getWriter().print(String.format("Hi %s", connectedUser != null ? connectedUser.getUsername() : "<anonymous>"));
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	private static final AbstractWebAction PUBLIC_ACTION = new TestAction();
	
	private static final AbstractWebAction SECURED_ACTION = new TestAction(Defs.SECURED_ROLE);
	
	private static final AbstractWebAction EXTRA_SECURED_ACTION = new TestAction(Defs.EXTRA_SECURED_ROLE);
	
	static {
		registerAction(LoginAction.getInstance(), HttpMethod.POST, URL_LOGIN);
		registerAction(LogoutAction.getInstance(), HttpMethod.GET, URL_LOGOUT);
		registerAction(PUBLIC_ACTION,        HttpMethod.GET, URL_PUBLIC);
		registerAction(SECURED_ACTION,       HttpMethod.GET, URL_SECURED);
		registerAction(EXTRA_SECURED_ACTION, HttpMethod.GET, URL_EXTRA_SECURED);
	}
	// =========================================================================
}
