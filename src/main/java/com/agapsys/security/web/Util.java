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

import java.util.Random;
import javax.servlet.http.HttpServletRequest;

/** General utilities */
class Util {
	// CLASS SCOPE =============================================================
	/** 
	 * Generates a random string (chars: [a-z][A-Z][0-9]).
	 * @param length length of returned string
	 * @return a random string with given length.
	 * @throws IllegalArgumentException if (length &lt; 1)
	 */
	public static String getRandomString(int length) throws IllegalArgumentException {
		char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		return getRandomString(length, chars);
	}
	
	/**
	 * Generates a random String 
	 * @param length length of returned string
	 * @param chars set of chars which will be using during random string generation
	 * @return a random string with given length.
	 * @throws IllegalArgumentException if (length &lt; 1 || chars == null || chars.length == 0)
	 */
	public static String getRandomString(int length, char[] chars) throws IllegalArgumentException {
		if (length < 1)
			throw new IllegalArgumentException("Invalid length: " + length);
		
		if (chars == null || chars.length == 0)
			throw new IllegalArgumentException("Null/Empty chars");
		
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}
	
	/** 
	 * @return HttpMethod used by given request
	 * @param request HTTP request
	 */
	public static HttpMethod getMethod(HttpServletRequest request) {
		return HttpMethod.fromString(request.getMethod());
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private Util() {} // private scope prevents external instantiation
	// =========================================================================
}
