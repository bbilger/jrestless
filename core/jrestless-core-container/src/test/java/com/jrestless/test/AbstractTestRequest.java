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

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;

import com.jrestless.core.container.io.JRestlessContainerRequest;

public abstract class AbstractTestRequest implements JRestlessContainerRequest {

	private final URI baseUri;
	private final URI requestUri;
	private final String httpMethod;
	private final Map<String, List<String>> headers;


	public AbstractTestRequest(String requestUri, String httpMethod) {
		this("/", requestUri, httpMethod, new MultivaluedHashMap<>());

	}

	public AbstractTestRequest(String baseUri, String requestUri, String httpMethod, Map<String, List<String>> headers) {
		this(URI.create(baseUri), URI.create(requestUri), httpMethod, headers);

	}

	public AbstractTestRequest(URI baseUri, URI requestUri, String httpMethod, Map<String, List<String>> headers) {
		super();
		this.httpMethod = httpMethod;
		this.requestUri = requestUri;
		this.baseUri = baseUri;
		this.headers = headers;
	}

	@Override
	public String getHttpMethod() {
		return httpMethod;
	}

	@Override
	public URI getRequestUri() {
		return requestUri;
	}

	@Override
	public URI getBaseUri() {
		return baseUri;
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return headers;
	}
}
