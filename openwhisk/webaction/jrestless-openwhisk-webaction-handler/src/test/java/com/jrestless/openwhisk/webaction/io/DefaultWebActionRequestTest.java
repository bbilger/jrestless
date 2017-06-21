/*
 * Copyright 2017 Bjoern Bilger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jrestless.openwhisk.webaction.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.jrestless.test.CopyConstructorEqualsTester;

public class DefaultWebActionRequestTest {

	@Test
	public void testEquals() {
		new CopyConstructorEqualsTester(getConstructor())
			.addArguments(0, null, "httpMethod")
			.addArguments(1, null, ImmutableMap.of("headers", "headers"))
			.addArguments(2, null, "path")
			.addArguments(3, null, "user")
			.addArguments(4, null, "body")
			.addArguments(5, null, "query")
			.testEquals();
	}

	@Test
	public void testGetters() {
		Map<String, String> headers = ImmutableMap.of("headers", "headers");
		DefaultWebActionRequest request = new DefaultWebActionRequest(
				"httpMethod",
				headers,
				"path",
				"user",
				"body",
				"query");

		assertEquals("httpMethod", request.getHttpMethod());
		assertEquals(headers, request.getHeaders());
		assertEquals("path", request.getPath());
		assertEquals("user", request.getUser());
		assertEquals("body", request.getBody());
		assertEquals("query", request.getQuery());
	}

	@Test
	public void testNoArgsConsutructorCreatesEmptyObject() {
		DefaultWebActionRequest request = new DefaultWebActionRequest();
		assertNull(request.getHttpMethod());
		assertNull(request.getHeaders());
		assertNull(request.getPath());
		assertNull(request.getUser());
		assertNull(request.getBody());
		assertNull(request.getQuery());

	}

	private Constructor<DefaultWebActionRequest> getConstructor() {
		try {
			return DefaultWebActionRequest.class.getDeclaredConstructor(String.class, Map.class, String.class,
					String.class, String.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
