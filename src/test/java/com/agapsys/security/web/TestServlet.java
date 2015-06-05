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

import com.agapsys.security.Role;
import com.agapsys.security.RoleRepository;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/test-servlet")
public class TestServlet extends HttpServlet {
	public static final Role TEST_ROLE;
	
	static {
		RoleRepository roles = RoleRepository.getSingletonInstance();
		TEST_ROLE = roles.createRole("com.agapsys.security.web.testRole");
	}
	
	private static final AbstractWebAction SECURED_ACTION = new AbstractWebAction(TEST_ROLE) {

		@Override
		protected void run(HttpServletRequest request, HttpServletResponse response, Object... params) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
	};
	
	private static final AbstractWebAction
	
	do
}
