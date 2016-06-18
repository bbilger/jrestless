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
package com.jrestless.aws.swagger.models;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.jrestless.test.ConstructorPreconditionsTester;
import com.jrestless.test.SimpleImmutableValueObjectEqualsTester;

public class ApiGatewayIntegrationResponseTest {

	private Map<String, String> responseParametersRef = ImmutableMap.of("rpk0", "rpv0", "rpk1", "rpk1");
	private Map<String, String> responseTemplatesRef = ImmutableMap.of("rtk0", "rtv0", "rtk1", "rtk1");
	private String statusCodeRef = "123";
	private ApiGatewayIntegrationResponse responseRef;

	@Before
	public void setup() {
		responseRef = new ApiGatewayIntegrationResponse(statusCodeRef, new HashMap<>(responseParametersRef),
				new HashMap<>(responseTemplatesRef));
	}

	@Test
	public void testGetters() {
		assertEquals(statusCodeRef, responseRef.getStatusCode());
		assertEquals(responseParametersRef, responseRef.getResponseParameters());
		assertEquals(responseTemplatesRef, responseRef.getResponseTemplates());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testResponseParametersImmutability() {
		responseRef.getResponseParameters().put("some", "value");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testResponseTemplatesImmutability() {
		responseRef.getResponseTemplates().put("some", "value");
	}

	@Test
	public void testConstructorPreconditions() {
		new ConstructorPreconditionsTester(getConstructor())
		// statusCode
		.addValidArgs(0, "200")
		.addInvalidIaeArgs(0, null, "")
		// responseParameters
		.addValidArgs(1, ImmutableMap.of())
		.addInvalidNpeArg(1)
		// responseTemplates
		.addValidArgs(2, ImmutableMap.of())
		.addInvalidNpeArg(2)
		.testPreconditionsAndValidCombinations();
	}

	@Test
	public void testEquals() {
		new SimpleImmutableValueObjectEqualsTester(getConstructor())
			// statusCode
			.addArguments(0, "1", "2")
			// responseParameters
			.addArguments(1, ImmutableMap.of(), ImmutableMap.of("k00", "v00"), ImmutableMap.of("k01", "v01"))
			// responseTemplates
			.addArguments(2, ImmutableMap.of(), ImmutableMap.of("k10", "v10"), ImmutableMap.of("k11", "v11"))
			.testEquals();
	}

	private static Constructor<ApiGatewayIntegrationResponse> getConstructor() {
		try {
			return ApiGatewayIntegrationResponse.class.getConstructor(String.class, Map.class, Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
