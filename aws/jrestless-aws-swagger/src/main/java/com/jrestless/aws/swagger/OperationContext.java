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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endpointMethod == null) ? 0 : endpointMethod.hashCode());
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		result = prime * result + ((swagger == null) ? 0 : swagger.hashCode());
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
		OperationContext other = (OperationContext) obj;
		if (endpointMethod == null) {
			if (other.endpointMethod != null) {
				return false;
			}
		} else if (!endpointMethod.equals(other.endpointMethod)) {
			return false;
		}
		if (operation == null) {
			if (other.operation != null) {
				return false;
			}
		} else if (!operation.equals(other.operation)) {
			return false;
		}
		if (swagger == null) {
			if (other.swagger != null) {
				return false;
			}
		} else if (!swagger.equals(other.swagger)) {
			return false;
		}
		return true;
	}
}
