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
package com.agapsys.test;

import com.agapsys.http.HttpResponse;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class TestUtils {	
	protected TestUtils() {}
	
	public static void assertStatus(int expected, HttpResponse.StringResponse resp) {
		if (resp.getStatusCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR && expected != HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
			System.out.println(resp.getContentString());
		}
		
		Assert.assertEquals(expected, resp.getStatusCode());
	}
	
	public static void assertStringResponse(int expectedStatus, String expectedContent, HttpResponse.StringResponse resp) {
		assertStatus(expectedStatus, resp);
		Assert.assertEquals(expectedContent, resp.getContentString());
	}
}
