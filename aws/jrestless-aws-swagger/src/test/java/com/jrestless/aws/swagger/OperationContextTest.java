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
import java.lang.reflect.Method;

import org.junit.Test;

import com.jrestless.test.ConstructorPreconditionsTester;
import com.jrestless.test.SimpleImmutableValueObjectEqualsTester;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;

public class OperationContextTest {

	@Test
	public void testGetters() {
		Operation operation = mock(Operation.class);
		Swagger swagger = mock(Swagger.class);
		OperationContext oc = new OperationContext(operation, getEndpointMethod("endpoint0"), swagger);
		assertEquals(operation, oc.getOperation());
		assertEquals(getEndpointMethod("endpoint0"), oc.getEndpointMethod());
		assertEquals(swagger, oc.getSwagger());
	}

	@Test
	public void testEquals() {
		Operation op1 = new Operation();
		Operation op2 = new Operation();
		op2.setDescription("asd");
		Swagger s1 = new Swagger();
		Swagger s2 = new Swagger();
		s2.setBasePath("asd");
		new SimpleImmutableValueObjectEqualsTester(getConstructor())
			// operation
			.addArguments(0, op1, op2)
			// endpointMethod
			.addArguments(1, getEndpointMethod("endpoint0"))
			.addArguments(1, getEndpointMethod("endpoint1"))
			// swagger
			.addArguments(2, s1, s2)
			.testEquals();
	}

	@Test
	public void testConstructorPreconditions() {
		Constructor<OperationContext> constructor = getConstructor();
		new ConstructorPreconditionsTester(constructor)
				// operation
				.addValidArgs(0, new Operation())
				.addInvalidNpeArg(0)
				// endpointMethod
				.addValidArgs(1, getEndpointMethod("endpoint0"))
				.addInvalidNpeArg(1)
				// swagger
				.addValidArgs(2, new Swagger())
				.addInvalidNpeArg(2)
				.testPreconditionsAndValidCombinations();
	}

	private static Method getEndpointMethod(String endpointMethodName) {
		try {
			return Resource0.class.getMethod(endpointMethodName);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static Constructor<OperationContext> getConstructor() {
		try {
			return OperationContext.class.getConstructor(Operation.class, Method.class, Swagger.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static class Resource0 {
		@SuppressWarnings("unused")
		public void endpoint0() {
		}
		@SuppressWarnings("unused")
		public void endpoint1() {
		}
	}
}
