package com.jrestless.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Test;

public class AccessibleRunnerTest {
	@Test
	public void run_InaccessibleAoGiven_ShouldMakeAoAccessibleBeforeInvocation() throws NoSuchMethodException, SecurityException {
		Method m = SomeObject.class.getDeclaredMethod("method");
		AccessibleRunner.run(m, () -> {
			assertTrue(m.isAccessible());
			return "whatever";
		});
	}

	@Test
	public void run_InaccessibleAoGiven_ShouldMakeAoInaccessibleAgainAfterInvocation() throws NoSuchMethodException, SecurityException {
		Method m = SomeObject.class.getDeclaredMethod("method");
		AccessibleRunner.run(m, () -> {
			return "whatever";
		});
		assertFalse(m.isAccessible());
	}

	@Test
	public void run_InaccessibleAoAndThrowingSupplierGiven_ShouldMakeAoInaccessibleAgainAfterInvocation() throws NoSuchMethodException, SecurityException {
		Method m = SomeObject.class.getDeclaredMethod("method");
		try {
			AccessibleRunner.run(m, () -> {
				throw new RuntimeException();
			});
			fail();
		} catch (RuntimeException re) {
			assertFalse(m.isAccessible());
		}
	}

	@Test
	public void run_AccessibleAoGiven_ShouldKeepAoAccessibleBeforeInvocation() throws NoSuchMethodException, SecurityException {
		Method m = SomeObject.class.getDeclaredMethod("method");
		m.setAccessible(true);
		AccessibleRunner.run(m, () -> {
			assertTrue(m.isAccessible());
			return "whatever";
		});
	}

	@Test
	public void run_AccessibleAoGiven_ShouldKeepAoAccessibleAfterInvocation() throws NoSuchMethodException, SecurityException {
		Method m = SomeObject.class.getDeclaredMethod("method");
		m.setAccessible(true);
		AccessibleRunner.run(m, () -> {
			return "whatever";
		});
		assertTrue(m.isAccessible());
	}

	@Test
	public void run_AccessibleAoAndThrowingSupplierGiven_ShouldKeepAoAccessibleAfterInvocation() throws NoSuchMethodException, SecurityException {
		Method m = SomeObject.class.getDeclaredMethod("method");
		m.setAccessible(true);
		try {
			AccessibleRunner.run(m, () -> {
				throw new RuntimeException();
			});
			fail();
		} catch (RuntimeException re) {
			assertTrue(m.isAccessible());
		}
	}

	@Test
	public void bumpCodeCoverageByInvokingThePrivateConstructor() {
		 UtilityClassCodeCoverageBumper.invokePrivateConstructor(AccessibleRunner.class);
	}

	public static class SomeObject {
		@SuppressWarnings("unused")
		private void method() {

		}
	}
}
