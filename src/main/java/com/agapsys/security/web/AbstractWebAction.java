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
import com.agapsys.security.RoleNotFoundException;
import com.agapsys.security.RoleRepository;
import com.agapsys.security.User;
import com.agapsys.security.SecurityException;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractWebAction extends AbstractAction {
	// CLASS SCOPE =============================================================
	private static class InternalWebSecurityException extends RuntimeException {
		public InternalWebSecurityException(HttpServletRequest request, HttpServletResponse response, Object[] params, String message) {}
	}
	
	private static class InternalIOException extends RuntimeException {
		public InternalIOException(IOException cause) {
			super(cause);
		}
	}
	
	public static final String CSRF_HEADER  = "X-Csrf-Token";
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================	
	// Constructors ------------------------------------------------------------	
	/** Creates an action with public access. */
	public AbstractWebAction() {
		super();
	}

	/**
	 * Creates an action the are restricted to users with given roles
	 * @param requiredRoles required roles for execution.
	 * @throws IllegalArgumentException if any of given roles is null
	 * @throws DuplicateException if there is an attempt to register the same role more than once (either directly of as a child of any associated role).
	 */
	public AbstractWebAction(Role...requiredRoles) throws IllegalArgumentException, DuplicateException {
		super(requiredRoles);
	}	
	
	/**
	 * Creates an action the are restricted to users with given roles
	 * @param requiredRoleNames required roles for execution.
	 * @throws IllegalArgumentException if any of given roles is null
	 * @throws RoleNotFoundException if any of given roleNames is not registered in {@linkplain RoleRepository}
	 * @throws DuplicateException if there is an attempt to register the same role more than once (either directly of as a child of any associated role).
	 */
	public AbstractWebAction(String...requiredRoleNames) throws IllegalArgumentException, DuplicateException, RoleNotFoundException {
		super(requiredRoleNames);
	}
	// -------------------------------------------------------------------------
	
	/**
	 * @param request HTTP request
	 * @return The user associated to given request.
	 * Default implementation returns {@linkplain Session#getSessionUser(HttpServletRequest)}.
	 */
	protected User getUser(HttpServletRequest request) {
		return Session.getSessionUser(request);
	}
	
	/**
	 * Performs a CSRF test in given request
	 * @param request HTTP request
	 * @return a boolean indicating if test passed
	 */
	protected boolean passCsrfTest(HttpServletRequest request) {
		String requestCsrfToken = request.getHeader(CSRF_HEADER);
		String sessionCsrfToken = Session.getSessionCsrfToken(request);

		return (getRequiredRoles().isEmpty() || Objects.equals(requestCsrfToken, sessionCsrfToken));
	}
	
	/** 
	 * Sends CSRF token to user.
	 * Default implementation send it as a header ({@link AbstractWebAction#CSRF_HEADER})
	 * @param request HTTP request
	 * @param response HTTP response
	 * @param csrfToken token to be sent
	 */
	protected void sendCsrfToken(HttpServletRequest request, HttpServletResponse response, String csrfToken) {
		response.setHeader(CSRF_HEADER, csrfToken);
	}
	
	/**
	 * Registers an user in the session related to given request.
	 * Default implementation registers a user in the session using {@linkplain Session#registerSessionUser(javax.servlet.http.HttpServletRequest, com.agapsys.security.User)},
	 * registers a CSRF token in the session using {@link Session#generateSessionCsrfToken(HttpServletRequest)} and
	 * sends it via {@link AbstractWebAction#sendCsrfToken(HttpServletRequest, HttpServletResponse, String)}.
	 * @param request HTTP request
	 * @param response HTTP response
	 * @param user user to be registered
	 */
	protected void registerSessionUser(HttpServletRequest request, HttpServletResponse response, User user) {
		Session.registerSessionUser(request, user);
		String csrfToken = Session.generateSessionCsrfToken(request);
		sendCsrfToken(request, response, csrfToken);
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
		
		if (!passCsrfTest(request)){
			throw new InternalWebSecurityException(request, response, params, "Invalid CSRF token");
		} else  {
			preRun(request, response, extraParams);
		}
	}
	
	protected void preRun(HttpServletRequest request, HttpServletResponse response, Object...params) {}
	
	
	@Override
	protected final void run(User user, Object...params) {
		HttpServletRequest  req  = (HttpServletRequest) params[0];
		HttpServletResponse resp = (HttpServletResponse) params[1];
		params = (Object[]) params[2];
		
		try {
			run(req, resp, params);
		} catch (WebSecurityException ex) {
			throw new InternalWebSecurityException(req, resp, params, ex.getMessage());
		} catch (IOException ex) {
			throw new InternalIOException(ex);
		}
	}
	
	protected abstract void run(HttpServletRequest request, HttpServletResponse response, Object...params) throws WebSecurityException, IOException;

	
	@Override
	protected final void postRun(User user, Object...params) {
		super.postRun(user, params);
		postRun((HttpServletRequest) params[0], (HttpServletResponse) params[1], (Object[]) params[2]);
	}

	protected void postRun(HttpServletRequest request, HttpServletResponse response, Object...params) {}
		
	public final void execute(HttpServletRequest request, HttpServletResponse response, Object...params) throws WebSecurityException, IOException {
		User user = getUser(request);
		try {
			super.execute(user, request, response, params);
		} catch (SecurityException | InternalWebSecurityException ex) {
			throw new WebSecurityException(request, response, params, ex.getMessage());
		} catch (InternalIOException ex) {
			throw (IOException) ex.getCause();
		}
	}
	// =========================================================================
}
