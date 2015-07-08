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
	private static final int   DEFAULT_CSRF_TOKEN_LENGTH      = 128;

	public static final String SESSION_ATTR_USER       = "com.agapsys.security.web.user";
	public static final String SESSION_ATTR_CSRF_TOKEN = "com.agapsys.security.web.csrf";
	
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
	 * @param obj attribute value
	 */
	public static void setSessionAttribute(HttpServletRequest request, String name, Object obj) {
		HttpSession session = request.getSession();
		session.setAttribute(name, obj);
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
	 * Assigns a user to request session and returns associated CSRF token
	 * @param request HTTP request
	 * @param response
	 * @param user 
	 */
	public static void registerSessionUser(HttpServletRequest request, HttpServletResponse response, User user) {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		if (user == null)
			throw new IllegalArgumentException("Null user");

		// Registers user in session...
		setSessionAttribute(request, SESSION_ATTR_USER, user);
	}
	
	/**
	 * @return CSRF (Cross-Site Request Forgery) token associated to request session
	 * @param request HTTP request
	 */
	public static String getSessionCsrfToken(HttpServletRequest request) {
		return (String) getSessionAttribute(request, SESSION_ATTR_CSRF_TOKEN);
	}
	
	/**
	 * Generates a CSRF token and assigns it to request session.
	 * @param request HTTP request
	 * @return generated token
	 */
	public static String generateSessionCsrfToken(HttpServletRequest request) {
		// Assigns a CSRF token for this user
		String csrfToken = Util.getRandomString(DEFAULT_CSRF_TOKEN_LENGTH);
		setSessionAttribute(request, SESSION_ATTR_CSRF_TOKEN, csrfToken);
		return csrfToken;
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private Session() {} // private scope prevents external instantiation
	// =========================================================================
}
