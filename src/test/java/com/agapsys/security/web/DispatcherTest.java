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

import com.agapsys.security.DuplicateException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;

public class DispatcherTest {
	
	@Test(expected = IllegalArgumentException.class)
	public void testPassNullAction() {
		ActionDispatcherServlet.registerAction(null, HttpMethod.POST, "/test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPassNullMethod() {
		AbstractWebAction action = new AbstractWebAction() {

			@Override
			public void run(HttpServletRequest req, HttpServletResponse resp, Object... params) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
		ActionDispatcherServlet.registerAction(action, null, "/test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPassNullUrl() {
		AbstractWebAction action = new AbstractWebAction() {

			@Override
			public void run(HttpServletRequest req, HttpServletResponse resp, Object... params) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
		ActionDispatcherServlet.registerAction(action, HttpMethod.POST, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPassEmptyUrl() {
		AbstractWebAction action = new AbstractWebAction() {

			@Override
			public void run(HttpServletRequest req, HttpServletResponse resp, Object... params) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
		ActionDispatcherServlet.registerAction(action, HttpMethod.POST, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPassEmptyIfTrimmedUrl() {
		AbstractWebAction action = new AbstractWebAction() {

			@Override
			public void run(HttpServletRequest req, HttpServletResponse resp, Object... params) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
		ActionDispatcherServlet.registerAction(action, HttpMethod.POST, "  ");
	}

	@Test
	public void testSameUrlDistinctMethods() {
		AbstractWebAction action = new AbstractWebAction() {

			@Override
			public void run(HttpServletRequest req, HttpServletResponse resp, Object... params) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
		ActionDispatcherServlet.registerAction(action, HttpMethod.GET, "/test");
		ActionDispatcherServlet.registerAction(action, HttpMethod.POST, "/test");
	}
	
	@Test (expected = DuplicateException.class)
	public void testSameUrlSameMethod() {
		AbstractWebAction action = new AbstractWebAction() {

			@Override
			public void run(HttpServletRequest req, HttpServletResponse resp, Object... params) {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
		ActionDispatcherServlet.registerAction(action, HttpMethod.GET, "/test");
		ActionDispatcherServlet.registerAction(action, HttpMethod.GET, "/test");
	}
}
