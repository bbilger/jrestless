/*
 * Copyright 2017 Bjoern Bilger
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
package com.jrestless.openwhisk.webaction.io;

import java.util.Map;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Implementation of {@link WebActionRequest}.
 * <p>
 * The implementation makes sure that the request object can get de-serialized
 * into this representation (using GSON).
 *
 * @author Bjoern Bilger
 *
 */
public final class DefaultWebActionRequest implements WebActionRequest {
	@SerializedName("__ow_method")
	private String httpMethod;
	@SerializedName("__ow_headers")
	private Map<String, String> headers;
	@SerializedName("__ow_path")
	private String path;
	@SerializedName("__ow_user")
	private String user;
	@SerializedName("__ow_body")
	private String body;
	@SerializedName("__ow_query")
	private String query;

	public DefaultWebActionRequest() {
	}
	// for testing, only
	DefaultWebActionRequest(String httpMethod, Map<String, String> headers, String path, String user, String body,
			String query) {
		this.httpMethod = httpMethod;
		this.headers = headers;
		this.path = path;
		this.user = user;
		this.body = body;
		this.query = query;
	}

	@Override
	public String getHttpMethod() {
		return httpMethod;
	}

	@Override
	public Map<String, String> getHeaders() {
		return headers;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public String getQuery() {
		return query;
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
		DefaultWebActionRequest castOther = (DefaultWebActionRequest) other;
		return Objects.equals(httpMethod, castOther.httpMethod) && Objects.equals(headers, castOther.headers)
				&& Objects.equals(path, castOther.path) && Objects.equals(user, castOther.user)
				&& Objects.equals(body, castOther.body) && Objects.equals(query, castOther.query);
	}

	@Override
	public int hashCode() {
		return Objects.hash(httpMethod, headers, path, user, body, query);
	}
}
