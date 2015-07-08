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
	 * @throws IllegalArgumentException if request == null or (attributeName == null || attributeName.isEmpty())
	 */
	public static Object getSessionAttribute(HttpServletRequest request, String attributeName) throws IllegalArgumentException {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		if (attributeName == null || attributeName.isEmpty())
			throw new IllegalArgumentException("Null/Empty attributeName");
		
		HttpSession session = request.getSession();
		return session.getAttribute(attributeName);
	}
	
	/**
	 * Sets an attribute into request session
	 * @param request HTTP request
	 * @param attributeName attribute name
	 * @param obj attribute value
	 * @throws IllegalArgumentException if request == null or (attributeName == null || attributeName.isEmpty())
	 */
	public static void setSessionAttribute(HttpServletRequest request, String attributeName, Object obj) throws IllegalArgumentException {
		if (request == null)
			throw new IllegalArgumentException("Null request");
		
		if (attributeName == null || attributeName.isEmpty())
			throw new IllegalArgumentException("Null/Empty attributeName");
		
		HttpSession session = request.getSession();
		session.setAttribute(attributeName, obj);
	}
	
	/**
	 * Remove a session attribute
	 * @param request HTTP request
	 * @param attributeName attribute name
	 * @throws IllegalArgumentException if request == null or (attributeName == null || attributeName.isEmpty())
	 */
	public static void removeSessionAttribute(HttpServletRequest request, String attributeName) throws IllegalArgumentException {
		if (request == null)
			throw new IllegalArgumentException("Null request");
		
		if (attributeName == null || attributeName.isEmpty())
			throw new IllegalArgumentException("Null/Empty attributeName");
		
		HttpSession session = request.getSession();
		session.removeAttribute(attributeName);
	}
	
	/** 
	 * Invalidates request session.
	 * @param request HTTP request
	 * @throws IllegalArgumentException if request == null
	 */
	public static void invalidateSession(HttpServletRequest request) throws IllegalArgumentException {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		request.getSession().invalidate();
	}
	
	
	/**
	 * @param request HTTP request
	 * @return Associated user registered into request session. If there is no registered user, returns null instead.
	 * @throws IllegalArgumentException if request == null
	 */
	public static User getSessionUser(HttpServletRequest request) throws IllegalArgumentException {
		if (request == null)
			throw new IllegalArgumentException("Null request");
		
		return (User) getSessionAttribute(request, SESSION_ATTR_USER);
	}
	
	/**
	 * Assigns a user to request session and returns associated CSRF token
	 * @param request HTTP request
	 * @param user user to be registered
	 * @throws IllegalArgumentException if request == null or user == null
	 */
	public static void registerSessionUser(HttpServletRequest request, User user) throws IllegalArgumentException {
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
	 * @throws IllegalArgumentException if request == null
	 */
	public static String getSessionCsrfToken(HttpServletRequest request) throws IllegalArgumentException {
		return (String) getSessionAttribute(request, SESSION_ATTR_CSRF_TOKEN);
	}
	
	/**
	 * Generates a CSRF token and assigns it to request session.
	 * @param request HTTP request
	 * @return generated token
	 * @throws IllegalArgumentException if request == null
	 */
	public static String generateSessionCsrfToken(HttpServletRequest request) throws IllegalArgumentException{
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
