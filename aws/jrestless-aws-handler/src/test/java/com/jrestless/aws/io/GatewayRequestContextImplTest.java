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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import com.jrestless.test.SimpleImmutableValueObjectEqualsTester;

public class GatewayRequestContextImplTest {

	@Test(expected = UnsupportedOperationException.class)
	public void testGetStageVariablesReturnsImmutableMap() {
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setStageVariables(new HashMap<>());
		context.getStageVariables().put("1", "1");
	}

	@Test
	public void testSetStageVariablesCopiesValues() {
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		Map<String, String> stageVariables = new HashMap<>();
		stageVariables.put("1", "1");
		stageVariables.put("2", "2");
		context.setStageVariables(stageVariables);
		stageVariables.remove("1");
		assertEquals(2, context.getStageVariables().size());
	}

	@Test
	public void testSetStageVariablesClearsValues() {
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setStageVariables(ImmutableMap.of("1", "1"));
		context.setStageVariables(ImmutableMap.of("2", "2"));
		assertEquals(1, context.getStageVariables().size());
		assertEquals("2", context.getStageVariables().get("2"));
	}

	@Test
	public void testSetStageVariablesClearsValuesOnNull() {
		GatewayRequestContextImpl context = new GatewayRequestContextImpl();
		context.setStageVariables(ImmutableMap.of("1", "1"));
		context.setStageVariables(null);
		assertEquals(0, context.getStageVariables().size());
	}

	@Test
	public void testGetters() {
		GatewayRequestContextImpl filled = new GatewayRequestContextImpl("apiId", "principalId", "httpMethod",
				"accountId", "apiKey", "caller", "cognitoAuthenticationProvider", "cognitoAuthenticationType",
				"cognitoIdentityId", "cognitoIdentityPoolId", "sourceIp", "user", "userAgent", "userArn", "requestId",
				"resourceId", "resourcePath", "stage", ImmutableMap.of());

		assertEquals("apiId", filled.getApiId());
		assertEquals("principalId", filled.getPrincipalId());
		assertEquals("httpMethod", filled.getHttpMethod());
		assertEquals("accountId", filled.getAccountId());
		assertEquals("apiKey", filled.getApiKey());
		assertEquals("caller", filled.getCaller());
		assertEquals("cognitoAuthenticationProvider", filled.getCognitoAuthenticationProvider());
		assertEquals("cognitoAuthenticationType", filled.getCognitoAuthenticationType());
		assertEquals("cognitoIdentityId", filled.getCognitoIdentityId());
		assertEquals("cognitoIdentityPoolId", filled.getCognitoIdentityPoolId());
		assertEquals("sourceIp", filled.getSourceIp());
		assertEquals("user", filled.getUser());
		assertEquals("userAgent", filled.getUserAgent());
		assertEquals("userArn", filled.getUserArn());
		assertEquals("requestId", filled.getRequestId());
		assertEquals("resourceId", filled.getResourceId());
		assertEquals("resourcePath", filled.getResourcePath());
		assertEquals("stage", filled.getStage());
	}

	@Test
	public void testToNullTrimming() {
		GatewayRequestContextImpl emptyObj = new GatewayRequestContextImpl("", "", "", "", "", "", "",
				"", "", "", "", "", "", "", "", "", "", "", ImmutableMap.of());
		assertNull(emptyObj.getApiId());
		assertNull(emptyObj.getPrincipalId());
		assertNull(emptyObj.getHttpMethod());
		assertNull(emptyObj.getAccountId());
		assertNull(emptyObj.getApiKey());
		assertNull(emptyObj.getCaller());
		assertNull(emptyObj.getCognitoAuthenticationProvider());
		assertNull(emptyObj.getCognitoAuthenticationType());
		assertNull(emptyObj.getCognitoIdentityId());
		assertNull(emptyObj.getCognitoIdentityPoolId());
		assertNull(emptyObj.getSourceIp());
		assertNull(emptyObj.getUser());
		assertNull(emptyObj.getUserAgent());
		assertNull(emptyObj.getUserArn());
		assertNull(emptyObj.getRequestId());
		assertNull(emptyObj.getResourceId());
		assertNull(emptyObj.getResourcePath());
		assertNull(emptyObj.getStage());
	}

	@Test
	public void testEquals() {
		GatewayRequestContextImpl filled0 = new GatewayRequestContextImpl("apiId", "principalId", "httpMethod",
				"accountId", "apiKey", "caller", "cognitoAuthenticationProvider", "cognitoAuthenticationType",
				"cognitoIdentityId", "cognitoIdentityPoolId", "sourceIp", "user", "userAgent", "userArn", "requestId",
				"resourceId", "resourcePath", "stage", ImmutableMap.of("stageKey", "stageVal"));
		GatewayRequestContextImpl filled1 = new GatewayRequestContextImpl("apiId", "principalId", "httpMethod",
				"accountId", "apiKey", "caller", "cognitoAuthenticationProvider", "cognitoAuthenticationType",
				"cognitoIdentityId", "cognitoIdentityPoolId", "sourceIp", "user", "userAgent", "userArn", "requestId",
				"resourceId", "resourcePath", "stage", ImmutableMap.of("stageKey", "stageVal"));
		GatewayRequestContextImpl nullObj = new GatewayRequestContextImpl(null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, null, null, null, null, null);
		GatewayRequestContextImpl emptyObj = new GatewayRequestContextImpl("", "", "", "", "", "", "",
				"", "", "", "", "", "", "", "", "", "", "", ImmutableMap.of());
		new EqualsTester()
			.addEqualityGroup(filled0, filled1)
			.addEqualityGroup(nullObj, emptyObj)
			.testEquals();
		// there are too many combinations to test all so just check if each param is taken into consideration
		for (int i = 0; i < getConstructor().getParameterCount(); i++) {
			new SimpleImmutableValueObjectEqualsTester(getConstructor())
				.addArguments(i, new Object[] { null })
				// apiId
				.addArguments(0, "apiId")
				// principalId
				.addArguments(1, "principalId")
				// httpMethod
				.addArguments(2, "httpMethod")
				// accountId
				.addArguments(3, "accountId")
				// apiKey
				.addArguments(4, "apiKey")
				// caller
				.addArguments(5, "caller")
				// cognitoAuthenticationProvider
				.addArguments(6, "cognitoAuthenticationProvider")
				// cognitoAuthenticationType
				.addArguments(7, "cognitoAuthenticationType")
				// cognitoIdentityId
				.addArguments(8, "cognitoIdentityId")
				// cognitoIdentityPoolId
				.addArguments(9, "cognitoIdentityPoolId")
				// sourceIp
				.addArguments(10, "sourceIp")
				// user
				.addArguments(11, "user")
				// userAgent
				.addArguments(12, "userAgent")
				// userArn
				.addArguments(13, "userArn")
				// requestId
				.addArguments(14, "requestId")
				// resourceId
				.addArguments(15, "resourceId")
				// resourcePath
				.addArguments(16, "resourcePath")
				// stage
				.addArguments(17, "stage")
				// stageVariables
				.addArguments(18, ImmutableMap.of("stageKey", "stageValue"))
				.testEquals();
		}

	}

	private Constructor<GatewayRequestContextImpl> getConstructor() {
		try {
			return GatewayRequestContextImpl.class.getDeclaredConstructor(String.class, String.class, String.class,
					String.class, String.class, String.class, String.class, String.class, String.class, String.class,
					String.class, String.class, String.class, String.class, String.class, String.class, String.class,
					String.class, Map.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
