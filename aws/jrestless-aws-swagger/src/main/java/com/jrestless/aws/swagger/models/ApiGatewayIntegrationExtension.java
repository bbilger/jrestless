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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((credentials == null) ? 0 : credentials.hashCode());
		result = prime * result + ((requestParameters == null) ? 0 : requestParameters.hashCode());
		result = prime * result + ((requestTemplates == null) ? 0 : requestTemplates.hashCode());
		result = prime * result + ((responses == null) ? 0 : responses.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		ApiGatewayIntegrationExtension other = (ApiGatewayIntegrationExtension) obj;
		if (credentials == null) {
			if (other.credentials != null) {
				return false;
			}
		} else if (!credentials.equals(other.credentials)) {
			return false;
		}
		if (requestParameters == null) {
			if (other.requestParameters != null) {
				return false;
			}
		} else if (!requestParameters.equals(other.requestParameters)) {
			return false;
		}
		if (requestTemplates == null) {
			if (other.requestTemplates != null) {
				return false;
			}
		} else if (!requestTemplates.equals(other.requestTemplates)) {
			return false;
		}
		if (responses == null) {
			if (other.responses != null) {
				return false;
			}
		} else if (!responses.equals(other.responses)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
			return false;
		}
		return true;
	}
}
