package com.jrestless.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Invokes the private no-args constructor of utility classes to bump code coverage.
 *
 * @author Bjoern Bilger
 *
 */
public final class UtilityClassCodeCoverageBumper {

	private UtilityClassCodeCoverageBumper() {
	}

	public static <T> void invokePrivateConstructor(Class<T> clazz) {
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			constructor.newInstance();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new ConstructorInvocationException(e);
		}
	}

	public static final class ConstructorInvocationException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		private ConstructorInvocationException(Exception e) {
			super(e);
		}

	}
}
