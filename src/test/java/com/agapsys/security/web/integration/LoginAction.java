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
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginAction extends AbstractWebAction {
	// CLASS SCOPE =============================================================
	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_PASSOWRD = "password";
	
	private static LoginAction singleton = null;
	
	public static LoginAction getInstance() {
		if (singleton == null)
			singleton = new LoginAction();
		
		return singleton;
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private LoginAction() {
		super();
	}
	
	@Override
	protected void run(HttpServletRequest request, HttpServletResponse response, Object... params) {
		String username = request.getParameter(PARAM_USERNAME);
		String password = request.getParameter(PARAM_PASSOWRD);

		try {
			if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			} else {
				User user = User.getUser(username, password);
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
	// =========================================================================
}
