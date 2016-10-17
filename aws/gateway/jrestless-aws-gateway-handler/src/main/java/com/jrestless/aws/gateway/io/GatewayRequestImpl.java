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
import java.util.HashMap;
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
public final class GatewayRequestImpl implements GatewayRequest {

	private String resource;
	private String path;
	private String httpMethod;
	private Map<String, String> headers = new HashMap<>();
	private Map<String, String> queryStringParameters = new HashMap<>();
	private Map<String, String> pathParameters = new HashMap<>();
	private Map<String, String> stageVariables = new HashMap<>();
	private GatewayRequestContext requestContext;
	private String body;

	public GatewayRequestImpl() {
		// for de-serialization
	}

	// for unit testing, only
	// CHECKSTYLE:OFF
	GatewayRequestImpl(String resource, String path, String httpMethod, Map<String, String> headers,
			Map<String, String> queryStringParameters, Map<String, String> pathParameters,
			Map<String, String> stageVariables, GatewayRequestContextImpl requestContext, String body) {
		setResource(resource);
		setPath(path);
		setHttpMethod(httpMethod);
		setHeaders(headers);
		setQueryStringParameters(queryStringParameters);
		setPathParameters(pathParameters);
		setStageVariables(stageVariables);
		setRequestContext(requestContext);
		setBody(body);
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
		return Collections.unmodifiableMap(headers);
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers.clear();
		if (headers != null) {
			this.headers.putAll(headers);
		}
	}

	@Override
	public Map<String, String> getQueryStringParameters() {
		return Collections.unmodifiableMap(queryStringParameters);
	}

	public void setQueryStringParameters(Map<String, String> queryStringParameters) {
		this.queryStringParameters.clear();
		if (queryStringParameters != null) {
			this.queryStringParameters.putAll(queryStringParameters);
		}
	}

	@Override
	public Map<String, String> getPathParameters() {
		return Collections.unmodifiableMap(pathParameters);
	}

	public void setPathParameters(Map<String, String> pathParameters) {
		this.pathParameters.clear();
		if (pathParameters != null) {
			this.pathParameters.putAll(pathParameters);
		}
	}

	@Override
	public Map<String, String> getStageVariables() {
		return Collections.unmodifiableMap(stageVariables);
	}

	public void setStageVariables(Map<String, String> stageVariables) {
		this.stageVariables.clear();
		if (stageVariables != null) {
			this.stageVariables.putAll(stageVariables);
		}
	}

	@Override
	public GatewayRequestContext getRequestContext() {
		return requestContext;
	}

	public void setRequestContext(GatewayRequestContextImpl requestContext) {
		this.requestContext = requestContext;
	}

	@Override
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
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
		GatewayRequestImpl castOther = (GatewayRequestImpl) other;
		return Objects.equals(resource, castOther.resource) && Objects.equals(path, castOther.path)
				&& Objects.equals(httpMethod, castOther.httpMethod) && Objects.equals(headers, castOther.headers)
				&& Objects.equals(queryStringParameters, castOther.queryStringParameters)
				&& Objects.equals(pathParameters, castOther.pathParameters)
				&& Objects.equals(stageVariables, castOther.stageVariables)
				&& Objects.equals(requestContext, castOther.requestContext) && Objects.equals(body, castOther.body);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resource, path, httpMethod, headers, queryStringParameters, pathParameters, stageVariables,
				requestContext, body);
	}

	@Override
	public String toString() {
		return "GatewayRequestImpl [resource=" + resource + ", path=" + path + ", httpMethod=" + httpMethod
				+ ", headers=" + headers + ", queryStringParameters=" + queryStringParameters + ", pathParameters="
				+ pathParameters + ", stageVariables=" + stageVariables + ", requestContext=" + requestContext
				+ ", body=" + body + "]";
	}
}
