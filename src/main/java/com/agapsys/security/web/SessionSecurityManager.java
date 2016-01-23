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

import javax.servlet.http.HttpSession;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class SessionSecurityManager extends WebSecurityManager {
	// CLASS SCOPE =============================================================
	private static final String SESSION_ATTR_USER = SessionSecurityManager.class + ".user";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Override
	public User getCurrentUser() {
		HttpSession session = getRequest().getSession(false);

		if (session == null)
			return null;

		return (User) session.getAttribute(SESSION_ATTR_USER);
	}

	@Override
	public void setCurrentUser(User user) {
		if (user == null)
			throw new IllegalArgumentException("User cannot be null");
		
		HttpSession session = getRequest().getSession(true);
		
		if (session == null)
			throw new RuntimeException("Could not create a session");
		
		session.setAttribute(SESSION_ATTR_USER, user);
	}

	@Override
	public void unregisterCurrentUser() {
		HttpSession session = getRequest().getSession(false);
		
		if (session != null)
			session.removeAttribute(SESSION_ATTR_USER);
	}
	// =========================================================================

	

	
}
