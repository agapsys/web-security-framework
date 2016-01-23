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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class AbstractWebSecurityManager implements com.agapsys.security.SecurityManager {
	
	private final AttributeService attributeService = AttributeService.getInstance();
	
	public final User getUser() {
		return getUser(getRequest());
	}

	protected abstract User getUser(HttpServletRequest request);
	
	protected boolean isAllowed(HttpServletRequest request) {
		return true;
	}
	
	protected final HttpServletRequest getRequest() {
		return (HttpServletRequest) attributeService.getAttribute(WebApplicationFilter.ATTR_HTTP_REQUEST);
	}
	
	protected final HttpServletResponse getResponse() {
		return (HttpServletResponse) attributeService.getAttribute(WebApplicationFilter.ATTR_HTTP_RESPONSE);
	}
	
	@Override
	public final boolean isAllowed(String[] requiredRoles) {
		
		if (!isAllowed(getRequest()))
			return false;
			
		for (String requiredRole : requiredRoles) {
			if (!getUser().getRoles().contains(requiredRole))
				return false;
		}
		
		return true;
	}

	@Override
	public final void onNotAllowed() {
		throw new NotAllowedException(getUser());
	}	
}
