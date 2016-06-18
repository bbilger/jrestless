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
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.jrestless.test.ConstructorPreconditionsTester;
import com.jrestless.test.SimpleImmutableValueObjectEqualsTester;

public class ApiGatewayIntegrationExtensionTest {

	private final String credentialRef = "someCredentials";
	private final String uriRef = "someUri";
	private final Map<String, String> requestTemplatesRef = ImmutableMap.of("rtv0", "rtv0", "rtv1", "rtv1");
	private final Map<String, String> requestParametersRef = ImmutableMap.of("rp0", "rp0", "rp1", "rp1");
	private final Map<String, ApiGatewayIntegrationResponse> responsesRef = ImmutableMap.of("resp0",
			mock(ApiGatewayIntegrationResponse.class), "resp1", mock(ApiGatewayIntegrationResponse.class));
	private ApiGatewayIntegrationExtension extensionRef;

	@Before
	public void setup() {
		Map<String, String> requestTemplates = new HashMap<>(requestTemplatesRef);
		Map<String, String> requestParameters = new HashMap<>(requestParametersRef);
		Map<String, ApiGatewayIntegrationResponse> responses = new HashMap<>(responsesRef);
		extensionRef = new ApiGatewayIntegrationExtension("someCredentials", "someUri", requestTemplates, requestParameters, responses);
	}

	@Test
	public void testGetters() {
		assertEquals(credentialRef, extensionRef.getCredentials());
		assertEquals(uriRef, extensionRef.getUri());
		assertEquals(requestTemplatesRef, extensionRef.getRequestTemplates());
		assertEquals(requestParametersRef, extensionRef.getRequestParameters());
		assertEquals(responsesRef, extensionRef.getResponses());
		assertEquals("aws", extensionRef.getType());
		assertEquals("POST", extensionRef.getHttpMethod());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRequestTemplateImmutability() {
		extensionRef.getRequestTemplates().put("some", "value");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testRequestParametersImmutability() {
		extensionRef.getRequestParameters().put("some", "value");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testResponsesImmutability() {
		extensionRef.getResponses().put("some", mock(ApiGatewayIntegrationResponse.class));
	}

	@Test
	public void testConstructorPreconditions() {
		Constructor<ApiGatewayIntegrationExtension> constructor = getConstructor();
		new ConstructorPreconditionsTester(constructor)
				// credentials
				.addValidArgs(0, "cred")
				.addInvalidIaeArgs(0, null, "")
				// uri
				.addValidArgs(1, "uri")
				.addInvalidIaeArgs(1, null, "")
				// requestTemplates
				.addValidArgs(2, ImmutableMap.of())
				.addInvalidNpeArg(2)
				// requestParameters
				.addValidArgs(3, ImmutableMap.of())
				.addInvalidNpeArg(3)
				// responses
				.addValidArgs(4, ImmutableMap.of())
				.addInvalidNpeArg(4)
				.testPreconditionsAndValidCombinations();
	}

	private static Constructor<ApiGatewayIntegrationExtension> getConstructor() {
		try {
			return ApiGatewayIntegrationExtension.class
			.getConstructor(String.class, String.class, Map.class, Map.class, Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testEquals() {
		new SimpleImmutableValueObjectEqualsTester(getConstructor())
			.addArguments(0, "cred0", "cred1")
			.addArguments(1, "uri0", "uri1")
			.addArguments(2, ImmutableMap.of(), ImmutableMap.of("k00", "v00"), ImmutableMap.of("k01", "v01"))
			.addArguments(3, ImmutableMap.of(), ImmutableMap.of("k10", "v10"), ImmutableMap.of("k11", "v11"))
			.addArguments(4, ImmutableMap.of(), ImmutableMap.of("k20", mock(ApiGatewayIntegrationResponse.class)), ImmutableMap.of("k21", mock(ApiGatewayIntegrationResponse.class)))
			.testEquals();
	}
}
