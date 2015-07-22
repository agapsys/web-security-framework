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

public abstract class AbstractActionServlet extends HttpServlet {
	/**
	 * @return Associated action to given servlet path. Application shall implement this method.
	 * @param req servlet request
	 * @param resp servlet response
	 */
	protected abstract AbstractWebAction getAction(HttpServletRequest req, HttpServletResponse resp);

	/**
	 * 
	 * @param req servlet request
	 * @param resp servlet response
	 * @return parameters to be passed to action returned by {@linkplain AbstractActionServlet#getAction(HttpServletRequest, HttpServletResponse)}. Default implementation returns an empty array
	 */
	protected Object[] getActionParameters(HttpServletRequest req, HttpServletResponse resp) {
		return new Object[0];
	}
	
	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			AbstractWebAction action = getAction(req, resp);
			
			if (action != null) {
				action.execute(req, resp, getActionParameters(req, resp));
			}
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
	
	/** 
	 * Called when there is a {@linkplain WebSecurityException} during servlet processing.
	 * Default implementation just sends a HTTP 403 status in the response
	 * @param exception exception
	 */
	protected void onWebSecutiryException(WebSecurityException exception) {
		try {
			exception.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/** 
	 * Called when there is a {@linkplain MethodNotAllowedException} during servlet processing.
	 * Default implementation just sends a HTTP 405 status in the response
	 * @param exception exception
	 */
	protected void onMethodNotAllowed(MethodNotAllowedException exception) {
		try {
			exception.getResponse().sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}