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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global attribute service with thread-safe access.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class AttributeService {
	// CLASS SCOPE =============================================================
	private static final AttributeService SINGLETON = new AttributeService();
	public static AttributeService getInstance() {
		return SINGLETON;
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private AttributeService() {}
	
	private final Map<Thread, Map<String, Object>> threadMap = new ConcurrentHashMap<>();

	private Map<String, Object> getAttributeMap() {
		Thread currentThread = Thread.currentThread();
		Map<String, Object> attributeMap = threadMap.get(currentThread);
		if (attributeMap == null) {
			attributeMap = new LinkedHashMap<>();
			threadMap.put(currentThread, attributeMap);
		}

		return attributeMap;
	}

	public Object getAttribute(String name) {
		if (name == null || name.trim().isEmpty())
			throw new IllegalArgumentException("Null/Empty name");

		return getAttributeMap().get(name);
	}
	public void setAttribute(String name, Object attribute) {
		if (name == null || name.trim().isEmpty())
			throw new IllegalArgumentException("Null/Empty name");

		getAttributeMap().put(name, attribute);
	}

	public void destroyAttribute(String name) {
		if (name == null || name.trim().isEmpty())
			throw new IllegalArgumentException("Null/Empty name");

		Map<String, Object> attributeMap = threadMap.get(Thread.currentThread());
		if (attributeMap != null)
			attributeMap.remove(name);
	}

	public void destroyAttributes() {
		threadMap.remove(Thread.currentThread());
	}
	// =========================================================================
}
