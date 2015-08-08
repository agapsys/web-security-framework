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

package com.agapsys.security.web.integration;

import com.agapsys.security.web.AbstractWebAction;
import com.agapsys.security.web.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogoutAction extends AbstractWebAction {
	// CLASS SCOPE =============================================================
	private static LogoutAction singleton;
	
	public static LogoutAction getInstance() {
		if (singleton == null)
			singleton = new LogoutAction();
		
		return singleton;
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private LogoutAction() {
		super();
	}
	
	@Override
	protected void run(HttpServletRequest request, HttpServletResponse response, Object... params) {
		Session.invalidateSession(request);
	}
	// =========================================================================
}
