package com.jrestless.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class UtilityClassCodeCoverageBumperTest {

	@Test
	public void testInvokesPublicNoArgsConstructor() {
		UtilityClassCodeCoverageBumper.invokePrivateConstructor(PublicNoArgs.class);
	}

	@Test
	public void testInvokesPrivateNoArgsConstructor() {
		UtilityClassCodeCoverageBumper.invokePrivateConstructor(PrivateNoArgs.class);
	}

	@Test
	public void testFailsToInvokeClassWithNoNoArgsConstructor() {
		assertThrows(RuntimeException.class, () -> UtilityClassCodeCoverageBumper.invokePrivateConstructor(NoNoArgs.class));
	}

	@Test
	public void bumpCodeCoverageByInvokingThePrivateConstructor() {
		 UtilityClassCodeCoverageBumper.invokePrivateConstructor(UtilityClassCodeCoverageBumper.class);
	}

	private static class PublicNoArgs {
	}

	private static class PrivateNoArgs {
		private PrivateNoArgs() {
		}
	}

	private static class NoNoArgs {
		@SuppressWarnings("unused")
		public NoNoArgs(String x) {
		}
	}
}
