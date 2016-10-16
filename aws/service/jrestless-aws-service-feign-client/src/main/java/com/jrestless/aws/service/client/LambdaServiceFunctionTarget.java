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
package com.jrestless.aws.service.client;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import feign.Request;
import feign.RequestTemplate;
import feign.Target;

/**
 * Special {@link Target} that allows us to have no absolute base URL since for
 * internal AWS lambda calls all URLs must be non-absolute.
 *
 * @author Bjoern Bilger
 *
 * @param <T>
 *            the API interface type
 */
public class LambdaServiceFunctionTarget<T> implements Target<T> {

	private final String name;
	private final Class<T> type;

	public LambdaServiceFunctionTarget(Class<T> type) {
		this(type, "lambda service: " + type.getSimpleName());
	}

	public LambdaServiceFunctionTarget(Class<T> type, String name) {
		this.type = requireNonNull(type);
		this.name = requireNonNull(name);
	}

	@Override
	public Class<T> type() {
		return type;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String url() {
		// no absolute URL
		return "";
	}

	@Override
	public Request apply(RequestTemplate input) {
		return input.request();
	}

	@Override
	@SuppressWarnings("rawtypes")
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
		LambdaServiceFunctionTarget castOther = (LambdaServiceFunctionTarget) other;
		return Objects.equals(name, castOther.name) && Objects.equals(type, castOther.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public String toString() {
		return "LambdaFunctionTarget [name=" + name + ", type=" + type + "]";
	}
}
