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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.google.common.testing.EqualsTester;
import com.jrestless.test.InvokableArguments.Argument;

/**
 * Simple equals tester for immutable value objects.
 * <p>
 * A constructor must be provided, as well as a list of possible arguments for
 * each parameter ({@link #addArguments(int, Object...)}. With all the possible
 * combinations of the arguments (the cartesian product), objects are generated.
 * Each object is considered to be equal to itself only. This makes sure that
 * when adding new properties to a value object, they will be added to the
 * equals and hashCode methods, too.
 * <p>
 * The number of possible arguments per parameter should be kept low since the
 * combinations to test quickly explode.
 *
 *
 * @author Bjoern Bilger
 *
 */
public class SimpleImmutableValueObjectEqualsTester {

	private final InvokableArguments<Argument> arguments;
	private final Constructor<?> constructor;

	public SimpleImmutableValueObjectEqualsTester(Constructor<?> constructor) {
		this.constructor = constructor;
		this.arguments = new InvokableArguments<>(constructor.getParameterTypes());
	}

	public SimpleImmutableValueObjectEqualsTester addArguments(int argIndex, Object... args) {
		this.arguments.addArg(argIndex, Argument::new, args);
		return this;
	}

	public void testEquals() {

		arguments.checkAllArgumentsSet();

		AccessibleRunner.run(constructor, () -> {
			EqualsTester tester = new EqualsTester();
			for (List<Argument> args : arguments.getCartesianProduct()) {
				try {
					Object[] invokeArgs = args.stream().map(a -> a.getValue()).toArray();
					Object o1 = constructor.newInstance(invokeArgs);
					Object o2 = constructor.newInstance(invokeArgs);
					tester.addEqualityGroup(o1, o2);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | InstantiationException e) {
					throw new ConstructorInvocationException(e);
				}
			}
			tester.testEquals();
			return null;
		});
	}

	public static class ConstructorInvocationException extends RuntimeException {

		private static final long serialVersionUID = -1533324172719220974L;

		ConstructorInvocationException(Exception cause) {
			super(cause);
		}
	}
}
