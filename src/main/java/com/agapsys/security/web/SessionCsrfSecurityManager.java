/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
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

import java.util.Objects;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class SessionCsrfSecurityManager extends SessionSecurityManager {

	// CLASS SCOPE =============================================================
	private static final String SESSION_ATTR_CSRF_TOKEN = SessionCsrfSecurityManager.class.getName() + ".csrfToken";
	
	private static final int CSRF_TOKEN_LENGTH = 128;
	
	/** Name of the header used to send/retrieve a CSRF token. */
	public static final String CSRF_HEADER  = "X-Csrf-Token";

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
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Override
	protected boolean isAllowed(HttpServletRequest request) {
		HttpSession session = getRequest().getSession(false);
		
		if (session == null)
			return false;
		
		String sessionToken = (String) session.getAttribute(SESSION_ATTR_CSRF_TOKEN);
		String requestToken = request.getHeader(CSRF_HEADER);
			
		return Objects.equals(sessionToken, requestToken);
	}

	@Override
	public void setCurrentUser(User user) {
		super.setCurrentUser(user);
		
		HttpSession session = getRequest().getSession(true);
		
		String token = getRandomString(CSRF_TOKEN_LENGTH);
		session.setAttribute(SESSION_ATTR_CSRF_TOKEN, token);
		getResponse().setHeader(CSRF_HEADER, token);
	}

	@Override
	public void unregisterCurrentUser() {
		super.unregisterCurrentUser();
		
		HttpSession session = getRequest().getSession(false);
		
		if (session != null)
			session.removeAttribute(SESSION_ATTR_CSRF_TOKEN);
	}
	// =========================================================================
}
