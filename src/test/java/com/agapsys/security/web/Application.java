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

public class Application {
	// CLASS SCOPE =============================================================
	private static volatile boolean active = true;
	
	public static void setActive(boolean active) {
		Application.active = active;
	}
	
	public static boolean isActive() {
		return active;
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private Application() {}
	// =========================================================================
}
