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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class ConstructorPreconditionsTesterTest {

	private ConstructorPreconditionsTester tester;
	private static Set<SomeClassCapture> validCaptures = new HashSet<>();
	private static Set<SomeClassCapture> invalidCaptures = new HashSet<>();

	@Before
	public void setup() throws NoSuchMethodException, SecurityException {
		tester = new ConstructorPreconditionsTester(SomeClass.class.getDeclaredConstructor(Integer.class, double.class));
		validCaptures.clear();
		invalidCaptures.clear();
	}

	@Test(expected = IllegalArgumentException.class)
	public void init_NoArgsConstructorGiven_ShouldThrowIae() throws NoSuchMethodException, SecurityException {
		new ConstructorPreconditionsTester(ConstructorPreconditionsTesterTest.class.getDeclaredConstructor());
	}

	@Test(expected = IllegalStateException.class)
	public void testPreconditions_NotAllArgumentsGiven_ShouldThrowIse() {
		tester.addValidArgs(0, 1);
		tester.testPreconditions();
	}

	@Test(expected = IllegalStateException.class)
	public void testPreconditions_NoInvalidArgsSet_ShouldThrowIse() {
		tester.addValidArgs(0, 1);
		tester.addValidArgs(1, 0.0);
		tester.testPreconditions();
	}

	@Test
	public void testPreconditions_ArgumentSetsGiven_ShouldTestInvalidCombinations() {
		tester.addValidArgs(0, 0, 1);
		tester.addInvalidArgs(0, NullPointerException.class, new Object[] {null});
		tester.addValidArgs(1, 0.0, 0.1);
		tester.addInvalidArgs(1, IllegalArgumentException.class, -0.1, -0.2);
		tester.testPreconditions();
		assertEquals(0, validCaptures.size());
		assertEquals(6, invalidCaptures.size());
		assertTrue(invalidCaptures.contains(new SomeClassCapture(null, 0.0)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(null, 0.1)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(0, -0.1)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(0, -0.2)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(1, -0.1)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(1, -0.2)));
	}
	@Test
	public void testPreconditions_ArgumentSetsViaShortcutsGiven_ShouldTestInvalidCombinations() {
		tester.addValidArgs(0, 0, 1);
		tester.addInvalidNpeArg(0);
		tester.addValidArgs(1, 0.0, 0.1);
		tester.addInvalidIaeArgs(1, -0.1, -0.2);
		tester.testPreconditions();
		assertEquals(0, validCaptures.size());
		assertEquals(6, invalidCaptures.size());
		assertTrue(invalidCaptures.contains(new SomeClassCapture(null, 0.0)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(null, 0.1)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(0, -0.1)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(0, -0.2)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(1, -0.1)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(1, -0.2)));
	}

	@Test(expected = AssertionError.class)
	public void testPreconditions_InvalidArgumentGivenThatIsValid_ShouldThrowException() {
		tester.addValidArgs(0, 0);
		tester.addInvalidArgs(0, NullPointerException.class, 1);
		tester.addValidArgs(1, 0.0);
		tester.testPreconditions();
	}

	@Test(expected = AssertionError.class)
	public void testValidCombinations_ValidArgumentGivenThatIsInalid_ShouldThrowException() {
		tester.addValidArgs(0, 0);
		tester.addInvalidNpeArg(0);
		tester.addValidArgs(1, -0.1);
		tester.addInvalidArgs(1, IllegalArgumentException.class, -0.2);
		tester.testValidCombinations();
	}

	@Test
	public void testValidArguments_ArgumentSetsGiven_ShouldTestInvalidCombinations() {
		tester.addValidArgs(0, 0, 1);
		tester.addValidArgs(1, 0.0, 0.1);
		tester.testValidCombinations();
		assertEquals(4, validCaptures.size());
		assertEquals(0, invalidCaptures.size());
		assertTrue(validCaptures.contains(new SomeClassCapture(0, 0.0)));
		assertTrue(validCaptures.contains(new SomeClassCapture(0, 0.1)));
		assertTrue(validCaptures.contains(new SomeClassCapture(1, 0.0)));
		assertTrue(validCaptures.contains(new SomeClassCapture(1, 0.1)));
	}

	@Test
	public void testValidArguments_EqualArgumentSetsGiven_ShouldIgnoreDupesAndTestValidCombinations() {
		Double b = new Double(0.0);
		tester.addValidArgs(0, 0, 0);
		tester.addValidArgs(1, b, b, 0.0);
		tester.testValidCombinations();
		assertEquals(1, validCaptures.size());
		assertEquals(0, invalidCaptures.size());
		assertTrue(validCaptures.contains(new SomeClassCapture(0, 0.0)));
	}

	@Test
	public void testPreconditions_EqualArgumentSetsGiven_ShouldIgnoreDupesAndTestInvalidCombinations() {
		Double bValid = new Double(0.0);
		Double bInvalid = new Double(-0.1);
		tester.addValidArgs(0, 0, 0);
		tester.addInvalidArgs(0, NullPointerException.class, null, null);
		tester.addValidArgs(1, bValid, bValid, 0.0);
		tester.addInvalidArgs(1, IllegalArgumentException.class, bInvalid, bInvalid, -0.1);
		tester.testPreconditions();
		assertEquals(0, validCaptures.size());
		assertEquals(2, invalidCaptures.size());
		assertTrue(invalidCaptures.contains(new SomeClassCapture(0, -0.1)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(null, 0.0)));
	}

	@Test
	public void testPreconditionsAndValidCombinations_ArgumentSetsGiven_ShouldTestInvalidAndValidCombinations() {
		tester.addValidArgs(0, 0, 1);
		tester.addInvalidArgs(0, NullPointerException.class, new Object[] {null});
		tester.addValidArgs(1, 0.0, 0.1);
		tester.addInvalidArgs(1, IllegalArgumentException.class, -0.1, -0.2);
		tester.testPreconditionsAndValidCombinations();
		assertEquals(4, validCaptures.size());
		assertTrue(validCaptures.contains(new SomeClassCapture(0, 0.0)));
		assertTrue(validCaptures.contains(new SomeClassCapture(0, 0.1)));
		assertTrue(validCaptures.contains(new SomeClassCapture(1, 0.0)));
		assertTrue(validCaptures.contains(new SomeClassCapture(1, 0.1)));
		assertEquals(6, invalidCaptures.size());
		assertTrue(invalidCaptures.contains(new SomeClassCapture(null, 0.0)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(null, 0.1)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(0, -0.1)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(0, -0.2)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(1, -0.1)));
		assertTrue(invalidCaptures.contains(new SomeClassCapture(1, -0.2)));
	}

	@Test(expected = IllegalStateException.class)
	public void testValidArguments_NotAllArgumentsGiven_ShouldThrowIse() {
		tester.addValidArgs(0, 1);
		tester.testValidCombinations();
	}

	@Test(expected = IllegalArgumentException.class)
	public void addValidArgs_NullOnPrimitiveTypeGiven_ShouldThrowIae() {
		tester.addValidArgs(1, 0.0, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addValidArgs_IncompatibleTypeGiven_ShouldThrowIae() {
		tester.addValidArgs(0, 1L);
	}

	@Test
	public void addArgs_NullAsVarargs_ShouldAccept() {
		tester.addValidArgs(0, 0);
		tester.addInvalidArgs(0, NullPointerException.class, (Object[]) null);
		tester.addValidArgs(1, 0.0);
		tester.testPreconditions();
		assertEquals(1, invalidCaptures.size());
		assertTrue(invalidCaptures.contains(new SomeClassCapture(null, 0.0)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addInvalidArgs_NullOnPrimitiveTypeGiven_ShouldThrowIae() {
		tester.addInvalidArgs(1, NullPointerException.class, 1.0, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addInvalidArgs_IncompatibleTypeGiven_ShouldThrowIae() {
		tester.addInvalidArgs(0, NullPointerException.class, 1L);
	}

	private static class SomeClassCapture {
		private final Integer a;
		private final double b;
		SomeClassCapture(Integer a, double b) {
			super();
			this.a = a;
			this.b = b;
		}

		@Override
		public boolean equals(final Object other) {
			if (this == other) {
				return true;
			}
			if (other == null) {
				return false;
			}
			if (!getClass().equals(other.getClass())) {
				return false;
			}
			SomeClassCapture castOther = (SomeClassCapture) other;
			return Objects.equals(a, castOther.a) && Objects.equals(b, castOther.b);
		}

		@Override
		public int hashCode() {
			return Objects.hash(a, b);
		}
	}

	private static class SomeClass {
		@SuppressWarnings("unused")
		SomeClass(Integer a, double b) {
			boolean valid = a != null && b >= 0;
			if (valid) {
				validCaptures.add(new SomeClassCapture(a, b));
			} else {
				invalidCaptures.add(new SomeClassCapture(a, b));
			}
			Objects.requireNonNull(a);
			if (b < 0) {
				throw new IllegalArgumentException("b may not be negative");
			}
		}
	}
}
