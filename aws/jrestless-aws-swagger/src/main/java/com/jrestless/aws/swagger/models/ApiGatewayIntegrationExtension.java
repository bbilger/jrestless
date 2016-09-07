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

import static com.jrestless.aws.swagger.util.Strings.requireNonBlank;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

/**
 * Represents AWS's API Gateway Swagger extension "x-amazon-apigateway-integration".
 *
 * @author Bjoern Bilger
 *
 */
public class ApiGatewayIntegrationExtension {

	public static final String EXTENSION_NAME = "x-amazon-apigateway-integration";

	private final String credentials;
	private final String uri;
	private final Map<String, String> requestTemplates;
	private final Map<String, String> requestParameters;
	private final Map<String, ApiGatewayIntegrationResponse> responses;

	public ApiGatewayIntegrationExtension(@Nonnull String credentials, @Nonnull String uri,
			@Nonnull Map<String, String> requestTemplates, @Nonnull Map<String, String> requestParameters,
			@Nonnull Map<String, ApiGatewayIntegrationResponse> responses) {
		requireNonBlank(credentials);
		requireNonBlank(uri);
		requireNonNull(requestTemplates);
		requireNonNull(requestParameters);
		requireNonNull(responses);
		this.credentials = credentials;
		this.uri = uri;
		this.requestTemplates = ImmutableMap.copyOf(requestTemplates);
		this.requestParameters = ImmutableMap.copyOf(requestParameters);
		this.responses = ImmutableMap.copyOf(responses);
	}

	public String getHttpMethod() {
		return "POST";
	}

	public String getType() {
		return "aws";
	}

	public String getCredentials() {
		return credentials;
	}

	public String getUri() {
		return uri;
	}

	public Map<String, String> getRequestTemplates() {
		return requestTemplates;
	}

	public Map<String, String> getRequestParameters() {
		return requestParameters;
	}

	public Map<String, ApiGatewayIntegrationResponse> getResponses() {
		return responses;
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
		ApiGatewayIntegrationExtension castOther = ApiGatewayIntegrationExtension.class.cast(other);
		return Objects.equals(credentials, castOther.credentials) && Objects.equals(uri, castOther.uri)
				&& Objects.equals(requestTemplates, castOther.requestTemplates)
				&& Objects.equals(requestParameters, castOther.requestParameters)
				&& Objects.equals(responses, castOther.responses);
	}

	@Override
	public int hashCode() {
		return Objects.hash(credentials, uri, requestTemplates, requestParameters, responses);
	}
}
