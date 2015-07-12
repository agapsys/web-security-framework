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
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractWebAction extends AbstractAction {
	// CLASS SCOPE =============================================================
	private static class InternalWebSecurityException extends RuntimeException {
		public InternalWebSecurityException(HttpServletRequest request, HttpServletResponse response, Object[] params, String message) {}
	}
	
	private static class InternalMethodNotAllowedException extends RuntimeException {
		public InternalMethodNotAllowedException(HttpServletRequest request, HttpServletResponse response, Object[] params, String message) {}
	}
	
	public static final String CSRF_HEADER  = "X-Csrf-Token";
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private HttpMethod requiredMethod = null;
	
	private void setMethod(HttpMethod method) throws IllegalArgumentException {
		if (method == null)
			throw new IllegalArgumentException("Null method");
		
		this.requiredMethod = method;
	}
	
	// Constructors ------------------------------------------------------------	
	/** Creates an action with public access and accepts any HTTP method. */
	public AbstractWebAction() {
		super();
	}
	
	/** 
	 * Creates an action that accepts only given HTTP method.
	 * @param requiredMethod accepted HTTP method
	 * @throws IllegalArgumentException if acceptedMethod == null
	 */
	public AbstractWebAction(HttpMethod requiredMethod) throws IllegalArgumentException {
		super();
		setMethod(requiredMethod);
	}

	/**
	 * Creates an action the are restricted to users with given roles (all HTTP methods are accepted)
	 * @param requiredRoles required roles for execution.
	 * @throws IllegalArgumentException if any of given roles is null
	 * @throws DuplicateException if there is an attempt to register the same role more than once (either directly of as a child of any associated role).
	 */
	public AbstractWebAction(Role...requiredRoles) throws IllegalArgumentException, DuplicateException {
		super(requiredRoles);
	}
	
	/**
	 * Creates an action the are restricted to users with given roles and accepts only given HTTP method
	 * @param requiredRoles required roles for execution.
	 * @param requiredMethod accepted HTTP method
	 * @throws IllegalArgumentException if any of given roles is null
	 * @throws DuplicateException if there is an attempt to register the same role more than once (either directly of as a child of any associated role).
	 */
	public AbstractWebAction(HttpMethod requiredMethod, Role...requiredRoles) throws IllegalArgumentException, DuplicateException {
		super(requiredRoles);
		setMethod(requiredMethod);
	}
	
	
	/**
	 * Creates an action the are restricted to users with given roles (all HTTP methods are accepted)
	 * @param requiredRoleNames required roles for execution.
	 * @throws IllegalArgumentException if any of given roles is null
	 * @throws RoleNotFoundException if any of given roleNames is not registered in {@linkplain RoleRepository}
	 * @throws DuplicateException if there is an attempt to register the same role more than once (either directly of as a child of any associated role).
	 */
	public AbstractWebAction(String...requiredRoleNames) throws IllegalArgumentException, DuplicateException, RoleNotFoundException {
		super(requiredRoleNames);
	}
	
	/**
	 * Creates an action the are restricted to users with given roles and accepts only given HTTP method
	 * @param requiredRoleNames required roles for execution.
	 * @param requiredMethod accepted HTTP method
	 * @throws IllegalArgumentException if any of given roles is null
	 * @throws RoleNotFoundException if any of given roleNames is not registered in {@linkplain RoleRepository}
	 * @throws DuplicateException if there is an attempt to register the same role more than once (either directly of as a child of any associated role).
	 */
	public AbstractWebAction(HttpMethod requiredMethod, String...requiredRoleNames) throws IllegalArgumentException, DuplicateException, RoleNotFoundException{
		super(requiredRoleNames);
		setMethod(requiredMethod);
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
		
		HttpMethod reqMethod = Util.getMethod(request);
		if (requiredMethod != null && requiredMethod != reqMethod)
			throw new InternalMethodNotAllowedException(request, response, params, "Method not allowed: " + reqMethod.name());
		
		if (!passCsrfTest(request)){
			throw new InternalWebSecurityException(request, response, params, "Invalid CSRF token");
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

	protected void postRun(HttpServletRequest request, HttpServletResponse response, Object...params) {}

	
	public HttpMethod getRequiredMethod() {
		return requiredMethod;
	}
	
	public void setRequiredMethod(HttpMethod requiredMethod) throws IllegalArgumentException {
		setMethod(requiredMethod);
	}

	
	public final void execute(HttpServletRequest request, HttpServletResponse response, Object...params) throws MethodNotAllowedException, WebSecurityException  {
		User user = getUser(request);
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
