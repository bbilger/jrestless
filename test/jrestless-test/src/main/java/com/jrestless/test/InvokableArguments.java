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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import com.jrestless.test.InvokableArguments.Argument;


class InvokableArguments<T extends Argument> {

	private final List<Set<T>> argumentsSets;
	private final Class<?>[] parameterTypes;

	InvokableArguments(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
		int nrArgs = parameterTypes.length;
		if (nrArgs < 1) {
			throw new IllegalArgumentException("the constructor needs at least one parameter");
		}
		this.argumentsSets = new ArrayList<>();
		for (int i = 0; i < nrArgs; i++) {
			argumentsSets.add(new HashSet<>());
		}
	}

	void addArg(int paramIndex, Function<Object, T> argFactory, Object... arguments) {
		if (arguments == null) {
			argumentsSets.get(paramIndex).add(argFactory.apply(null));
		} else {
			for (Object argument : arguments) {
				checkArgumentType(paramIndex, argument);
				argumentsSets.get(paramIndex).add(argFactory.apply(argument));
			}
		}
	}

	List<Set<T>> getCapturedArgumentsSets() {
		return argumentsSets;
	}

	Set<List<T>> getCartesianProduct() {
		return getCartesianProduct(argumentsSets);
	}

	static <T> Set<List<T>> getCartesianProduct(List<Set<T>> arguments) {
		return Sets.cartesianProduct(arguments);
	}

	void checkAllArgumentsSet() {
		int paramIndex = 0;
		for (Set<T> validArgs : argumentsSets) {
			if (validArgs.size() < 1) {
				throw new IllegalStateException("no valid arguments defined for parameter with index " + paramIndex);
			}
			paramIndex++;
		}
	}

	private void checkArgumentType(int paramIndex, Object value) {
		Class<?> paramType = parameterTypes[paramIndex];
		boolean primitiveParam = paramType.isPrimitive();
		if (primitiveParam) {
			paramType = Primitives.wrap(paramType);
		}
		if (value == null && primitiveParam) {
			throw new IllegalArgumentException("primitive parameter cannot be null - index:" + paramIndex);
		} else if (value != null && !paramType.isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException("expected argument ('" + value + "') with index " + paramIndex
					+ " to be of type '" + paramType + "' but was '" + value.getClass() + "'");
		}
	}

	/**
	 * Wrap all values in a class since guava doesn't like null values in
	 * collections (required for the cartesian product).
	 *
	 * @author Bjoern Bilger
	 *
	 */
	protected static class Argument {
		private final Object value;
		protected Argument(Object value) {
			this.value = value;
		}
		protected Object getValue() {
			return value;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
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
			Argument other = (Argument) obj;
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}
	}

	protected interface Invokable {
		Object invoke(Object... args) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException;
	}
}
