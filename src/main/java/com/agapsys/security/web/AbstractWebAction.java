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
	private static class InternalSecurityException extends RuntimeException {
		public InternalSecurityException(HttpServletRequest request, HttpServletResponse response, Object[] params, String message) {}
	}

	private static final int   XSRF_TOKEN_LENGTH      = 16;
	public static final String XSRF_HEADER            = "X-Csrf-Token";
	public static final String SESSION_ATTR_USER       = "com.agapsys.security.web.user";
	public static final String SESSION_ATTR_XSRF_TOKEN = "com.agapsys.security.web.xsrf";

	
	/** Returns a random string with given length (chars: [a-z][A-Z][0-9]). */
	private static String getRandomString(int length) {
		char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		return getRandomString(length, chars);
	}
	
	/** Returns an random string with given length. */
	private static String getRandomString(int length, char[] chars) {
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}
	
	
	public static void registerSessionUser(HttpServletRequest request, HttpServletResponse response, User user) {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		if (user == null)
			throw new IllegalArgumentException("Null user");

		// Registers user in session...
		HttpSession session = request.getSession();
		session.setAttribute(SESSION_ATTR_USER, user);
		
		// Assigns a XSRF token for this user
		String xsrfToken = getRandomString(XSRF_TOKEN_LENGTH);
		session.setAttribute(SESSION_ATTR_XSRF_TOKEN, xsrfToken);
		response.setHeader(AbstractWebAction.XSRF_HEADER, xsrfToken);
	}
	
	public static void removeSessionUser(HttpServletRequest request) {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		request.getSession().invalidate();
	}
	
	
	private static Object getSessionAttribute(HttpServletRequest request, String attribute) {
		if (request == null)
			throw new IllegalArgumentException("Null request");

		HttpSession session = request.getSession();
		return session.getAttribute(attribute);
	}
	
	private static User getSessionUser(HttpServletRequest request) {
		return (User) getSessionAttribute(request, SESSION_ATTR_USER);
	}
	
	private static String getSessionXsrfToken(HttpServletRequest request) {
		return (String) getSessionAttribute(request, SESSION_ATTR_XSRF_TOKEN);
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	public AbstractWebAction() {
		super();
	}
	
	public AbstractWebAction(Role...requiredRoles) throws IllegalArgumentException, DuplicateException {
		super(requiredRoles);
	}
	
	public AbstractWebAction(String...requiredRoles) throws IllegalArgumentException, DuplicateException {
		super(requiredRoles);
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
					
		String requestXsrfToken = request.getHeader(XSRF_HEADER);
		String sessionXsrfToken = getSessionXsrfToken(request);

		if (!Objects.equals(requestXsrfToken, sessionXsrfToken)) {
			throw new InternalSecurityException(request, response, params, "Invalid XSRF token");
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
	

	public final void execute(HttpServletRequest request, HttpServletResponse response, Object...params) throws WebSecurityException {
		User user = getSessionUser(request);
		try {
			super.execute(user, request, response, params);
		} catch (SecurityException | InternalSecurityException ex) {
			throw new WebSecurityException(request, response, params, ex.getMessage());
		}
	}
	// =========================================================================
}
