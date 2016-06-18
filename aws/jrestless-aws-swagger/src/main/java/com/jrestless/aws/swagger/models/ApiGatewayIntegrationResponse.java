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
 * Represents a single entry in the 'responses' field in AWS's API Gateway Swagger extension
 * 'x-amazon-apigateway-integration' (see {@link ApiGatewayIntegrationExtension}).
 *
 * @author Bjoern Bilger
 *
 */
public class ApiGatewayIntegrationResponse {

	private final String statusCode;
	private final Map<String, String> responseParameters;
	private final Map<String, String> responseTemplates;

	public ApiGatewayIntegrationResponse(@Nonnull String statusCode, @Nonnull Map<String, String> responseParameters,
			@Nonnull Map<String, String> responseTemplates) {
		requireNonBlank(statusCode);
		requireNonNull(responseParameters);
		requireNonNull(responseTemplates);
		this.statusCode = statusCode;
		this.responseParameters = ImmutableMap.copyOf(responseParameters);
		this.responseTemplates = ImmutableMap.copyOf(responseTemplates);
	}

	public String getStatusCode() {
		return statusCode;
	}

	public Map<String, String> getResponseParameters() {
		return responseParameters;
	}

	public Map<String, String> getResponseTemplates() {
		return responseTemplates;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((responseParameters == null) ? 0 : responseParameters.hashCode());
		result = prime * result + ((responseTemplates == null) ? 0 : responseTemplates.hashCode());
		result = prime * result + ((statusCode == null) ? 0 : statusCode.hashCode());
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
		ApiGatewayIntegrationResponse other = (ApiGatewayIntegrationResponse) obj;
		if (responseParameters == null) {
			if (other.responseParameters != null) {
				return false;
			}
		} else if (!responseParameters.equals(other.responseParameters)) {
			return false;
		}
		if (responseTemplates == null) {
			if (other.responseTemplates != null) {
				return false;
			}
		} else if (!responseTemplates.equals(other.responseTemplates)) {
			return false;
		}
		if (statusCode == null) {
			if (other.statusCode != null) {
				return false;
			}
		} else if (!statusCode.equals(other.statusCode)) {
			return false;
		}
		return true;
	}

}
