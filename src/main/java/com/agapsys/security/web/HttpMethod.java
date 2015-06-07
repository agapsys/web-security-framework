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

/** Represents a HTTP method. */
public enum HttpMethod {
	GET,
	HEAD,
	OPTIONS,
	POST,
	PUT,
	TRACE;
	
	static HttpMethod fromString(String method) {
		switch (method) {
			case "GET":
				return GET;
				
			case "HEAD":
				return HEAD;
				
			case "OPTIONS":
				return OPTIONS;
				
			case "POST":
				return POST;
				
			case "PUT":
				return PUT;
				
			case "TRACE":
				return TRACE;
				
			default:
				throw new IllegalArgumentException(String.format("Unsupported method: %s", method));
		}
	}
}
