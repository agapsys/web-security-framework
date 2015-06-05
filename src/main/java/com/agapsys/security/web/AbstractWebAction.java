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
import com.agapsys.security.AbstractAction;
import com.agapsys.security.DuplicateException;
import com.agapsys.security.User;
import com.agapsys.security.SecurityException;
import java.util.Objects;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public abstract class AbstractWebAction extends AbstractAction {
	// CLASS SCOPE =============================================================
	private static class InternalWebSecurityException extends RuntimeException {
		public InternalWebSecurityException(HttpServletRequest request, HttpServletResponse response, Object[] params, String message) {}
	}
	
	private static class InternalMethodNotAllowedException extends RuntimeException {
		public InternalMethodNotAllowedException(HttpServletRequest request, HttpServletResponse response, Object[] params, String message) {}
	}
	
	private static final int   DEFAULT_XSRF_TOKEN_LENGTH      = 128;
	
	public static final String XSRF_HEADER            = "X-Csrf-Token";
	public static final String SESSION_ATTR_USER       = "com.agapsys.security.web.user";
	public static final String SESSION_ATTR_XSRF_TOKEN = "com.agapsys.security.web.xsrf";
	
	/** 
	 * Generates a random string (chars: [a-z][A-Z][0-9]).
	 * @param length length of returned string
	 * @return a random string with given length.
	 * @throws IllegalArgumentException if (length &lt; 1)
	 */
	private static String getRandomString(int length) throws IllegalArgumentException {
		char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		return getRandomString(length, chars);
	}
	
	/**
	 * Generates a random String 
	 * @param length length of returned string
	 * @param chars set of chars which will be using during random string generation
	 * @return a random string with given length.
	 * @throws IllegalArgumentException if (length &lt; 1 || chars == null || chars.length == 0)
	 */
	private static String getRandomString(int length, char[] chars) throws IllegalArgumentException {
		if (length < 1)
			throw new IllegalArgumentException("Invalid length: " + length);
		
		if (chars == null || chars.length == 0)
			throw new IllegalArgumentException("Null/Empty chars");
		
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}
	
	
	protected static Object getSessionAttribute(HttpServletRequest request, String attribute) {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		HttpSession session = request.getSession();
		return session.getAttribute(attribute);
	}
	
	protected static void setSessionAttribute(HttpServletRequest request, String name, String value) {
		HttpSession session = request.getSession();
		session.setAttribute(name, value);
	}
	
	protected static void invalidateSession(HttpServletRequest request) {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		request.getSession().invalidate();
	}

	
	protected static User getSessionUser(HttpServletRequest request) {
		return (User) getSessionAttribute(request, SESSION_ATTR_USER);
	}
	
	
	protected static String getSessionXsrfToken(HttpServletRequest request) {
		return (String) getSessionAttribute(request, SESSION_ATTR_XSRF_TOKEN);
	}
	
	
	protected static HttpMethod getMethod(HttpServletRequest request) {
		return HttpMethod.fromString(request.getMethod());
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private HttpMethod acceptedMethod = null;
	
	private void setMethod(HttpMethod method) throws IllegalArgumentException {
		if (method == null)
			throw new IllegalArgumentException("Null method");
		
		this.acceptedMethod = method;
	}
			
	public AbstractWebAction() {
		super();
	}
	
	public AbstractWebAction(HttpMethod acceptedMethod) throws IllegalArgumentException {
		super();
		setMethod(acceptedMethod);
	}

	public AbstractWebAction(Role...requiredRoles) throws IllegalArgumentException, DuplicateException {
		super(requiredRoles);
	}
		
	public AbstractWebAction(HttpMethod acceptedMethod, Role...requiredRoles) throws IllegalArgumentException {
		super(requiredRoles);
		setMethod(acceptedMethod);
	}
	
	public AbstractWebAction(String...requiredRoles) throws IllegalArgumentException, DuplicateException {
		super(requiredRoles);
	}
	
	public AbstractWebAction(HttpMethod acceptedMethod, String...requiredRoles) throws IllegalArgumentException, DuplicateException {
		super(requiredRoles);
		setMethod(acceptedMethod);
	}
	
	@Override
	protected final void preRun(User user, Object...params) {
		super.preRun(user, params);
		
		HttpServletRequest request;
		HttpServletResponse response;
		
		try {
			request = (HttpServletRequest) params[0];
			if (request == null)
				throw new IllegalArgumentException();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Missing request parameter at index 0");
		}
		
		try {
			response = (HttpServletResponse) params[1];
			if (response == null)
				throw new IllegalArgumentException();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Missing request parameter at index 1");
		}
		
		Object[] extraParams = params.length >= 2 ? (Object[])params[2] : new Object[0];
		
		HttpMethod reqMethod = getMethod(request);
		if (acceptedMethod != null && acceptedMethod != reqMethod)
			throw new InternalMethodNotAllowedException(request, response, params, "Method not allowed: " + reqMethod.name());
		
		String requestXsrfToken = request.getHeader(XSRF_HEADER);
		String sessionXsrfToken = getSessionXsrfToken(request);

		if (!Objects.equals(requestXsrfToken, sessionXsrfToken) && !getRequiredRoles().isEmpty()) {
			throw new InternalWebSecurityException(request, response, params, "Invalid XSRF token");
		} else  {
			preRun(request, response, extraParams);
		}
	}
	
	protected void preRun(HttpServletRequest request, HttpServletResponse response, Object...params) {}
	
	
	@Override
	protected final void run(User user, Object...params) {		
		run((HttpServletRequest) params[0], (HttpServletResponse) params[1], (Object[]) params[2]);
	}
	
	protected abstract void run(HttpServletRequest request, HttpServletResponse response, Object...params);

	
	@Override
	protected final void postRun(User user, Object...params) {
		super.postRun(user, params);
		postRun((HttpServletRequest) params[0], (HttpServletResponse) params[1], (Object[]) params[2]);
	}

	protected void postRun(HttpServletRequest request, Object...params) {}

	
	public HttpMethod getAcceptedMethod() {
		return acceptedMethod;
	}
	
	public void setAcceptedMethod(HttpMethod method) throws IllegalArgumentException {
		setMethod(method);
	}
	
	protected int getXsrfTokenLength() {
		return DEFAULT_XSRF_TOKEN_LENGTH;
	}
	
	protected void registerSessionUser(HttpServletRequest request, HttpServletResponse response, User user) {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		if (user == null)
			throw new IllegalArgumentException("Null user");

		// Registers user in session...
		HttpSession session = request.getSession();
		session.setAttribute(SESSION_ATTR_USER, user);
		
		// Assigns a XSRF token for this user
		String xsrfToken = getRandomString(getXsrfTokenLength());
		session.setAttribute(SESSION_ATTR_XSRF_TOKEN, xsrfToken);
		response.setHeader(XSRF_HEADER, xsrfToken);
	}
	

	public final void execute(HttpServletRequest request, HttpServletResponse response, Object...params) throws MethodNotAllowedException, WebSecurityException  {
		User user = getSessionUser(request);
		try {
			super.execute(user, request, response, params);
		} catch (InternalMethodNotAllowedException ex) {
			throw new MethodNotAllowedException(request, response, params, ex.getMessage());
		} catch (SecurityException | InternalWebSecurityException ex) {
			throw new WebSecurityException(request, response, params, ex.getMessage());
		}
	}
	// =========================================================================
}
