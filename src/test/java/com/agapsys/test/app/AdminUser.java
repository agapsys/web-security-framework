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
package com.agapsys.test.app;

import com.agapsys.security.web.User;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class AdminUser implements User {
	private static final Set<String> ROLES = Collections.unmodifiableSet(new LinkedHashSet<String>());
	
	@Override
	public Set<String> getRoles() {
		return ROLES;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}
}
