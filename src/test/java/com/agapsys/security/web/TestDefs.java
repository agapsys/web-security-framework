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
import com.agapsys.security.RoleRepository;

public class TestDefs {
	// CLASS SCOPE =============================================================
	public static final Role SECURED_ROLE;
	public static final Role EXTRA_SECURED_ROLE;
	
	static {
		RoleRepository roles = RoleRepository.getSingletonInstance();
		SECURED_ROLE = roles.createRole("com.agapsys.security.web.SECURED_ROLE");
		EXTRA_SECURED_ROLE = roles.createRole("com.agapsys.security.web.EXTRA_SECURED_ROLE");
		EXTRA_SECURED_ROLE.addChild(SECURED_ROLE);
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private TestDefs() {}
	// =========================================================================
}
