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
package com.jrestless.core.container.io;

import static java.util.Objects.requireNonNull;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Default implementation of {@link JRestlessContainerRequestImpl}.
 *
 * @author Bjoern Bilger
 *
 */
public class JRestlessContainerRequestImpl implements JRestlessContainerRequest {

	private URI baseUri;
	private URI requestUri;
	private String httpMethod;
	private InputStream entityStream;
	private Map<String, List<String>> headers;

	public JRestlessContainerRequestImpl(@Nonnull URI baseUri, @Nonnull URI requestUri, @Nonnull String httpMethod,
			@Nonnull InputStream entityStream, @Nonnull Map<String, List<String>> headers) {
		requireNonNull(baseUri);
		requireNonNull(requestUri);
		requireNonNull(httpMethod);
		requireNonNull(entityStream);
		requireNonNull(headers);
		this.baseUri = baseUri;
		this.requestUri = requestUri;
		this.httpMethod = httpMethod;
		this.entityStream = entityStream;
		this.headers = new HashMap<>();
		for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
			List<String> value = entry.getValue();
			List<String> valueCopy = (value == null) ? null : Collections.unmodifiableList(new ArrayList<>(value));
			this.headers.put(entry.getKey(), valueCopy);
		}
		this.headers = Collections.unmodifiableMap(this.headers);
	}

	@Override
	public URI getBaseUri() {
		return baseUri;
	}

	@Override
	public URI getRequestUri() {
		return requestUri;
	}

	@Override
	public String getHttpMethod() {
		return httpMethod;
	}

	@Override
	public InputStream getEntityStream() {
		return entityStream;
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return headers;
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
		JRestlessContainerRequestImpl castOther = (JRestlessContainerRequestImpl) other;
		return Objects.equals(baseUri, castOther.baseUri) && Objects.equals(requestUri, castOther.requestUri)
				&& Objects.equals(httpMethod, castOther.httpMethod)
				&& Objects.equals(entityStream, castOther.entityStream) && Objects.equals(headers, castOther.headers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(baseUri, requestUri, httpMethod, entityStream, headers);
	}

	@Override
	public String toString() {
		return "JRestlessContainerRequestImpl [baseUri=" + baseUri + ", requestUri=" + requestUri + ", httpMethod="
				+ httpMethod + ", entityStream=" + entityStream + ", headers=" + headers + "]";
	}
}
