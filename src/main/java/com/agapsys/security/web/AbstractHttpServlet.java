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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractHttpServlet extends HttpServlet {
	protected abstract AbstractWebAction getAction(String servletPath);

	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			AbstractWebAction action = getAction(req.getServletPath());
			
			if (action == null)
				throw new RuntimeException(String.format("There is no action for '%s'", req.getServletPath()));
			
			action.execute(req, resp);
		} catch(MethodNotAllowedException ex) {
			onMethodNotAllowed(ex);
		}catch (WebSecurityException ex) {
			onWebSecutiryException(ex);
		}
	}
	
	@Override
	public final void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		super.service(req, res);
	}

	@Override
	protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doDelete(req, resp);
	}

	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doGet(req, resp);
	}

	@Override
	protected final void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doHead(req, resp);
	}

	@Override
	protected final void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doOptions(req, resp);
	}

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
	}

	@Override
	protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPut(req, resp);
	}

	@Override
	protected final void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doTrace(req, resp);
	}
	
	
	protected void onWebSecutiryException(WebSecurityException exception) {
		try {
			exception.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	protected void onMethodNotAllowed(MethodNotAllowedException exception) {
		try {
			exception.getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
