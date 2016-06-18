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
import java.util.function.Supplier;

/**
 * Checks preconditions on a constructor.
 * <p>
 * A constructor must be provided, as well as a list of possible arguments for
 * each parameter {@link #addValidArgs(int, Object...) addValidArgs} (this is
 * enough if you want to test all valid combinations, only). Furthermore you
 * have to provide invalid arguments for at least one parameter (
 * {@link #addInvalidArgs(int, Class, Object...) addInvalidArgs},
 * {@link #addInvalidIaeArgs(int, Object...) addInvalidIaeArgs},
 * {@link #addInvalidNpeArg(int) addInvalidNpeArg}). Calling
 * {@link #testPreconditions() testPreconditions} will then test all invalid
 * combinations Calling {@link #testValidCombinations() testValidCombinations}
 * will test all valid combinations. In case you want to test both, you can use
 * {@link #testPreconditionsAndValidCombinations()}.
 * <p>
 * <b>It's essential to keep the list of possible arguments (valid and invalid)
 * for each parameter as low as possible since the possible combinations quickly
 * explode.</b>
 * <p>
 * Example for a 2-args constructor:
 * <ol>
 * <li>parameter 0
 * <ol>
 * <li>valid argument list: {1, 2}
 * <li>invalid argument list: {null, -1}
 * </ol>
 * <li>parameter 1
 * <ol>
 * <li>valid argument list: {3.0, 4.0}
 * <li>invalid argument list: {null}
 * </ol>
 * </ol>
 * <p>
 * Calling {@link #testPreconditions()} will invoke the constructor as follows:
 * <ul>
 * <li>(null, 3.0)
 * <li>(null, 4.0)
 * <li>(-1, 3.0)
 * <li>(-1, 4.0)
 * <li>(1, null)
 * <li>(2, null)
 * </ul>
 * So only one invalid parameter is passed to the constructor at a time.
 * <p>
 * Calling {@link #testValidCombinations()} will invoke the constructor as
 * follows:
 * <ul>
 * <li>(1, 3.0)
 * <li>(1, 4.0)
 * <li>(2, 3.0)
 * <li>(2, 4.0)
 * </ul>
 *
 *
 * @author Bjoern Bilger
 *
 */
public class ConstructorPreconditionsTester extends PreconditionsTester {

	private final Constructor<?> constructor;

	public ConstructorPreconditionsTester(Constructor<?> constructor) {
		super(constructor::newInstance, constructor.getParameterTypes());
		this.constructor = constructor;
	}

	@Override
	public void testValidCombinations() {
		Supplier<Void> fn = () -> {
			super.testValidCombinations();
			return null;
		};
		AccessibleRunner.run(constructor, fn);
	}

	@Override
	public void testPreconditions() {
		Supplier<Void> fn = () -> {
			super.testPreconditions();
			return null;
		};
		AccessibleRunner.run(constructor, fn);
	}
}
