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
package com.jrestless.aws.service.io;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
*
* Note: this object is mutable for de-serialization frameworks, only.
*
* @see ServiceRequest ServiceRequest
*
* @author Bjoern Bilger
*/
public final class DefaultServiceRequest extends ServiceDto implements ServiceRequest {
	private URI requestUri;
	private String httpMethod;

	/**
	 * For de-serialization frameworks, only.
	 */
	public DefaultServiceRequest() {
		super(Collections.emptyMap());
		// for de-serialization
	}

	public DefaultServiceRequest(@Nullable String body, @Nonnull Map<String, List<String>> headers,
			@Nonnull URI requestUri, @Nonnull String httpMethod) {
		super(body, headers, Collections.emptyMap());
		setRequestUri(requestUri);
		setHttpMethod(httpMethod);
	}

	/**
	 * Returns the request URI.
	 * <p>
	 * Note: it's possible that null is returned but this indicates an incorrect
	 * initialization.
	 *
	 * @return the request URI
	 */
	@Override
	public URI getRequestUri() {
		return requestUri;
	}

	/**
	 * For de-serialization frameworks, only.
	 */
	public void setRequestUri(@Nonnull URI requestUri) {
		requireNonNull(requestUri);
		this.requestUri = requestUri;
	}

	/**
	 * Returns the HTTP method.
	 * <p>
	 * Note: it's possible that null is returned but this indicates an incorrect
	 * initialization.
	 *
	 * @return the HTTP method
	 */
	@Override
	public String getHttpMethod() {
		return httpMethod;
	}

	/**
	 * For de-serialization frameworks, only.
	 */
	public void setHttpMethod(@Nonnull String httpMethod) {
		requireNonNull(httpMethod);
		this.httpMethod = httpMethod;
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
		DefaultServiceRequest castOther = (DefaultServiceRequest) other;
		return Objects.equals(getBody(), castOther.getBody()) && Objects.equals(getHeaders(), castOther.getHeaders())
				&& Objects.equals(requestUri, castOther.requestUri) && Objects.equals(httpMethod, castOther.httpMethod);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getBody(), getHeaders(), requestUri, httpMethod);
	}

	@Override
	public String toString() {
		return "ServiceRequestImpl [body=" + getBody() + ", headers=" + getHeaders() + ", requestUri=" + requestUri
				+ ", httpMethod=" + httpMethod + "]";
	}
}
