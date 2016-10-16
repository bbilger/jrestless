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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.jrestless.test.InvokableArguments.Argument;

/**
 * Capture all valid and invalid arguments for an invokable (constructor or
 * method) and test the preconditions and combinations against it.
 *
 * @author Bjoern Bilger
 *
 */
public abstract class PreconditionsTester {

	private final Invokable invokable;

	private final InvokableArguments<Argument> validArguments;
	private final InvokableArguments<InvalidArgument> invalidArguments;

	protected PreconditionsTester(Invokable invokable, Class<?>[] parameterTypes) {

		this.validArguments = new InvokableArguments<>(parameterTypes);
		this.invalidArguments = new InvokableArguments<>(parameterTypes);

		this.invokable = invokable;
	}

	/**
	 * Add a list of <b>valid</b> arguments for the parameter.
	 *
	 * @param paramIndex
	 *            the index of the parameter
	 * @param arguments
	 *            valid arguments for the parameter
	 * @return
	 */
	public PreconditionsTester addValidArgs(int paramIndex, Object... arguments) {
		validArguments.addArg(paramIndex, Argument::new, arguments);
		return this;
	}

	/**
	 * Add a list of <b>invalid</b> arguments for the parameter and the expected
	 * exception.
	 *
	 * @param paramIndex
	 * @param expectedException
	 * @param arguments
	 * @return
	 */
	public PreconditionsTester addInvalidArgs(int paramIndex, Class<? extends Exception> expectedException,
			Object... arguments) {
		invalidArguments.addArg(paramIndex, a -> new InvalidArgument(a, expectedException), arguments);
		return this;
	}

	/**
	 * Shortcut for {@link #addInvalidArgs(int, Class, Object...)
	 * addInvalidArgs(paramIndex, NullpointerException.class, new Object[]
	 * $&#123;null&#125;}.
	 *
	 * @param paramIndex
	 * @return
	 */
	public PreconditionsTester addInvalidNpeArg(int paramIndex) {
		addInvalidArgs(paramIndex, NullPointerException.class, new Object[] {null});
		return this;
	}

	/**
	 * Shortcut for {@link #addInvalidArgs(int, Class, Object...)
	 * addInvalidArgs(paramIndex, IllegalArgumentException.class, arguments}.
	 *
	 * @param paramIndex
	 * @return
	 */
	public PreconditionsTester addInvalidIaeArgs(int paramIndex, Object... arguments) {
		addInvalidArgs(paramIndex, IllegalArgumentException.class, arguments);
		return this;
	}

	public void testPreconditionsAndValidCombinations() {
		testValidCombinations();
		testPreconditions();
	}

	/**
	 * Tests all possible combinations valid of arguments on the invokable (constructor or method).
	 * <p>
	 * This requires at least one valid argument for <b>each</b> parameter.
	 *
	 * @throws AssertionError
	 *             if invoking the constructor with a valid set of arguments
	 *             throws an exception
	 */
	public void testValidCombinations() {
		validArguments.checkAllArgumentsSet();
		testArguments(validArguments.getCapturedArgumentsSets(), null);
	}

	/**
	 * Tests all preconditions on the invokable (constructor or method).
	 * <p>
	 * This requires all at least one invalid argument for parameter and at
	 * least one valid argument for <b>each</b> parameter.
	 *
	 * @throws AssertionError
	 *             if invoking the constructor with a valid set of arguments
	 *             throws an exception, or a set of arguments with one invalid
	 *             argument doesn't throw an exception or rather the expected
	 *             exception
	 *
	 */
	public void testPreconditions() {
		validArguments.checkAllArgumentsSet();
		boolean oneTested = false;
		for (int i = 0; i < validArguments.getCapturedArgumentsSets().size(); i++) {
			List<Set<Argument>> argumentsLists = new ArrayList<>(validArguments.getCapturedArgumentsSets());
			Set<InvalidArgument> currInvalidArgs = invalidArguments.getCapturedArgumentsSets().get(i);
			if (!currInvalidArgs.isEmpty()) {
				for (InvalidArgument invalidParam : currInvalidArgs) {
					argumentsLists.set(i, Collections.singleton(invalidParam));
					testArguments(argumentsLists, invalidParam.getExpectedException());
					oneTested = true;
				}
			}
		}
		if (!oneTested) {
			throw new IllegalStateException("no invlid arguments set - cannot test any preconditions");
		}
	}

	private void testArguments(List<Set<Argument>> argumentsLists, Class<? extends Exception> expectedException) {
		for (List<Argument> arguments : InvokableArguments.getCartesianProduct(argumentsLists)) {
			List<Object> argVals = arguments.stream().map(a -> a.getValue()).collect(Collectors.toList());
			try {
				invokable.invoke(argVals.toArray(new Object[argVals.size()]));
				if (expectedException != null) {
					throw new AssertionError(
							"expected " + expectedException.getName() + " to be thrown for arguments: " + argVals);
				}
			} catch (Exception e) {
				Throwable t = e;
				if (t instanceof InvocationTargetException) {
					t = ((InvocationTargetException) t).getTargetException();
				}
				if (expectedException == null) {
					throw new AssertionError("exception has been thrown for arguments: " + argVals, t);
				}
				if (!expectedException.isAssignableFrom(t.getClass())) {
					throw new AssertionError("expected " + expectedException.getClass() + " to be thrown but got "
							+ t.getClass() + " for arguments: " + argVals, t);
				}
			}
		}
	}

	private static class InvalidArgument extends Argument {
		private final Class<? extends Exception> expectedException;
		InvalidArgument(Object value, Class<? extends Exception> expectedException) {
			super(value);
			this.expectedException = expectedException;
		}
		Class<? extends Exception> getExpectedException() {
			return expectedException;
		}
		@Override
		public int hashCode() {
			/*
			 * do not take expectedException into account
			 * since only the argument's value is essential
			 */
			return super.hashCode();
		}
		@Override
		public boolean equals(Object obj) {
			/*
			 * do not take expectedException into account
			 * since only the argument's value is essential
			 */
			return super.equals(obj);
		}

	}

	protected interface Invokable {
		Object invoke(Object... args) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException;
	}
}
