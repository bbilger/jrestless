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
package com.jrestless.aws.swagger;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.Objects;

import io.swagger.models.Operation;
import io.swagger.models.Swagger;

/**
 * Value object keeping track of the relation between and {@link Operation} and
 * its {@code Method}.
 *
 * @author Bjoern Bilger
 *
 */
public class OperationContext {

	private final Operation operation;
	private final Method endpointMethod;
	private final Swagger swagger;

	public OperationContext(Operation operation, Method endpointMethod, Swagger swagger) {
		requireNonNull(operation);
		requireNonNull(endpointMethod);
		requireNonNull(swagger);
		this.operation = operation;
		this.endpointMethod = endpointMethod;
		this.swagger = swagger;
	}

	public Operation getOperation() {
		return operation;
	}

	public Method getEndpointMethod() {
		return endpointMethod;
	}

	public Swagger getSwagger() {
		return swagger;
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
		OperationContext castOther = (OperationContext) other;
		return Objects.equals(operation, castOther.operation)
				&& Objects.equals(endpointMethod, castOther.endpointMethod)
				&& Objects.equals(swagger, castOther.swagger);
	}

	@Override
	public int hashCode() {
		return Objects.hash(operation, endpointMethod, swagger);
	}
}
