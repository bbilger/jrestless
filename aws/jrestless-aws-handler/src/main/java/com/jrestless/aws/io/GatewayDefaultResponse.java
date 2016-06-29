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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.RuntimeDelegate;

import org.glassfish.jersey.message.internal.HeaderUtils;
/**
 * The default response that will the passed back to the API Gateway.
 * <p>
 * The implementation highly depends on the AWS API Gateway response template and
 * is designed to get serialized to it.
 *
 * @author Bjoern Bilger
 *
 */
public class GatewayDefaultResponse {

	private final String body;
	private final Map<String, String> headers;
	private final int statusCode;

	/**
	 *
	 * @param body
	 * @param headers
	 * 		headers containing a null value will be filtered out
	 * @param statusType
	 */
	public GatewayDefaultResponse(@Nullable String body, @Nonnull Map<String, List<String>> headers,
			@Nonnull StatusType statusType) {
		this.statusCode = statusType.getStatusCode();
		this.body = body;
		this.headers = headers.entrySet().stream()
				.filter(e -> e.getValue() != null)
				.collect(Collectors.toMap(
						e -> e.getKey(),
						e -> asHeaderString(e.getValue())
				));
	}

	private String asHeaderString(List<String> headerValues) {
		return HeaderUtils.asHeaderString(headerValues.stream().collect(Collectors.toList()),
				RuntimeDelegate.getInstance());
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + statusCode;
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
		GatewayDefaultResponse other = (GatewayDefaultResponse) obj;
		if (body == null) {
			if (other.body != null) {
				return false;
			}
		} else if (!body.equals(other.body)) {
			return false;
		}
		if (headers == null) {
			if (other.headers != null) {
				return false;
			}
		} else if (!headers.equals(other.headers)) {
			return false;
		}
		if (statusCode != other.statusCode) {
			return false;
		}
		return true;
	}
}
