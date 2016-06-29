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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jrestless.test.ConstructorPreconditionsTester;
import com.jrestless.test.SimpleImmutableValueObjectEqualsTester;

public class GatewayDefaultResponseTest {

	@Test
	public void getBody_NullBodyGiven_ShouldBeReturnedAsIs() {
		GatewayDefaultResponse resp = new GatewayDefaultResponse(null, ImmutableMap.of(), Status.OK);
		Assert.assertEquals(null, resp.getBody());
	}

	@Test
	public void getBody_BodyGiven_ShouldBeReturnedAsIs() {
		GatewayDefaultResponse resp = new GatewayDefaultResponse("body", ImmutableMap.of(), Status.OK);
		Assert.assertEquals("body", resp.getBody());
	}

	@Test
	public void getStatusCode_StatusTypeGiven_ShouldReturnStatusCodeFromType() {
		GatewayDefaultResponse resp = new GatewayDefaultResponse(null, ImmutableMap.of(), Status.CONFLICT);
		Assert.assertEquals(409, resp.getStatusCode());
	}

	@Test
	public void getHeaders_SingleValueForHeaderGiven_ShouldBeReturnedAsIs() {
		Map<String, List<String>> multiHeaders = ImmutableMap.of("Header", ImmutableList.of("val1"));
		GatewayDefaultResponse resp = new GatewayDefaultResponse("", multiHeaders, Status.OK);
		Map<String, String> headers = resp.getHeaders();
		Assert.assertEquals(1, headers.size());
		Assert.assertEquals("val1", headers.get("Header"));
	}

	@Test
	public void getHeaders_MultipleValuesForHeaderGiven_ShouldBeMergedCommaSeparated() {
		Map<String, List<String>> multiHeaders = ImmutableMap.of("Header", ImmutableList.of("val1", "val2"));
		GatewayDefaultResponse resp = new GatewayDefaultResponse("", multiHeaders, Status.OK);
		Map<String, String> headers = resp.getHeaders();
		Assert.assertEquals(1, headers.size());
		Assert.assertEquals("val1,val2", headers.get("Header"));
	}

	@Test
	public void getHeaders_NullValueForHeaderGiven_ShouldBeFilteredOut() {
		Map<String, List<String>> multiHeaders = new HashMap<>();
		multiHeaders.put("Header", null);
		GatewayDefaultResponse resp = new GatewayDefaultResponse("", multiHeaders, Status.OK);
		Map<String, String> headers = resp.getHeaders();
		Assert.assertEquals(0, headers.size());
	}

	@Test
	public void testEquals() {
		new SimpleImmutableValueObjectEqualsTester(getConstructor())
			// body
			.addArguments(0, null, "body", "")
			// headers
			.addArguments(1, ImmutableMap.of(), ImmutableMap.of("Header", ImmutableList.of()))
			.addArguments(1, ImmutableMap.of("Header", Collections.singletonList(null)))
			.addArguments(1, ImmutableMap.of("Header", ImmutableList.of("Val")))
			// statusType
			.addArguments(2, Status.OK, Status.BAD_GATEWAY)
			.testEquals();
	}

	@Test
	public void testConstructorPreconditions() {
		new ConstructorPreconditionsTester(getConstructor())
			// body
			.addValidArgs(0, null, "body")
			// headers
			.addValidArgs(1, ImmutableMap.of(), ImmutableMap.of("Header", ImmutableList.of()))
			.addValidArgs(1, ImmutableMap.of("Header", Collections.singletonList(null)))
			.addValidArgs(1, ImmutableMap.of("Header", ImmutableList.of("Val")))
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
