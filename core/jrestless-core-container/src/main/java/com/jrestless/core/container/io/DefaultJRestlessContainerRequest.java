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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Default implementation of {@link DefaultJRestlessContainerRequest}.
 *
 * @author Bjoern Bilger
 *
 */
public class DefaultJRestlessContainerRequest implements JRestlessContainerRequest {

	private URI baseUri;
	private URI requestUri;
	private String httpMethod;
	private InputStream entityStream;
	private Map<String, List<String>> headers;

	public DefaultJRestlessContainerRequest(@Nonnull URI baseUri, @Nonnull URI requestUri, @Nonnull String httpMethod,
			@Nonnull InputStream entityStream, @Nonnull Map<String, List<String>> headers) {
		this.baseUri = requireNonNull(baseUri);
		this.requestUri = requireNonNull(requestUri);
		this.httpMethod = requireNonNull(httpMethod);
		this.entityStream = requireNonNull(entityStream);
		requireNonNull(headers);
		this.headers = headers.entrySet().stream()
				.filter(e -> e.getKey() != null)
				.filter(e -> e.getValue() != null)
				.collect(Collectors.collectingAndThen(
						Collectors.toMap(
								Map.Entry::getKey,
								DefaultJRestlessContainerRequest::toImmutableList),
						Collections::unmodifiableMap));
	}

	private static List<String> toImmutableList(Map.Entry<String, List<String>> entry) {
		return Collections.unmodifiableList(new ArrayList<>(entry.getValue()));
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
		DefaultJRestlessContainerRequest castOther = (DefaultJRestlessContainerRequest) other;
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
