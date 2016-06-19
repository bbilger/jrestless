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
