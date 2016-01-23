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

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebApplicationFilter implements Filter {
	// CLASS SCOPE =============================================================
	public static final String ATTR_HTTP_REQUEST  = WebApplicationFilter.class.getName() + ".httpRequest";
	public static final String ATTR_HTTP_RESPONSE = WebApplicationFilter.class.getName() + ".httpResponse";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final AttributeService attributeService = AttributeService.getInstance();
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		
		attributeService.setAttribute(ATTR_HTTP_REQUEST, req);
		attributeService.setAttribute(ATTR_HTTP_RESPONSE, resp);
		
		try {
			chain.doFilter(request, response);
		} finally {
			attributeService.destroyAttributes();
		}
	}

	@Override
	public void destroy() {}
	// =========================================================================
}
