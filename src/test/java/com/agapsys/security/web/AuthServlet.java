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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {AuthServlet.URL_LOGIN, AuthServlet.URL_LOGOUT})
public class AuthServlet extends AbstractHttpServlet {
	// CLASS SCOPE =============================================================
	public static final String URL_LOGIN = "/login";
	public static final String URL_LOGOUT = "/logout";
	
	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_PASSOWRD = "password";
	
	public static final int    XSRF_TOKEN_LENGTH = 8;

	private static final AbstractWebAction LOGIN_ACTION = new AbstractWebAction(HttpMethod.POST) {
		@Override
		protected void run(HttpServletRequest request, HttpServletResponse response, Object... params) {
			String username = request.getParameter(PARAM_USERNAME);
			String password = request.getParameter(PARAM_PASSOWRD);
			
			try {
				if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
				} else {
					TestUser user = TestUser.getUser(username, password);
					if (user == null) {
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
					} else {
						registerSessionUser(request, response, user);
					}
				}
			}catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
		
		@Override
		protected int getXsrfTokenLength() {
			return XSRF_TOKEN_LENGTH;
		}
	};
	
	private static final AbstractWebAction LOGOUT_ACTION = new AbstractWebAction(HttpMethod.GET) {

		@Override
		protected void run(HttpServletRequest request, HttpServletResponse response, Object... params) {
			invalidateSession(request);
		}
	};
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	@Override
	protected AbstractWebAction getAction(String servletPath) {
		switch (servletPath) {
			case URL_LOGIN:
				return LOGIN_ACTION;
				
			case URL_LOGOUT:
				return LOGOUT_ACTION;
				
			default:
				throw new UnsupportedOperationException("Unsupported servletPath: " + servletPath);
		}
	}
	// =========================================================================


}
