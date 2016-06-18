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
package com.jrestless.aws.swagger.models;

import java.util.Objects;

/**
 * Represents AWS's API Gateway Swagger extension "x-amazon-apigateway-auth".
 *
 * @author Bjoern Bilger
 *
 */
public class ApiGatewayAuth {

	public static final String EXTENSION_NAME = "x-amazon-apigateway-auth";

	private final AuthType type;

	public ApiGatewayAuth(AuthType type) {
		Objects.requireNonNull(type);
		this.type = type;
	}

	public ApiGatewayAuth() {
		this(AuthType.aws_iam);
	}

	public AuthType getType() {
		return type;
	}

	public enum AuthType {
		// lower-case since jackson annotations aren't read by swagger
		none,
		aws_iam;
	}

	@Override
	public String toString() {
		return "ApiGatewayAuth [type=" + type + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		ApiGatewayAuth other = (ApiGatewayAuth) obj;
		if (type != other.type) {
			return false;
		}
		return true;
	}
}
