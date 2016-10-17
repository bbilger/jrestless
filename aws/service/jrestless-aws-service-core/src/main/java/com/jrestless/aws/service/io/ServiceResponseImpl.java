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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * Note: this object is mutable for de-serialization frameworks, only.
 *
 * @see ServiceResponse ServiceResponse
 *
 * @author Bjoern Bilger
 */
public final class ServiceResponseImpl implements ServiceResponse {
	private String body;
	private Map<String, List<String>> headers;
	private int statusCode;
	private String reasonPhrase;

	/**
	 * For de-serialization frameworks, only.
	 */
	public ServiceResponseImpl() {
		// for de-serialization
	}

	public ServiceResponseImpl(@Nullable String body, @Nonnull Map<String, List<String>> headers, int statusCode,
			@Nullable String reasonPhrase) {
		setBody(body);
		setHeaders(headers);
		setStatusCode(statusCode);
		setReasonPhrase(reasonPhrase);
	}

	@Override
	public String getBody() {
		// we don't copy the value for performance reasons
		return body;
	}

	/**
	 * For de-serialization frameworks, only.
	 */
	public void setBody(@Nullable String body) {
		// we don't copy the value for performance reasons
		this.body = body;
	}

	@Override
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * For de-serialization frameworks, only.
	 */
	public void setHeaders(@Nonnull Map<String, List<String>> headers) {
		requireNonNull(headers);
		this.headers = headers.entrySet().stream()
			.filter(e -> e.getKey() != null)
			.filter(e -> e.getValue() != null)
			.collect(Collectors.collectingAndThen(
					Collectors.toMap(
							e -> e.getKey(),
							ServiceResponseImpl::copyList),
					Collections::unmodifiableMap));
	}

	private static List<String> copyList(Map.Entry<String, List<String>> header) {
		return Collections.unmodifiableList(new ArrayList<>(header.getValue()));
	}

	@Override
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * For de-serialization frameworks, only.
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	@Override
	public String getReasonPhrase() {
		return reasonPhrase;
	}

	/**
	 * For de-serialization frameworks, only.
	 */
	public void setReasonPhrase(@Nullable String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
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
		ServiceResponseImpl castOther = (ServiceResponseImpl) other;
		return Objects.equals(body, castOther.body) && Objects.equals(headers, castOther.headers)
				&& Objects.equals(statusCode, castOther.statusCode)
				&& Objects.equals(reasonPhrase, castOther.reasonPhrase);
	}

	@Override
	public int hashCode() {
		return Objects.hash(body, headers, statusCode, reasonPhrase);
	}

	@Override
	public String toString() {
		return "ServiceResponseImpl [body=" + body + ", headers=" + headers + ", statusCode="
				+ statusCode + ", reasonPhrase=" + reasonPhrase + "]";
	}
}
