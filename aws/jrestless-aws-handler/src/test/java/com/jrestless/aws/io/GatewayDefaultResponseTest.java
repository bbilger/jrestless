/*
 * Copyright 2016 Bjoern Bilger
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
package com.jrestless.aws.io;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.jrestless.test.ConstructorPreconditionsTester;
import com.jrestless.test.SimpleImmutableValueObjectEqualsTester;

public class GatewayDefaultResponseTest {

	@Test
	public void getStatusCode_StatusTypeGiven_ShouldReturnStatusCodeFromType() {
		GatewayDefaultResponse resp = new GatewayDefaultResponse(null, ImmutableMap.of(), Status.CONFLICT);
		Assert.assertEquals(409, resp.getStatusCode());
	}

	@Test
	public void testEquals() {
		Map<String, String> nullHeader = new HashMap<>();
		nullHeader.put("headerName", null);
		new SimpleImmutableValueObjectEqualsTester(getConstructor())
			// body
			.addArguments(0, null, "body", "")
			// headers
			.addArguments(1, ImmutableMap.of(), ImmutableMap.of("headerName", "headerValue"), nullHeader)
			// statusType
			.addArguments(2, Status.OK, Status.BAD_GATEWAY)
			.testEquals();
	}

	@Test
	public void testConstructorPreconditions() {
		Map<String, String> nullHeader = new HashMap<>();
		nullHeader.put("headerName", null);
		new ConstructorPreconditionsTester(getConstructor())
			// body
			.addValidArgs(0, null, "body")
			// headers
			.addValidArgs(1, ImmutableMap.of(), ImmutableMap.of("headerName", "headerValue"), nullHeader)
			.addInvalidNpeArg(1)
			// statusType
			.addValidArgs(2, Status.OK)
			.addInvalidNpeArg(2)
			.testPreconditionsAndValidCombinations();
	}

	private Constructor<GatewayDefaultResponse> getConstructor() {
		try {
			return GatewayDefaultResponse.class.getConstructor(String.class, Map.class, StatusType.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
