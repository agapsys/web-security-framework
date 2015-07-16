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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base servlet for web applications.
 * The main difference between this class and {@linkplain javax.servlet.http.HttpServlet}
 * is the fact that is possible to disable the servlet via
 * {@linkplain HttpServlet#setActive(boolean)}
 * 
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class HttpServlet extends javax.servlet.http.HttpServlet {
	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (isActive()) {
			doService(req, resp);
		} else {
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		}
	}
	
	/** 
	 * Process request. 
	 * The request will be processed only if {@linkplain HttpServlet#isActive()} returns true.
	 */
	protected void doService(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.service(req, resp);
	}
	
	/** 
	 * Returns the activity state of this servlet.
	 * @return Default implementation returns always true
	 */
	protected boolean isActive() {
		return true;
	}
}
