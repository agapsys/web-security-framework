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

import com.agapsys.security.User;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Session utilities */
public class Session {
	// CLASS SCOPE =============================================================
	static final String XSRF_HEADER            = "X-Csrf-Token";
	
	private static final int    DEFAULT_XSRF_TOKEN_LENGTH      = 128;
	private static final String SESSION_ATTR_USER       = "com.agapsys.security.web.user";
	private static final String SESSION_ATTR_XSRF_TOKEN = "com.agapsys.security.web.xsrf";
	
	/** 
	 * @param request HTTP request
	 * @param attributeName desired attribute.
	 * @return An attribute from request session. If there is such attribute, returns null
	 */
	public static Object getSessionAttribute(HttpServletRequest request, String attributeName) {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		HttpSession session = request.getSession();
		return session.getAttribute(attributeName);
	}
	
	/**
	 * Sets an attribute into request session
	 * @param request HTTP request
	 * @param name attribute name
	 * @param value attribute value
	 */
	public static void setSessionAttribute(HttpServletRequest request, String name, String value) {
		HttpSession session = request.getSession();
		session.setAttribute(name, value);
	}
	
	/** 
	 * Invalidates request session.
	 * @param request HTTP request
	 */
	public static void invalidateSession(HttpServletRequest request) {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		request.getSession().invalidate();
	}
	
	
	/**
	 * @param request HTTP request
	 * @return Associated user registered into request session. If there is no registered user, returns null instead.
	 */
	public static User getSessionUser(HttpServletRequest request) {
		return (User) getSessionAttribute(request, SESSION_ATTR_USER);
	}
	
	/**
	 * Assigns a user to request session
	 * @param request HTTP request
	 * @param response
	 * @param user 
	 */
	public static void setSessionUser(HttpServletRequest request, HttpServletResponse response, User user) {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		if (user == null)
			throw new IllegalArgumentException("Null user");

		// Registers user in session...
		HttpSession session = request.getSession();
		session.setAttribute(SESSION_ATTR_USER, user);
		
		// Assigns a XSRF token for this user
		String xsrfToken = Util.getRandomString(DEFAULT_XSRF_TOKEN_LENGTH);
		session.setAttribute(SESSION_ATTR_XSRF_TOKEN, xsrfToken);
		response.setHeader(XSRF_HEADER, xsrfToken);
	}
	
	/**
	 * @return XSRF (Cross-Site Request Forgery) token associated to request session
	 * @param request HTTP request
	 */
	protected static String getSessionXsrfToken(HttpServletRequest request) {
		return (String) getSessionAttribute(request, SESSION_ATTR_XSRF_TOKEN);
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private Session() {} // private scope prevents external instantiation
	// =========================================================================
}
