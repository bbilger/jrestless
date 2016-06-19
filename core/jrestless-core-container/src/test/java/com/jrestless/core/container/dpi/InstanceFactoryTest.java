package com.jrestless.core.container.dpi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.glassfish.jersey.process.internal.RequestScoped;
import org.junit.Test;

import com.jrestless.core.container.dpi.InstanceBinder.InstanceFactory;
import com.jrestless.test.ConstructorPreconditionsTester;

public class InstanceFactoryTest {

	@Test
	public void dispose_ShouldDoNothing() {
		SomeObject instance = mock(SomeObject.class);
		InstanceFactory<SomeObject> instanceFactory = new InstanceFactory<>(instance, SomeObject.class, RequestScoped.class);
		instanceFactory.dispose(instance);
		verifyZeroInteractions(instance);
	}

	@Test
	public void testGetters() {
		SomeObject instance = mock(SomeObject.class);
		InstanceFactory<SomeObject> instanceFactory = new InstanceFactory<>(instance, SomeObject.class, RequestScoped.class);
		assertSame(instance, instanceFactory.provide());
		assertEquals(SomeObject.class, instanceFactory.getType());
		assertEquals(RequestScoped.class, instanceFactory.getScope());
	}

	@Test
	public void testPreconditions() throws NoSuchMethodException, SecurityException {
		new ConstructorPreconditionsTester(InstanceFactory.class.getConstructor(Object.class, Class.class, Class.class))
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
