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
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/*")
public class ActionDispatcherServlet extends HttpServlet {
	// CLASS SCOPE =============================================================
	private static final Map<HttpMethod, Map<String, AbstractWebAction>> ACTION_MAP = new LinkedHashMap<>();

	/**
	 * Registers an action with given URL
	 * @param action action to be associated with given URL and HTTP method
	 * @param httpMethod associated HTTP method
	 * @param url URL associated with given action and HTTP method
	 */
	public static void registerAction(AbstractWebAction action, HttpMethod httpMethod, String url) {
		if (action == null) {
			throw new IllegalArgumentException("action == null");
		}

		if (httpMethod == null) {
			throw new IllegalArgumentException("httpMethod == null");
		}

		if (url == null || url.trim().isEmpty()) {
			throw new IllegalArgumentException("url == null || url.trim().isEmpty()");
		}

		url = url.trim();

		Map<String, AbstractWebAction> map = ACTION_MAP.get(httpMethod);

		if (map == null) {
			map = new LinkedHashMap<>();
			ACTION_MAP.put(httpMethod, map);
		}

		if (map.containsKey(url)) {
			throw new DuplicateException(String.format("Duplicate method/URL: %s/%s", httpMethod.name(), url));
		}

		map.put(url, action);
	}

	/**
	 * @return the action associated with given request and respective URL
	 * parameter map. If there is no mapping, returns null
	 * @param req HTTP request
	 */
	public static AbstractWebAction getAction(HttpServletRequest req) {
		HttpMethod httpMethod;
		try {
			httpMethod = HttpMethod.valueOf(req.getMethod());
		} catch (IllegalArgumentException ex) {
			httpMethod = null;
		}
		
		String path = req.getPathInfo();

		Map<String, AbstractWebAction> map = ACTION_MAP.get(httpMethod);
		if (map == null) {
			return null;
		} else {
			return map.get(path);
		}
	}
	// =========================================================================	

	// INSTANCE SCOPE ==========================================================
	protected void onActionNotFound(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	/** 
	 * Called when there is a {@linkplain WebSecurityException} during servlet processing.
	 * Default implementation just sends a HTTP 403 status in the response
	 * @param ex exception
	 * @param req HTTP request
	 * @param resp HTTP response
	 */
	protected void onSecurityException(HttpServletRequest req, HttpServletResponse resp, WebSecurityException ex) throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_FORBIDDEN);
	}
	
	@Override
	protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		AbstractWebAction action = getAction(req);
		
		if (action == null) {
			onActionNotFound(req, resp);
		} else {
			try {
				action.execute(req, resp);
			} catch(WebSecurityException ex) {
				onSecurityException(req, resp, ex);
			}
		}
	}
	// =========================================================================	
}
