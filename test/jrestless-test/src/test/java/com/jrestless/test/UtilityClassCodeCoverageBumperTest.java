package com.jrestless.test;

import org.junit.Test;

public class UtilityClassCodeCoverageBumperTest {

	@Test
	public void testInvokesPublicNoArgsConstructor() {
		UtilityClassCodeCoverageBumper.invokePrivateConstructor(PublicNoArgs.class);
	}

	@Test
	public void testInvokesPrivateNoArgsConstructor() {
		UtilityClassCodeCoverageBumper.invokePrivateConstructor(PrivateNoArgs.class);
	}

	@Test(expected = RuntimeException.class)
	public void testFailsToInvokeClassWithNoNoArgsConstructor() {
		UtilityClassCodeCoverageBumper.invokePrivateConstructor(NoNoArgs.class);
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
		public NoNoArgs(String x) {
		}
	}
}
