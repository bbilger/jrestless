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
package com.jrestless.aws.swagger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;

import org.junit.Test;

import com.jrestless.aws.swagger.ApiGatewayIntegrationExtensionFactoryImpl.AwsOperationContext;
import com.jrestless.aws.swagger.ApiGatewayIntegrationExtensionFactoryImpl.ResponseContext;
import com.jrestless.test.ConstructorPreconditionsTester;

import io.swagger.models.Response;

public class ResponseContextTest {
	@Test
	public void testGetters() {
		AwsOperationContext aoc = mock(AwsOperationContext.class);
		Response response = mock(Response.class);
		ResponseContext rc = new ResponseContext(aoc, response, "123");
		assertEquals(aoc, rc.getAwsOperationContext());
		assertEquals(response, rc.getResponse());
		assertEquals("123", rc.getStatusCode());
	}

	@Test
	public void testConstructorPreconditions() {
		Constructor<ResponseContext> constructor = getConstructor();
		new ConstructorPreconditionsTester(constructor)
				// awsOperationContext
				.addValidArgs(0, mock(AwsOperationContext.class))
				.addInvalidNpeArg(0)
				// response
				.addValidArgs(1, new Response())
				.addInvalidNpeArg(1)
				// statusCode
				.addValidArgs(2, "test")
				.addInvalidNpeArg(2)
				.testPreconditionsAndValidCombinations();
	}

	private static Constructor<ResponseContext> getConstructor() {
		try {
			return ResponseContext.class.getDeclaredConstructor(AwsOperationContext.class, Response.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
