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

import org.junit.Test;

public class SimpleImmutableValueObjectEqualsTesterTest {

	@Test(expected = IllegalStateException.class)
	public void testEquals_NotAllParametersGiven_ShouldThrowIse() throws NoSuchMethodException, SecurityException {
		new SimpleImmutableValueObjectEqualsTester(CorrectValueObject.class.getDeclaredConstructor(Integer.class, double.class))
			.addArguments(0, 0, 1)
			.testEquals();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEquals_InvalidArgumentTypeGiven_ShouldThrowIae() throws NoSuchMethodException, SecurityException {
		new SimpleImmutableValueObjectEqualsTester(CorrectValueObject.class.getDeclaredConstructor(Integer.class, double.class))
			.addArguments(0, 0, 1L);
	}

	@Test
	public void testEquals_CorrectValueObjectGiven_ShouldPassEqualsTest() throws NoSuchMethodException, SecurityException {
		new SimpleImmutableValueObjectEqualsTester(CorrectValueObject.class.getDeclaredConstructor(Integer.class, double.class))
			.addArguments(0, 0, 1)
			.addArguments(1, 0.0, 0.1)
			.testEquals();
	}

	@Test(expected = AssertionError.class)
	public void testEquals_IncorrectValueObjectGiven_ShouldNotPassEqualsTest() throws NoSuchMethodException, SecurityException {
		new SimpleImmutableValueObjectEqualsTester(IncorrectValueObject.class.getDeclaredConstructor(Integer.class, double.class, char.class))
			.addArguments(0, 0, 1)
			.addArguments(1, 0.0, 0.1)
			.addArguments(2, 'a', 'b')
			.testEquals();
	}

	private static class IncorrectValueObject extends CorrectValueObject {

		private final char c;

		IncorrectValueObject(Integer a, double b, char c) {
			super(a, b);
			this.c = c;
		}

		@Override
		public String toString() {
			return "IncorrectValueObject [c=" + c + ", getA()=" + getA() + ", getB()=" + getB() + "]";
		}
	}

	private static class CorrectValueObject {

		private final Integer a;
		private final double b;

		CorrectValueObject(Integer a, double b) {
			this.a = a;
			this.b = b;
		}

		Integer getA() {
			return a;
		}

		double getB() {
			return b;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			long temp;
			temp = Double.doubleToLongBits(b);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			CorrectValueObject other = (CorrectValueObject) obj;
			if (a == null) {
				if (other.a != null) {
					return false;
				}
			} else if (!a.equals(other.a)) {
				return false;
			}
			if (Double.doubleToLongBits(b) != Double.doubleToLongBits(other.b)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "SomeClass [a=" + a + ", b=" + b + "]";
		}

	}
}
