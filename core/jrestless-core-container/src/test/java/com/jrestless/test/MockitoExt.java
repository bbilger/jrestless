package com.jrestless.test;

import static org.mockito.Matchers.argThat;

import java.io.ByteArrayOutputStream;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

public final class MockitoExt {

	public static ByteArrayOutputStream emptyBaos() {
		return eqBaos("");
	}
	public static ByteArrayOutputStream eqBaos(final String expected) {
		return argThat(new ArgumentMatcher<ByteArrayOutputStream>() {
			@Override
			public boolean matches(Object actual) {
				if (actual instanceof ByteArrayOutputStream) {
					return expected.equals(actual.toString());
				} else {
					return false;
				}
			}
			@Override
			public void describeTo(Description description) {
				description.appendText(expected);
			}
		});
	}
}
