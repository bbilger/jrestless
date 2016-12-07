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

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public final class DefaultServiceResponse extends ServiceDto implements ServiceResponse {
	private int statusCode;
	private String reasonPhrase;

	/**
	 * For de-serialization frameworks, only.
	 */
	public DefaultServiceResponse() {
		super(null);
		// for de-serialization
	}

	public DefaultServiceResponse(@Nullable String body, @Nonnull Map<String, List<String>> headers, int statusCode,
			@Nullable String reasonPhrase) {
		super(body, headers, null);
		setStatusCode(statusCode);
		setReasonPhrase(reasonPhrase);
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
		DefaultServiceResponse castOther = (DefaultServiceResponse) other;
		return Objects.equals(getBody(), castOther.getBody()) && Objects.equals(getHeaders(), castOther.getHeaders())
				&& Objects.equals(statusCode, castOther.statusCode)
				&& Objects.equals(reasonPhrase, castOther.reasonPhrase);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getBody(), getHeaders(), statusCode, reasonPhrase);
	}

	@Override
	public String toString() {
		return "ServiceResponseImpl [body=" + getBody() + ", headers=" + getHeaders() + ", statusCode="
				+ statusCode + ", reasonPhrase=" + reasonPhrase + "]";
	}
}
