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

import com.agapsys.security.Security;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class WebSecurity extends Security {
	
	// CLASS SCOPE =============================================================
	public static void init(WebSecurityManager securityManager) throws IllegalStateException {
		Security.init(securityManager);
	}
	
	public static void init(ClassLoader classLoader, WebSecurityManager securityManager) {
		Security.init(classLoader, securityManager);
	}
	
	public static void init(WebSecurityManager securityManager, String... securedClasses) {
		Security.init(securityManager, securedClasses);
	}
	
	public static void init(ClassLoader classLoader, WebSecurityManager securityManager, String... securedClasses) {
		Security.init(classLoader, securityManager, securedClasses);
	}
	
	public static WebSecurityManager getSecurityManager() {
		return (WebSecurityManager) Security.getSecurityManager();
	}
	
	public static User getCurrentUser() {
		WebSecurityManager securityManager = getSecurityManager();
		if (securityManager != null)
			return securityManager.getCurrentUser();
		
		return null;
	}
	
	public static void setCurrentUser(User user) {
		WebSecurityManager securityManager = getSecurityManager();
		
		if (securityManager == null)
			throw new RuntimeException("There is no security manager");
		
		securityManager.setCurrentUser(user);
	}
	
	public static void unregisterCurrentUser() {
		WebSecurityManager securityManager = getSecurityManager();

		if (securityManager == null)
			throw new RuntimeException("There is no security manager");
		
		securityManager.unregisterCurrentUser();
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	protected WebSecurity() {}
	// =========================================================================
}
