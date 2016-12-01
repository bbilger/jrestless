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
package com.jrestless.aws.gateway.io;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of {@link GatewayRequest}.
 * <p>
 * The implementation makes sure that the request object passed from AWS API
 * Gateway can get de-serialized into this representation.
 *
 * @author Bjoern Bilger
 *
 */
public final class DefaultGatewayRequest implements GatewayRequest {

	private String resource;
	private String path;
	private String httpMethod;
	private Map<String, String> headers = Collections.emptyMap();
	private Map<String, String> queryStringParameters = Collections.emptyMap();
	private Map<String, String> pathParameters = Collections.emptyMap();
	private Map<String, String> stageVariables = Collections.emptyMap();
	private GatewayRequestContext requestContext;
	private String body;
	private boolean base64Encoded;

	public DefaultGatewayRequest() {
		// for de-serialization
	}

	// for unit testing, only
	// CHECKSTYLE:OFF
	DefaultGatewayRequest(String resource, String path, String httpMethod, Map<String, String> headers,
			Map<String, String> queryStringParameters, Map<String, String> pathParameters,
			Map<String, String> stageVariables, DefaultGatewayRequestContext requestContext, String body,
			boolean base64Encoded) {
		setResource(resource);
		setPath(path);
		setHttpMethod(httpMethod);
		setHeaders(headers);
		setQueryStringParameters(queryStringParameters);
		setPathParameters(pathParameters);
		setStageVariables(stageVariables);
		setRequestContext(requestContext);
		setBody(body);
		setIsBase64Encoded(base64Encoded);
	}
	// CHECKSTYLE:ON

	@Override
	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	@Override
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	@Override
	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = toUnmodifiableMap(headers);
	}

	@Override
	public Map<String, String> getQueryStringParameters() {
		return queryStringParameters;
	}

	public void setQueryStringParameters(Map<String, String> queryStringParameters) {
		this.queryStringParameters = toUnmodifiableMap(queryStringParameters);
	}

	@Override
	public Map<String, String> getPathParameters() {
		return pathParameters;
	}

	public void setPathParameters(Map<String, String> pathParameters) {
		this.pathParameters = toUnmodifiableMap(pathParameters);
	}

	@Override
	public Map<String, String> getStageVariables() {
		return stageVariables;
	}

	public void setStageVariables(Map<String, String> stageVariables) {
		this.stageVariables = toUnmodifiableMap(stageVariables);
	}

	@Override
	public GatewayRequestContext getRequestContext() {
		return requestContext;
	}

	public void setRequestContext(DefaultGatewayRequestContext requestContext) {
		this.requestContext = requestContext;
	}

	@Override
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	private Map<String, String> toUnmodifiableMap(Map<String, String> map) {
		if (map == null) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(map);
	}

	@Override
	public boolean isBase64Encoded() {
		return base64Encoded;
	}
	// the property is called "isBase64Encoded"
	public void setIsBase64Encoded(boolean base64Encoded) {
		this.base64Encoded = base64Encoded;
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
		DefaultGatewayRequest castOther = (DefaultGatewayRequest) other;
		return Objects.equals(resource, castOther.resource)
				&& Objects.equals(path, castOther.path)
				&& Objects.equals(httpMethod, castOther.httpMethod)
				&& Objects.equals(headers, castOther.headers)
				&& Objects.equals(queryStringParameters, castOther.queryStringParameters)
				&& Objects.equals(pathParameters, castOther.pathParameters)
				&& Objects.equals(stageVariables, castOther.stageVariables)
				&& Objects.equals(requestContext, castOther.requestContext)
				&& Objects.equals(body, castOther.body)
				&& Objects.equals(base64Encoded, castOther.base64Encoded);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resource, path, httpMethod, headers, queryStringParameters, pathParameters, stageVariables,
				requestContext, body, base64Encoded);
	}

	@Override
	public String toString() {
		return "DefaultGatewayRequest [resource=" + resource + ", path=" + path + ", httpMethod=" + httpMethod
				+ ", headers=" + headers + ", queryStringParameters=" + queryStringParameters + ", pathParameters="
				+ pathParameters + ", stageVariables=" + stageVariables + ", requestContext=" + requestContext
				+ ", body=" + body + ", base64Encoded=" + base64Encoded + "]";
	}
}
