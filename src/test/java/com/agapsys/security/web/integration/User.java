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

import com.agapsys.security.AbstractUser;
import com.agapsys.security.DuplicateException;
import com.agapsys.security.Role;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class User extends AbstractUser implements Serializable {
	// CLASS SCOPE =============================================================
	private static final Map<String, User> APP_USERS = new LinkedHashMap<>();
	
	public static User addUser(String username, String password, Role... roles) throws IllegalArgumentException, DuplicateException {
		if (username == null || username.isEmpty())
			throw new IllegalArgumentException("Null/Empty username");
		
		if (password == null || password.isEmpty())
			throw new IllegalArgumentException("Null/Empty password");
		
		if (username.contains("<"))
			throw new IllegalArgumentException("Invalid username: " + username);
		
		if (APP_USERS.containsKey(username))
			throw new IllegalArgumentException(String.format("An user with the same username (%s) is already registered", username));
		
		User user = new User(username, password, roles);
		APP_USERS.put(username, user);
		return user;
	}
	
	public static User getUser(String username, String password) throws IllegalArgumentException {
		if (username == null || username.isEmpty())
			throw new IllegalArgumentException("Null/Empty username");
		
		User user = APP_USERS.get(username);
		if (user != null) {
			if (!user.password.equals(password)) {
				user = null;
			}
		}
		return user;
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final String password;
	private final String username;
	
	private User(String username, String password, Role... roles) throws IllegalArgumentException, DuplicateException {
		super(roles);
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	// =========================================================================

}
