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
package com.jrestless.core.container.dpi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import org.glassfish.jersey.process.internal.RequestScoped;
import org.junit.Test;

import com.jrestless.core.container.dpi.InstanceBinder.InstanceFactoryDefinition;
import com.jrestless.test.ConstructorPreconditionsTester;

public class InstanceFactoryDefinitionTest {

	@Test
	public void testGetters() {
		SomeObject instance = mock(SomeObject.class);
		InstanceFactoryDefinition<SomeObject> instanceFactory = new InstanceFactoryDefinition<>(instance,
				SomeObject.class, RequestScoped.class);
		assertSame(instance, instanceFactory.getInstanceFactory().get());
		assertEquals(SomeObject.class, instanceFactory.getType());
		assertEquals(RequestScoped.class, instanceFactory.getScope());
	}

	@Test
	public void testPreconditions() throws NoSuchMethodException, SecurityException {
		new ConstructorPreconditionsTester(
				InstanceFactoryDefinition.class.getDeclaredConstructor(Object.class, Class.class, Class.class))
			.addValidArgs(0, "String")
			.addInvalidNpeArg(0)
			.addValidArgs(1, String.class)
			.addInvalidNpeArg(1)
			.addValidArgs(2, RequestScoped.class)
			.addInvalidNpeArg(2)
			.testPreconditionsAndValidCombinations();
	}

	private interface SomeObject {

	}
}
