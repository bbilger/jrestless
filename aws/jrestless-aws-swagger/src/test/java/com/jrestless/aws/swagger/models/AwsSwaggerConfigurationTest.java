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


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jrestless.aws.swagger.models.AwsSwaggerConfiguration.AuthType;
import com.jrestless.test.ConstructorPreconditionsTester;
import com.jrestless.test.SimpleImmutableValueObjectEqualsTester;

public class AwsSwaggerConfigurationTest {

	private final String lambdaCredentialRef = "someLambdaCredential";
	private final String lambdaUriRef = "someLambdaUriRef";
	private final boolean defaultCorsEnabledRef = false;
	private final String defaultAccessControlAllowOriginRef = "someDefaultAccessControlAllowOrigin";
	private final String defaultAccessControlAllowHeadersRef = "someDefaultAccessControlAllowHeaders";
	private final AuthType defaultAuthTypeRef = AuthType.AUTHORIZER;
	private final List<Map<String, List<String>>> defaultSecurityRef = ImmutableList.of(ImmutableMap.of("a", ImmutableList.of("b")));
	private final int[] additionalResponseCodesRef = new int[] { 400, 404, 500 };
	private final String[] supportedNonDefaultHeadersInOrderRef = new String[] { "Content-Type", "Location" };

	private final AwsSwaggerConfiguration emptyCfg = new AwsSwaggerConfiguration(null, null, null, null, null, null,
			null, null, null);
	private final AwsSwaggerConfiguration filledCfg = new AwsSwaggerConfiguration(lambdaCredentialRef, lambdaUriRef,
			defaultCorsEnabledRef, defaultAccessControlAllowOriginRef, defaultAccessControlAllowHeadersRef,
			defaultAuthTypeRef, new ArrayList<>(defaultSecurityRef), additionalResponseCodesRef,
			supportedNonDefaultHeadersInOrderRef);

	@Test
	public void testFilledGetters() {
		assertEquals(lambdaCredentialRef, filledCfg.getLambdaCredential());
		assertEquals(lambdaUriRef, filledCfg.getLambdaUri());
		assertEquals(defaultCorsEnabledRef, filledCfg.isDefaultCorsEnabled());
		assertEquals(defaultAccessControlAllowOriginRef, filledCfg.getDefaultAccessControlAllowOrigin());
		assertEquals(defaultAccessControlAllowHeadersRef, filledCfg.getDefaultAccessControlAllowHeaders());
		assertEquals(defaultAuthTypeRef, filledCfg.getDefaultAuthType());
		assertEquals(defaultSecurityRef, filledCfg.getDefaultSecurity());
		assertArrayEquals(additionalResponseCodesRef, filledCfg.getAdditionalResponseCodes());
	}

	@Test
	public void testEmptyGetters() {
		assertNull(emptyCfg.getLambdaCredential());
		assertNull(emptyCfg.getLambdaUri());
		assertNull(emptyCfg.getDefaultAccessControlAllowOrigin());
		assertNull(emptyCfg.getDefaultAccessControlAllowHeaders());
		assertNull(emptyCfg.getDefaultAuthType());
		assertNull(emptyCfg.getDefaultSecurity());
		assertNull(emptyCfg.getAdditionalResponseCodes());
	}

	@Test
	public void testIsSetOnFilled() {
		assertTrue(filledCfg.isSetLambdaCredential());
		assertTrue(filledCfg.isSetLambdaUri());
		assertTrue(filledCfg.isSetDefaultCorsEnabled());
		assertTrue(filledCfg.isSetDefaultAccessControlAllowOrigin());
		assertTrue(filledCfg.isSetDefaultAccessControlAllowHeaders());
		assertTrue(filledCfg.isSetDefaultAuthType());
		assertTrue(filledCfg.isSetDefaultSecurity());
		assertTrue(filledCfg.isSetAdditionalResponseCodes());
		assertTrue(filledCfg.isSetSupportedNonDefaultHeadersInOrder());
	}

	@Test
	public void testIsSetOnEmpty() {
		assertFalse(emptyCfg.isSetLambdaCredential());
		assertFalse(emptyCfg.isSetLambdaUri());
		assertFalse(emptyCfg.isSetDefaultCorsEnabled());
		assertFalse(emptyCfg.isSetDefaultAccessControlAllowOrigin());
		assertFalse(emptyCfg.isSetDefaultAccessControlAllowHeaders());
		assertFalse(emptyCfg.isSetDefaultAuthType());
		assertFalse(emptyCfg.isSetDefaultSecurity());
		assertFalse(emptyCfg.isSetAdditionalResponseCodes());
		assertFalse(emptyCfg.isSetSupportedNonDefaultHeadersInOrder());
	}

	@Test
	public void testConstructorPreconditions() {
		// esp. null is valid, here => make sure it's acceptable
		new ConstructorPreconditionsTester(getConstructor())
			// lambdaCredential
			.addValidArgs(0, null, "", "cred")
			// lambdaUri
			.addValidArgs(1, null, "", "uri")
			// defaultCorsEnabled
			.addValidArgs(2, null, true, false)
			// defaultAccessControlAllowOrigin
			.addValidArgs(3, null, "", "origin")
			// defaultAccessControlAllowHeaders
			.addValidArgs(4, null, "", "headers")
			// defaultAuthType
			.addValidArgs(5, null, AuthType.AUTHORIZER, AuthType.NONE, AuthType.IAM)
			// defaultSecurity
			.addValidArgs(6, null, null, ImmutableList.of(), ImmutableList.of(ImmutableMap.of()))
			.addValidArgs(6, ImmutableList.of(ImmutableMap.of("a", ImmutableList.of())))
			.addValidArgs(6, ImmutableList.of(ImmutableMap.of("a", ImmutableList.of("b"))))
			.addValidArgs(7, null, new int[0], new int[] {0, 1})
			.addValidArgs(8, null, new String[0], new String[] {"x", "y"})
			.testValidCombinations();
	}

	@Test
	public void testEquals() {
		new SimpleImmutableValueObjectEqualsTester(getConstructor())
			// lambdaCredential
			.addArguments(0, null, "cred")
			// lambdaUri
			.addArguments(1, null, "uri")
			// defaultCorsEnabled
			.addArguments(2, null, true, false)
			// defaultAccessControlAllowOrigin
			.addArguments(3, null, "origin")
			// defaultAccessControlAllowHeaders
			.addArguments(4, null, "headers")
			// defaultAuthType
			.addArguments(5, null, AuthType.AUTHORIZER, AuthType.NONE, AuthType.IAM)
			// defaultSecurity
			.addArguments(6, null, ImmutableList.of())
			.addArguments(6, ImmutableList.of(ImmutableMap.of("a", ImmutableList.of("b"))))
			.addArguments(7, null, new int[0], new int[] {0, 1})
			.addArguments(8, null, new String[0], new String[] {"x", "y"})
			.testEquals();
	}

	@Test(expected = NullPointerException.class)
	public void authTypeForValue_NullGiven_ShouldThrowNpe() {
		assertNull(AuthType.forValue(null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void authTypeForValue_UnknownValueGiven_ShouldThrowIae() {
		assertNull(AuthType.forValue("asd"));
	}

	@Test
	public void authTypeForValue_KnownValueGiven_ShouldReturnType() {
		assertEquals(AuthType.NONE, AuthType.forValue("none"));
		assertEquals(AuthType.AUTHORIZER, AuthType.forValue("authorizer"));
		assertEquals(AuthType.IAM, AuthType.forValue("iam"));
	}

	private static Constructor<AwsSwaggerConfiguration> getConstructor() {
		try {
			return AwsSwaggerConfiguration.class.getDeclaredConstructor(String.class, String.class, Boolean.class,
					String.class, String.class, AuthType.class, List.class, int[].class, String[].class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
