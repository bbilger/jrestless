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
package com.jrestless.aws.io;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response.StatusType;
/**
 * The default response that will the passed back to the API Gateway.
 * <p>
 * The implementation highly depends on the AWS API Gateway response template and
 * is designed to get serialized to it.
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayResponse {

	private final String body;
	private final Map<String, String> headers;
	private final int statusCode;

	/**
	 *
	 * @param body
	 * @param headers
	 * @param statusType
	 */
	public GatewayResponse(@Nullable String body, @Nonnull Map<String, String> headers,
			@Nonnull StatusType statusType) {
		requireNonNull(headers);
		requireNonNull(statusType);
		this.statusCode = statusType.getStatusCode();
		this.body = body;
		this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
	}

	public String getBody() {
		return body;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public int getStatusCode() {
		return statusCode;
	}



	@Override
	public String toString() {
		return "GatewayDefaultResponse [body=" + body + ", headers=" + headers + ", statusCode=" + statusCode + "]";
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
		GatewayResponse castOther = (GatewayResponse) other;
		return Objects.equals(body, castOther.body) && Objects.equals(headers, castOther.headers)
				&& Objects.equals(statusCode, castOther.statusCode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(body, headers, statusCode);
	}
}
