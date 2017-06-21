package com.jrestless.test;

import java.lang.reflect.Constructor;

import org.junit.Test;

public class InvokableArgumentsArgumentTest {

	@Test
	public void testEquals() {
		new CopyConstructorEqualsTester(getConstructor())
			.addArguments(0, null, "a", 1, 2)
			.testEquals();
	}

	private Constructor<InvokableArguments.Argument> getConstructor() {
		try {
			return InvokableArguments.Argument.class.getDeclaredConstructor(Object.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
